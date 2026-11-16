package com.ac.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 식약처 레시피 API 를 호출해서 DB 에 적재한다.
 * - API 응답은 XML 로 받아 JDK 기본 XML 파서(DOM)로 읽는다.
 * - 적재는 별도 스레드에서 진행하고, 관리자 화면은 /admin/status 를 0.5초마다 조회(AJAX)해서
 *   진행 막대를 그린다.
 * - 동시에 두 번 실행되지 않도록 static + synchronized 로 막는다.
 */
public class RecipeLoader {

    // 진행 상태 (적재 스레드가 쓰고, 서블릿이 읽으므로 volatile)
    private static volatile boolean running;
    private static volatile boolean done;
    private static volatile String message = "아직 적재하지 않았습니다.";
    private static volatile int total, processed, inserted, updated, failed;

    /** 적재 시작. 이미 실행 중이면 false */
    public static synchronized boolean start() {
        if (running) return false;
        running = true;
        done = false;
        total = processed = inserted = updated = failed = 0;
        message = "API 호출 준비 중...";

        Thread thread = new Thread(() -> run(), "bapsim-recipe-loader");
        thread.setDaemon(true); // 톰캣이 꺼질 때 같이 종료
        thread.start();
        return true;
    }

    public static boolean isRunning() {
        return running;
    }

    public static String getMessage() {
        return message;
    }

    /** 진행 상태 JSON: {"running":true,"processed":809,"total":1156,...} */
    public static String statusJson(int recipeCount) {
        return "{\"running\":" + running + ",\"done\":" + done
             + ",\"message\":" + JsonUtil.quote(message)
             + ",\"total\":" + total + ",\"processed\":" + processed
             + ",\"inserted\":" + inserted + ",\"updated\":" + updated
             + ",\"failed\":" + failed
             + ",\"recipeCount\":" + recipeCount + "}";
    }

    public static int getTotal() { return total; }
    public static int getInserted() { return inserted; }
    public static int getUpdated() { return updated; }
    public static int getFailed() { return failed; }

    private static void run() {
        try {
            String key = Config.API_KEY;
            if (key.isEmpty() || !key.matches("[A-Za-z0-9]+")) {
                throw new IllegalStateException("src/main/resources/api.properties 의 apiKey 에 인증키를 입력하세요.");
            }
            int pageSize = Math.min(1000, Math.max(1, Config.API_PAGE_SIZE));

            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                RecipeSaveDAO dao = new RecipeSaveDAO(conn);

                // API 는 한 번에 최대 1,000건 → 1~1000, 1001~끝 으로 나눠 호출
                int start = 1;
                do {
                    int end = start + pageSize - 1;
                    message = String.format("API 호출 중 (%,d~%,d건)", start, total > 0 ? Math.min(end, total) : end);
                    Element root = fetch(key, start, end);

                    if (total == 0) total = parseInt(RecipeParser.text(root, "total_count"));
                    if (total == 0) throw new IllegalStateException("API 에서 받은 레시피가 없습니다.");

                    // API 가 가끔 일부만 돌려주는 경우가 있어서, 받아야 할 건수보다 적으면 최대 2번 다시 요청
                    int expected = Math.min(end, total) - start + 1;
                    NodeList rows = root.getElementsByTagName("row");
                    for (int retry = 1; retry <= 2 && rows.getLength() < expected; retry++) {
                        message = String.format("API 응답이 %,d건뿐이라 다시 요청 중 (%d번째)", rows.getLength(), retry);
                        root = fetch(key, start, end);
                        rows = root.getElementsByTagName("row");
                    }
                    for (int i = 0; i < rows.getLength(); i++) {
                        saveOne(conn, dao, (Element) rows.item(i));
                    }
                    if (rows.getLength() == 0) break; // 더 받을 데이터 없음
                    start = end + 1;
                } while (start <= total);
            }
            message = processed < total
                    ? String.format("적재 완료 (API 에서 %,d건 중 %,d건만 받음 - 다시 실행해 주세요)", total, processed)
                    : "적재 완료";
        } catch (Exception e) {
            message = "적재 실패: " + e.getMessage();
        } finally {
            running = false;
            done = true;
        }
    }

    private static void saveOne(Connection conn, RecipeSaveDAO dao, Element row) {
        try {
            Recipe recipe = RecipeParser.toRecipe(row);
            List<String> ingredients = RecipeParser.parseIngredientNames(recipe.getPartsRaw());
            boolean isNew = dao.save(recipe, ingredients);
            conn.commit();
            if (isNew) inserted++; else updated++;
        } catch (Exception e) {
            DBUtil.rollback(conn);
            failed++;
            try {
                dao.reloadIngredientCache();
            } catch (Exception ignore) {
            }
        } finally {
            processed++;
            message = String.format("%,d / %,d건 저장 중", processed, total);
        }
    }

    /** http://openapi.foodsafetykorea.go.kr/api/{키}/COOKRCP01/xml/{시작}/{끝} → XML 최상위 태그 */
    private static Element fetch(String key, int start, int end) throws Exception {
        String url = Config.API_BASE + "/" + key + "/COOKRCP01/xml/" + start + "/" + end;

        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
        http.setConnectTimeout(10_000);
        http.setReadTimeout(60_000);
        try {
            if (http.getResponseCode() != 200) {
                throw new IOException("API 응답 코드 " + http.getResponseCode());
            }
            Document doc;
            try (InputStream in = http.getInputStream()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                // 외부 DTD 를 읽지 않도록 설정 (XML 보안)
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.parse(in);
            }
            Element root = doc.getDocumentElement();

            // 정상: INFO-000, 데이터 없음: INFO-200, 그 외(인증키 오류 등)는 실패
            String code = RecipeParser.text(root, "CODE");
            if (!code.isEmpty() && !"INFO-000".equals(code) && !"INFO-200".equals(code)) {
                throw new IOException(RecipeParser.text(root, "MSG") + " (" + code + ")");
            }
            return root;
        } finally {
            http.disconnect();
        }
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
