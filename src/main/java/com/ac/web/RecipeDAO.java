package com.ac.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/** recipe · recipe_step 조회 */
public class RecipeDAO {

    /** 카드 목록에 필요한 컬럼 */
    static final String CARD_COLS =
            "r.id, r.name, r.category, r.cook_way, r.kcal, r.carb, r.protein, r.fat, r.sodium, "
          + "r.hash_tag, r.img_main, r.kcal_mismatch";

    static Recipe mapCard(ResultSet rs) throws SQLException {
        Recipe r = new Recipe();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setCategory(rs.getString("category"));
        r.setCookWay(rs.getString("cook_way"));
        r.setKcal(rs.getDouble("kcal"));
        r.setCarb(rs.getDouble("carb"));
        r.setProtein(rs.getDouble("protein"));
        r.setFat(rs.getDouble("fat"));
        r.setSodium(rs.getDouble("sodium"));
        r.setHashTag(rs.getString("hash_tag"));
        r.setImgMain(rs.getString("img_main"));
        r.setKcalMismatch(rs.getBoolean("kcal_mismatch"));
        return r;
    }

    /** LIKE 검색어의 %, _ 를 일반 문자로 처리 */
    static String likeParam(String s) {
        String escaped = s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    public int count() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM recipe")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------------- 메인 화면 ----------------

    /** 접속할 때마다 바뀌는 랜덤 추천 */
    public List<Recipe> findRandom(int limit) throws SQLException {
        return list("SELECT " + CARD_COLS + " FROM recipe r ORDER BY RAND() LIMIT ?", limit);
    }

    /** 저칼로리 추천: 200kcal 이하 중 랜덤 */
    public List<Recipe> findLowKcal(int limit) throws SQLException {
        return list("SELECT " + CARD_COLS + " FROM recipe r WHERE r.kcal > 0 AND r.kcal <= 200 "
                  + "ORDER BY RAND() LIMIT ?", limit);
    }

    private List<Recipe> list(String sql, int limit) throws SQLException {
        List<Recipe> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapCard(rs));
            }
        }
        return result;
    }

    // ---------------- 요리 검색 (음식 종류 화면) ----------------

    public Paging<Recipe> search(SearchCondition c) throws SQLException {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!c.getQ().isEmpty()) {
            where.append(" AND (r.name LIKE ? OR r.hash_tag LIKE ?)");
            params.add(likeParam(c.getQ()));
            params.add(likeParam(c.getQ()));
        }
        if (!c.getCat().isEmpty()) {
            where.append(" AND r.category = ?");
            params.add(c.getCat());
        }
        if (c.getKcalMax() > 0) {
            where.append(" AND r.kcal <= ?");
            params.add(c.getKcalMax());
        }
        if (c.getProteinMin() > 0) {
            where.append(" AND r.protein >= ?");
            params.add(c.getProteinMin());
        }
        if (c.getSodiumMax() > 0) {
            where.append(" AND r.sodium <= ?");
            params.add(c.getSodiumMax());
        }

        try (Connection conn = DBUtil.getConnection()) {
            int total;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM recipe r" + where)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getInt(1);
                }
            }

            String sql = "SELECT " + CARD_COLS + " FROM recipe r" + where
                       + " ORDER BY " + c.orderBy() + " LIMIT ? OFFSET ?";
            List<Recipe> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = bind(ps, params);
                ps.setInt(idx++, Paging.PAGE_SIZE);
                ps.setInt(idx, Paging.offset(c.getPage()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(mapCard(rs));
                }
            }
            return new Paging<>(items, total, c.getPage());
        }
    }

    /** 파라미터를 순서대로 바인딩하고 다음 인덱스를 돌려준다 */
    private static int bind(PreparedStatement ps, List<Object> params) throws SQLException {
        int i = 1;
        for (Object p : params) ps.setObject(i++, p);
        return i;
    }

    // ---------------- 음식 상세 ----------------

    public Recipe findById(int id) throws SQLException {
        String sql = "SELECT " + CARD_COLS + ", r.weight, r.parts_raw, r.tip FROM recipe r WHERE r.id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            Recipe r;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    r = mapCard(rs);
                    r.setWeight(rs.getString("weight"));
                    r.setPartsRaw(rs.getString("parts_raw"));
                    r.setTip(rs.getString("tip"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT description FROM recipe_step WHERE recipe_id = ? ORDER BY step_no")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) r.getSteps().add(rs.getString(1));
                }
            }
            return r;
        }
    }
}
