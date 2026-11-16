package com.ac.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 관리자 데이터 적재용 저장 DAO.
 * 하나의 Connection 으로 여러 레시피를 저장하며, 레시피 한 건이 한 트랜잭션이다.
 */
public class RecipeSaveDAO {

    private final Connection conn;
    private final Map<String, Integer> ingredientCache = new HashMap<>();

    public RecipeSaveDAO(Connection conn) throws SQLException {
        this.conn = conn;
        reloadIngredientCache();
    }

    /** 저장 실패(롤백) 후에는 캐시와 DB 가 어긋날 수 있으므로 다시 읽는다 */
    public void reloadIngredientCache() throws SQLException {
        ingredientCache.clear();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM ingredient")) {
            while (rs.next()) ingredientCache.put(rs.getString(2), rs.getInt(1));
        }
    }

    /** 레시피 저장. 이미 있으면 갱신하고 false, 새로 넣었으면 true */
    public boolean save(Recipe r, List<String> ingredientNames) throws SQLException {
        boolean exists;
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM recipe WHERE id = ?")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
        }

        if (exists) {
            update(r);
            deleteChildren(r.getId());
        } else {
            insert(r);
        }
        insertSteps(r);
        insertIngredients(r.getId(), ingredientNames);
        return !exists;
    }

    private void insert(Recipe r) throws SQLException {
        String sql = "INSERT INTO recipe (name, category, cook_way, weight, kcal, carb, protein, fat, sodium, "
                   + "hash_tag, img_main, parts_raw, tip, id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindRecipe(ps, r);
            ps.executeUpdate();
        }
    }

    private void update(Recipe r) throws SQLException {
        String sql = "UPDATE recipe SET name=?, category=?, cook_way=?, weight=?, kcal=?, carb=?, protein=?, "
                   + "fat=?, sodium=?, hash_tag=?, img_main=?, parts_raw=?, tip=?, "
                   + "updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindRecipe(ps, r);
            ps.executeUpdate();
        }
    }

    /** insert / update 가 같은 컬럼 순서를 쓰도록 맞춤 (id 가 마지막) */
    private void bindRecipe(PreparedStatement ps, Recipe r) throws SQLException {
        ps.setString(1, r.getName());
        ps.setString(2, r.getCategory());
        ps.setString(3, r.getCookWay());
        ps.setString(4, r.getWeight());
        ps.setDouble(5, r.getKcal());
        ps.setDouble(6, r.getCarb());
        ps.setDouble(7, r.getProtein());
        ps.setDouble(8, r.getFat());
        ps.setDouble(9, r.getSodium());
        ps.setString(10, r.getHashTag());
        ps.setString(11, r.getImgMain());
        ps.setString(12, r.getPartsRaw());
        ps.setString(13, r.getTip());
        ps.setInt(14, r.getId());
    }

    private void deleteChildren(int recipeId) throws SQLException {
        for (String table : new String[] { "recipe_step", "recipe_ingredient" }) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE recipe_id = ?")) {
                ps.setInt(1, recipeId);
                ps.executeUpdate();
            }
        }
    }

    private void insertSteps(Recipe r) throws SQLException {
        String sql = "INSERT INTO recipe_step (recipe_id, step_no, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int no = 1;
            for (String step : r.getSteps()) {
                ps.setInt(1, r.getId());
                ps.setInt(2, no++);
                ps.setString(3, step.length() > 1000 ? step.substring(0, 1000) : step);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertIngredients(int recipeId, List<String> names) throws SQLException {
        Set<Integer> ids = new LinkedHashSet<>();
        for (String name : names) ids.add(ingredientId(name));

        String sql = "INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer id : ids) {
                ps.setInt(1, recipeId);
                ps.setInt(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** 재료 이름의 id (없으면 새로 저장) */
    private int ingredientId(String name) throws SQLException {
        Integer cached = ingredientCache.get(name);
        if (cached != null) return cached;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ingredient (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                int id = rs.getInt(1);
                ingredientCache.put(name, id);
                return id;
            }
        }
    }
}
