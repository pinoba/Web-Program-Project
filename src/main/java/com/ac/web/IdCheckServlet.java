package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 아이디 중복 확인 (AJAX): GET /api/idcheck?userId=abc → {"available":true,"message":"..."} */
@WebServlet("/api/idcheck")
public class IdCheckServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public IdCheckServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        userId = (userId == null) ? "" : userId.trim();

        boolean available;
        String message;
        if (!userId.matches(JoinServlet.ID_PATTERN)) {
            available = false;
            message = "아이디는 영문·숫자 4~20자로 입력하세요.";
        } else {
            try {
                available = !new MemberDAO().existsUserId(userId);
            } catch (SQLException e) {
                throw new ServletException(e);
            }
            message = available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";
        }
        JsonUtil.write(response, "{\"available\":" + available + ",\"message\":" + JsonUtil.quote(message) + "}");
    }
}
