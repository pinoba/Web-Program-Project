package com.ac.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 관리자 데이터 적재용 저장 DAO.
 * 하나의 Connection 으로 여러 레시피를 저장하며, 레시피 한 건이 한 트랜잭션이다.
 */
public class RecipeSaveDAO {

    private final Connection conn;

    public RecipeSaveDAO(Connection conn) {
        this.conn = conn;
    }

    /** 레시피 저장. 이미 있으면 갱신하고 false, 새로 넣었으면 true */
    public boolean save(Recipe r) throws SQLException {
        boolean exists;
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM recipe WHERE id = ?")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
        }

        if (exists) {
            update(r);
            deleteSteps(r.getId());
        } else {
            insert(r);
        }
        insertSteps(r);
        return !exists;
    }

    private void insert(Recipe r) throws SQLException {
        String sql = "INSERT INTO recipe (name, category, cook_way, weight, kcal, carb, protein, fat, sodium, "
                   + "hash_tag, img_main, parts_raw, tip, kcal_mismatch, id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindRecipe(ps, r);
            ps.executeUpdate();
        }
    }

    private void update(Recipe r) throws SQLException {
        String sql = "UPDATE recipe SET name=?, category=?, cook_way=?, weight=?, kcal=?, carb=?, protein=?, "
                   + "fat=?, sodium=?, hash_tag=?, img_main=?, parts_raw=?, tip=?, kcal_mismatch=?, "
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
        ps.setBoolean(14, r.isKcalMismatch());
        ps.setInt(15, r.getId());
    }

    private void deleteSteps(int recipeId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM recipe_step WHERE recipe_id = ?")) {
            ps.setInt(1, recipeId);
            ps.executeUpdate();
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
}
