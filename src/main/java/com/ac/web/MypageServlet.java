package com.ac.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 마이페이지 (로그인 필요): 즐겨찾기(DB), 최근 본 레시피(세션 → JSP 에서 sessionScope 로 읽음) → mypage.jsp */
@WebServlet("/mypage")
public class MypageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MypageServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Member m = (Member) request.getSession().getAttribute(Config.LOGIN);
        if (m == null) {
            String back = URLEncoder.encode(request.getContextPath() + "/mypage", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login?redirect=" + back);
            return;
        }

        try {
            request.setAttribute("favorites", new FavoriteDAO().findByMember(m.getId()));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("mypage.jsp");
        dispatcher.forward(request, response);
    }
}
