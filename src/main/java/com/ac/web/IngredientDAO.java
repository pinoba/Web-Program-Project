package com.ac.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/** ingredient 테이블: 재료 자동완성 */
public class IngredientDAO {

    /**
     * 입력 글자가 포함된 재료 이름 (자동완성).
     * 입력 글자로 시작하는 재료를 먼저, 그다음 많이 쓰이는 재료 순으로 보여준다.
     */
    public List<String> suggest(String keyword, int limit) throws SQLException {
        String sql = "SELECT i.name, COUNT(ri.recipe_id) AS cnt FROM ingredient i "
                   + "LEFT JOIN recipe_ingredient ri ON ri.ingredient_id = i.id "
                   + "WHERE i.name LIKE ? GROUP BY i.id, i.name "
                   + "ORDER BY CASE WHEN i.name LIKE ? THEN 0 ELSE 1 END, cnt DESC, i.name "
                   + "LIMIT ?";
        List<String> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            ps.setString(1, "%" + escaped + "%");
            ps.setString(2, escaped + "%");
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString(1));
            }
        }
        return result;
    }
}
