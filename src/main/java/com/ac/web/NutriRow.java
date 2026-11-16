package com.ac.web;

/** 영양 막대 한 줄 (예: 단백질 3.1g, 기준치 대비 6%) — JSP 에서 c:forEach 로 출력 */
public class NutriRow {

    private String label;  // 열량, 탄수화물 ...
    private double value;
    private String unit;   // kcal, g, mg
    private int pct;       // 1일 기준치 대비 %

    public NutriRow() {}

    public NutriRow(String label, double value, String unit, int pct) {
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.pct = pct;
    }

    /** 100% 를 넘으면 빨간색 경고 */
    public boolean isOver() {
        return pct > 100;
    }

    /** 막대 길이는 최대 100% */
    public int getBarWidth() {
        return Math.min(pct, 100);
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public int getPct() {
        return pct;
    }
    public void setPct(int pct) {
        this.pct = pct;
    }
}
