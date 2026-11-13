package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 로그인
 * GET  /login  → login.jsp
 * POST /login  → 성공하면 세션에 회원 정보 저장 후 원래 보던 화면으로, 실패하면 오류 문구와 함께 login.jsp
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession().getAttribute(Config.LOGIN) != null) {
            response.sendRedirect(request.getContextPath() + "/main");
            return;
        }
        // 이동할 곳이 없으면 직전 화면(Referer)으로 돌아가도록 기억
        String redirect = request.getParameter("redirect");
        if (redirect == null) {
            String referer = request.getHeader("Referer");
            if (referer != null) {
                int idx = referer.indexOf(request.getContextPath() + "/");
                if (idx >= 0) redirect = referer.substring(idx);
            }
        }
        if (redirect != null && (redirect.contains("/login") || redirect.contains("/join"))) {
            redirect = null;
        }
        request.setAttribute("redirect", redirect);
        RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        String error = null;
        Member member = null;
        if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
            error = "아이디와 비밀번호를 모두 입력하세요.";
        } else {
            try {
                member = new MemberDAO().login(userId.trim(), password);
            } catch (SQLException e) {
                throw new ServletException(e);
            }
            if (member == null) error = "아이디 또는 비밀번호가 일치하지 않습니다.";
        }

        if (error != null) { // 입력값을 유지한 채 오류 문구 표시
            request.setAttribute("error", error);
            request.setAttribute("userId", userId);
            request.setAttribute("redirect", redirect);
            RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
            dispatcher.forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        request.changeSessionId(); // 로그인할 때 세션 ID 를 바꿔 세션 고정 공격 방지
        session.setAttribute(Config.LOGIN, member);

        // 같은 사이트 안의 주소로만 이동 (외부 주소로 보내는 것 방지)
        String ctx = request.getContextPath();
        if (redirect == null || !redirect.startsWith(ctx + "/") || redirect.startsWith("//")) {
            redirect = ctx + "/main";
        }
        response.sendRedirect(redirect);
    }
}
