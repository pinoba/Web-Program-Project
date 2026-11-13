package com.ac.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/** member 테이블: 회원가입, 로그인, 아이디 중복 확인 */
public class MemberDAO {

    public boolean existsUserId(String userId) throws SQLException {
        String sql = "SELECT 1 FROM member WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** 회원 저장 (비밀번호는 솔트 + 해시로 변환) */
    public void insert(String userId, String password, String nickname, String role) throws SQLException {
        String salt = PasswordUtil.newSalt();
        String sql = "INSERT INTO member (user_id, password, salt, nickname, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, PasswordUtil.hash(password, salt));
            ps.setString(3, salt);
            ps.setString(4, nickname);
            ps.setString(5, role);
            ps.executeUpdate();
        }
    }

    /** 아이디·비밀번호가 맞으면 회원 정보, 아니면 null */
    public Member login(String userId, String password) throws SQLException {
        String sql = "SELECT id, user_id, password, salt, nickname, role FROM member WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!PasswordUtil.matches(password, rs.getString("salt"), rs.getString("password"))) {
                    return null;
                }
                Member m = new Member();
                m.setId(rs.getInt("id"));
                m.setUserId(rs.getString("user_id"));
                m.setNickname(rs.getString("nickname"));
                m.setRole(rs.getString("role"));
                return m;
            }
        }
    }

    /** 서버 첫 실행 시 관리자 계정이 없으면 만든다 */
    public void ensureAdmin(String adminId, String adminPassword) throws SQLException {
        if (!existsUserId(adminId)) {
            insert(adminId, adminPassword, "관리자", "ADMIN");
        }
    }
}
