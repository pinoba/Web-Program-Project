package com.ac.web;

import java.util.List;

/** 페이징된 목록 결과와 페이지 번호 계산 (한 페이지 12개) */
public class Paging<T> {

    public static final int PAGE_SIZE = 12;
    private static final int BLOCK = 5; // 한 번에 보여줄 페이지 번호 개수

    private final List<T> items;
    private final int total;
    private final int page;

    public Paging(List<T> items, int total, int page) {
        this.items = items;
        this.total = total;
        this.page = page;
    }

    public static int offset(int page) {
        return (Math.max(page, 1) - 1) * PAGE_SIZE;
    }

    public List<T> getItems() { return items; }
    public int getTotal() { return total; }
    public int getPage() { return page; }

    public int getTotalPages() {
        return Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public int getStartPage() {
        int start = page - BLOCK / 2;
        int maxStart = Math.max(1, getTotalPages() - BLOCK + 1);
        return Math.max(1, Math.min(start, maxStart));
    }

    public int getEndPage() {
        return Math.min(getTotalPages(), getStartPage() + BLOCK - 1);
    }
}
