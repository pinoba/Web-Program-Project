package com.ac.web;

import javax.servlet.http.HttpServletRequest;

/**
 * 요리 검색 조건 (검색어 · 필터 선택 상자 · 정렬 · 페이지).
 * 선택 상자 값은 정해진 값만 허용해서 SQL 에 그대로 들어가지 않도록 숫자로 바꿔 쓴다.
 */
public class SearchCondition {

    private String q = "";
    private String cat = "";
    private int kcalMax;     // 0 = 전체
    private int proteinMin;  // 0 = 전체
    private int sodiumMax;   // 0 = 전체
    private String sort = "";
    private int page = 1;

    public static SearchCondition from(HttpServletRequest req) {
        SearchCondition c = new SearchCondition();
        c.q = trim(req.getParameter("q"));
        if (c.q.startsWith("#")) c.q = c.q.substring(1).trim(); // 해시태그 검색 허용
        c.cat = trim(req.getParameter("cat"));
        c.kcalMax = allowed(parseInt(req.getParameter("kcal"), 0), 200, 400, 600);
        c.proteinMin = allowed(parseInt(req.getParameter("protein"), 0), 10, 20, 30);
        c.sodiumMax = allowed(parseInt(req.getParameter("sodium"), 0), 300, 600, 1000);
        String s = trim(req.getParameter("sort"));
        c.sort = (s.equals("kcal") || s.equals("protein") || s.equals("name")) ? s : "";
        c.page = Math.max(1, parseInt(req.getParameter("page"), 1));
        return c;
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static int allowed(int value, int... options) {
        for (int o : options) if (o == value) return value;
        return 0;
    }

    /** 정렬 키를 ORDER BY 절로 변환 (화이트리스트) */
    public String orderBy() {
        switch (sort) {
            case "kcal":    return "r.kcal ASC, r.id ASC";
            case "protein": return "r.protein DESC, r.id ASC";
            case "name":    return "r.name ASC";
            default:        return "r.id ASC";
        }
    }

    public String getQ() { return q; }
    public String getCat() { return cat; }
    public int getKcalMax() { return kcalMax; }
    public int getProteinMin() { return proteinMin; }
    public int getSodiumMax() { return sodiumMax; }
    public String getSort() { return sort; }
    public int getPage() { return page; }
}
