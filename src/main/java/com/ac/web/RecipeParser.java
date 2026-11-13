package com.ac.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 식약처 API(COOKRCP01) 의 레시피 한 건(XML 의 <row>)을 정제해서 Recipe 로 바꾼다.
 *
 * 데이터 문제와 처리 방법
 *  - 재료 문자열은 원문 그대로 저장 (상세 화면에서 줄 단위로 표시)
 *  - 조리 단계 앞의 "1. " 번호와 끝의 영문 한 글자(a, b, c) → 제거
 *  - 표기 열량과 탄·단·지 환산 열량이 크게 다른 레시피 → kcalMismatch 표시
 */
public class RecipeParser {

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
        r.setKcalMismatch(isKcalMismatch(r.getKcal(), r.getCalcKcal()));

        List<String> steps = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String step = cleanStep(text(row, String.format("MANUAL%02d", i)));
            if (!step.isEmpty()) steps.add(step);
        }
        r.setSteps(steps);
        return r;
    }

    /** "1. 두부를 썬다.a" → "두부를 썬다." */
    public static String cleanStep(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\r", " ").replace("\n", " ").trim();
        s = STEP_NUMBER.matcher(s).replaceFirst("");
        s = STEP_TAIL.matcher(s).replaceFirst("");
        return s.trim();
    }

    /** 표기 열량과 환산 열량의 차이가 50kcal 이상이면서 30% 이상이면 이상치로 본다 */
    public static boolean isKcalMismatch(double kcal, double calcKcal) {
        if (kcal <= 0) return false;
        double diff = Math.abs(kcal - calcKcal);
        return diff >= 50 && diff >= kcal * 0.3;
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
