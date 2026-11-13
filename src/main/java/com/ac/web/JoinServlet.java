package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 회원가입 (화면의 JS 검사와 별개로 서버에서도 한 번 더 검사)
 * GET  /join → join.jsp
 * POST /join → 가입 후 로그인 화면으로
 */
@WebServlet("/join")
public class JoinServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final String ID_PATTERN = "^[A-Za-z0-9]{4,20}$";

    public JoinServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("join.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String userId = trim(request.getParameter("userId"));
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        String nickname = trim(request.getParameter("nickname"));

        MemberDAO memberDAO = new MemberDAO();
        String error = null;
        try {
            if (userId.isEmpty() || password == null || password.isBlank() || nickname.isEmpty()) {
                error = "빈 칸을 모두 입력하세요.";
            } else if (!userId.matches(ID_PATTERN)) {
                error = "아이디는 영문·숫자 4~20자로 입력하세요.";
            } else if (password.length() < 6 || password.length() > 30) {
                error = "비밀번호는 6~30자로 입력하세요.";
            } else if (!password.equals(password2)) {
                error = "비밀번호 확인이 일치하지 않습니다.";
            } else if (nickname.length() > 15) {
                error = "닉네임은 15자 이하로 입력하세요.";
            } else if (memberDAO.existsUserId(userId)) {
                error = "이미 사용 중인 아이디입니다.";
            }

            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("userId", userId);
                request.setAttribute("nickname", nickname);
                RequestDispatcher dispatcher = request.getRequestDispatcher("join.jsp");
                dispatcher.forward(request, response);
                return;
            }
            memberDAO.insert(userId, password, nickname, "USER");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        // 완료 안내는 로그인 화면에서 한 번만 보여준다 (세션에 잠깐 저장)
        request.getSession().setAttribute("flash", "회원가입이 완료되었습니다. 로그인해 주세요.");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
