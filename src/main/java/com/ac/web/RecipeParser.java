package com.ac.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 식약처 API(COOKRCP01) 의 레시피 한 건(XML 의 <row>)을 정제해서 Recipe 로 바꾼다.
 *
 * 데이터 문제와 처리 방법
 *  - 재료 문자열 형식이 제각각 (●, 그룹명 :, 줄바꿈, [1인분]) → 재료 이름만 뽑고 원문은 그대로 저장
 *  - 조리 단계 앞의 "1. " 번호와 끝의 영문 한 글자(a, b, c) → 제거
 */
public class RecipeParser {

    private static final Pattern BRACKET = Pattern.compile("\\[[^\\]]*\\]");          // [1인분]
    private static final Pattern PAREN = Pattern.compile("\\([^)]*\\)?");             // (3/4모)
    private static final Pattern BEFORE_DIGIT = Pattern.compile("^([^0-9]*)");        // 숫자 앞까지 = 재료명
    private static final Pattern AMOUNT_WORD =
            Pattern.compile("\\s*(약간|적당량|적량|조금|소량|한줌|한 줌|한꼬집|한 꼬집|기호에 맞게)\\s*$");
    private static final Pattern GROUP_PREFIX =
            Pattern.compile("^(주재료|부재료|재료|양념장|양념|소스|고명|드레싱)\\s+");
    private static final Pattern HAS_LETTER = Pattern.compile("[가-힣A-Za-z]");
    private static final Pattern STEP_NUMBER = Pattern.compile("^\\s*\\d+\\s*[.)]\\s*");
    private static final Pattern STEP_TAIL = Pattern.compile("(?<=[^A-Za-z0-9\\s])\\s*[A-Za-z]$");

    /** <row> 하나 → Recipe (조리 단계 포함) */
    public static Recipe toRecipe(Element row) {
        Recipe r = new Recipe();
        r.setId(Integer.parseInt(text(row, "RCP_SEQ")));
        r.setName(text(row, "RCP_NM"));
        r.setCategory(text(row, "RCP_PAT2"));
        r.setCookWay(text(row, "RCP_WAY2"));
        r.setWeight(text(row, "INFO_WGT"));
        r.setKcal(number(row, "INFO_ENG"));
        r.setCarb(number(row, "INFO_CAR"));
        r.setProtein(number(row, "INFO_PRO"));
        r.setFat(number(row, "INFO_FAT"));
        r.setSodium(number(row, "INFO_NA"));
        r.setHashTag(text(row, "HASH_TAG"));
        r.setImgMain(text(row, "ATT_FILE_NO_MAIN"));
        r.setPartsRaw(text(row, "RCP_PARTS_DTLS"));
        r.setTip(text(row, "RCP_NA_TIP"));

        List<String> steps = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String step = cleanStep(text(row, String.format("MANUAL%02d", i)));
            if (!step.isEmpty()) steps.add(step);
        }
        r.setSteps(steps);
        return r;
    }

    /** 재료 원문에서 재료 이름만 추출 (중복 제거, 순서 유지) */
    public static List<String> parseIngredientNames(String raw) {
        Set<String> names = new LinkedHashSet<>();
        if (raw == null) return new ArrayList<>(names);

        for (String line : raw.replace("●", "\n").split("\\r?\\n")) {
            line = BRACKET.matcher(line).replaceAll(" ").trim();
            if (line.isEmpty()) continue;

            int colon = line.lastIndexOf(':');
            if (colon >= 0) {
                line = line.substring(colon + 1);          // "조림장 : 감자전분 6g" → 그룹명 제거
            } else if (line.indexOf(',') < 0 && !line.matches(".*\\d.*")) {
                continue;                                   // "고명", 요리 이름 같은 제목 줄
            }

            for (String token : line.split("[,，]")) {
                String name = cleanIngredient(token);
                if (name != null) names.add(name);
            }
        }
        return new ArrayList<>(names);
    }

    private static String cleanIngredient(String token) {
        String t = PAREN.matcher(token).replaceAll(" ");
        Matcher m = BEFORE_DIGIT.matcher(t);
        t = m.find() ? m.group(1) : t;                     // "연두부 75g" → "연두부 "
        t = AMOUNT_WORD.matcher(t.trim()).replaceAll("");    // "소금 약간" → "소금"
        t = GROUP_PREFIX.matcher(t.trim()).replaceAll("");   // "재료 곤약" → "곤약"
        t = t.replaceAll("^[-·•*\\s]+|[-·•*.\\s]+$", "").replaceAll("\\s+", " ");
        if (t.isEmpty() || t.length() > 20 || !HAS_LETTER.matcher(t).find()) return null;
        return t;
    }

    /** "1. 두부를 썬다.a" → "두부를 썬다." */
    public static String cleanStep(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\r", " ").replace("\n", " ").trim();
        s = STEP_NUMBER.matcher(s).replaceFirst("");
        s = STEP_TAIL.matcher(s).replaceFirst("");
        return s.trim();
    }

    /** <row> 안의 태그 글자 (없으면 "") */
    static String text(Element row, String tag) {
        NodeList list = row.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }

    private static double number(Element row, String tag) {
        try {
            return Double.parseDouble(text(row, tag));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
