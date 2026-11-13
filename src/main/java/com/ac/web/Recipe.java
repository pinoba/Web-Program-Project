package com.ac.web;

import java.util.ArrayList;
import java.util.List;

/** 레시피 한 건 (목록 카드, 상세, 최근 본 레시피에서 사용) */
public class Recipe extends Nutrition {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String category;
    private String cookWay;
    private String weight;
    private String hashTag;
    private String imgMain;
    private String partsRaw;
    private String tip;
    private boolean kcalMismatch;

    private List<String> steps = new ArrayList<>();          // 상세: 조리 순서

    /** 화면 표시용 요리종류 (국&찌개 → 국·찌개) */
    public String getCategoryLabel() {
        return category == null ? "" : category.replace("&", "·");
    }

    /** 재료 원문을 줄 단위로 나눠 화면에 표시 (분량·그룹은 원문 그대로) */
    public List<String> getPartsLines() {
        List<String> lines = new ArrayList<>();
        if (partsRaw == null) return lines;
        for (String line : partsRaw.replace("●", "\n").split("\\r?\\n")) {
            String t = line.trim();
            if (!t.isEmpty()) lines.add(t);
        }
        return lines;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCookWay() { return cookWay; }
    public void setCookWay(String cookWay) { this.cookWay = cookWay; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getHashTag() { return hashTag; }
    public void setHashTag(String hashTag) { this.hashTag = hashTag; }
    public String getImgMain() { return imgMain; }
    public void setImgMain(String imgMain) { this.imgMain = imgMain; }
    public String getPartsRaw() { return partsRaw; }
    public void setPartsRaw(String partsRaw) { this.partsRaw = partsRaw; }
    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }
    public boolean isKcalMismatch() { return kcalMismatch; }
    public void setKcalMismatch(boolean kcalMismatch) { this.kcalMismatch = kcalMismatch; }
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
}
