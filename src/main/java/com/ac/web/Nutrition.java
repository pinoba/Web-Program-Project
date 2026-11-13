package com.ac.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 영양 정보 5종 자바빈과 성인 1일 기준치 대비 비율.
 * Recipe 가 상속해서 음식 상세 화면의 영양 막대에 쓴다.
 */
public class Nutrition implements Serializable {
    private static final long serialVersionUID = 1L;

    // 성인 1일 영양성분 기준치 (고정)
    public static final double REF_KCAL = 2000;
    public static final double REF_CARB = 324;
    public static final double REF_PROTEIN = 55;
    public static final double REF_FAT = 54;
    public static final double REF_SODIUM = 2000;

    private double kcal;
    private double carb;
    private double protein;
    private double fat;
    private double sodium;

    public Nutrition() {}

    private static int pct(double value, double ref) {
        return (int) Math.round(value / ref * 100);
    }

    public int getKcalPct() {
        return pct(kcal, REF_KCAL);
    }
    public int getCarbPct() {
        return pct(carb, REF_CARB);
    }
    public int getProteinPct() {
        return pct(protein, REF_PROTEIN);
    }
    public int getFatPct() {
        return pct(fat, REF_FAT);
    }
    public int getSodiumPct() {
        return pct(sodium, REF_SODIUM);
    }

    /** 영양 막대 5줄 */
    public List<NutriRow> getRows() {
        List<NutriRow> rows = new ArrayList<>();
        rows.add(new NutriRow("kcal", "열량", kcal, "kcal", getKcalPct()));
        rows.add(new NutriRow("carb", "탄수화물", carb, "g", getCarbPct()));
        rows.add(new NutriRow("protein", "단백질", protein, "g", getProteinPct()));
        rows.add(new NutriRow("fat", "지방", fat, "g", getFatPct()));
        rows.add(new NutriRow("sodium", "나트륨", sodium, "mg", getSodiumPct()));
        return rows;
    }

    /** 탄수화물·단백질 4kcal/g, 지방 9kcal/g 으로 환산한 열량 */
    public double getCalcKcal() {
        return carb * 4 + protein * 4 + fat * 9;
    }

    public double getKcal() {
        return kcal;
    }
    public void setKcal(double kcal) {
        this.kcal = kcal;
    }
    public double getCarb() {
        return carb;
    }
    public void setCarb(double carb) {
        this.carb = carb;
    }
    public double getProtein() {
        return protein;
    }
    public void setProtein(double protein) {
        this.protein = protein;
    }
    public double getFat() {
        return fat;
    }
    public void setFat(double fat) {
        this.fat = fat;
    }
    public double getSodium() {
        return sodium;
    }
    public void setSodium(double sodium) {
        this.sodium = sodium;
    }
}
