package com.ac.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 서버가 시작될 때 한 번 실행되는 초기화 서블릿 (loadOnStartup = 1).
 * 서블릿 생명주기의 init() 에서
 *  1) WEB-INF/schema.sql 로 테이블 생성 (이미 있으면 건너뜀)
 *  2) 관리자 계정이 없으면 생성
 */
@WebServlet(urlPatterns = "/init", loadOnStartup = 1)
public class InitServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public InitServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        try {
            runSchema();
            MemberDAO memberDAO = new MemberDAO();
            memberDAO.ensureAdmin(Config.ADMIN_ID, Config.ADMIN_PASSWORD);
            System.out.println("<bapsim> DB 초기화 완료");
        } catch (Exception e) {
            throw new ServletException("DB 초기화 실패", e);
        }
    }

    /** schema.sql 의 주석을 지우고 ; 기준으로 한 문장씩 실행 */
    private void runSchema() throws Exception {
        String sql;
        try (InputStream in = getServletContext().getResourceAsStream("/WEB-INF/schema.sql")) {
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\\r?\\n")) {
            int idx = line.indexOf("--");
            clean.append(idx >= 0 ? line.substring(0, idx) : line).append('\n');
        }
        try (Connection conn = DBUtil.getConnection(); Statement st = conn.createStatement()) {
            for (String stmt : clean.toString().split(";")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/main");
    }
}
