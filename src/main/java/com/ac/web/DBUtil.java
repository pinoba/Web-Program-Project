package com.ac.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB 연결을 한 곳에서 관리한다.
 * 다른 DB(MySQL 등)로 바꿀 때는 드라이버 이름과 Config.DB_URL 만 바꾸면 된다.
 */
public class DBUtil {

    private static final String DRIVER = "org.h2.Driver";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("JDBC 드라이버를 찾을 수 없습니다: " + DRIVER);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);
    }

    /** 트랜잭션 실패 시 롤백 (예외는 무시) */
    public static void rollback(Connection conn) {
        if (conn == null) return;
        try {
            conn.rollback();
        } catch (SQLException ignore) {
        }
    }
}
