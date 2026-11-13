package com.ac.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/** favorite 테이블: 즐겨찾기 저장·해제·목록 */
public class FavoriteDAO {

    public boolean exists(int memberId, int recipeId) throws SQLException {
        String sql = "SELECT 1 FROM favorite WHERE member_id = ? AND recipe_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void add(int memberId, int recipeId) throws SQLException {
        String sql = "INSERT INTO favorite (member_id, recipe_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, recipeId);
            ps.executeUpdate();
        }
    }

    public void remove(int memberId, int recipeId) throws SQLException {
        String sql = "DELETE FROM favorite WHERE member_id = ? AND recipe_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, recipeId);
            ps.executeUpdate();
        }
    }

    /** 저장되어 있으면 해제, 없으면 저장. 결과 상태(true = 즐겨찾기됨)를 돌려준다 */
    public boolean toggle(int memberId, int recipeId) throws SQLException {
        if (exists(memberId, recipeId)) {
            remove(memberId, recipeId);
            return false;
        }
        add(memberId, recipeId);
        return true;
    }

    public List<Recipe> findByMember(int memberId) throws SQLException {
        String sql = "SELECT " + RecipeDAO.CARD_COLS + " FROM favorite f "
                   + "JOIN recipe r ON r.id = f.recipe_id WHERE f.member_id = ? ORDER BY f.created_at DESC";
        List<Recipe> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(RecipeDAO.mapCard(rs));
            }
        }
        return result;
    }
}
