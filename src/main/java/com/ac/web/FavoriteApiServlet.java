package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 즐겨찾기 (AJAX, 로그인 필요)
 * POST /api/favorite  recipeId=258                → 저장/해제 전환 (음식 상세)
 * POST /api/favorite  recipeId=258&action=remove  → 해제 (마이페이지)
 * 응답: {"ok":true,"favorited":true}
 */
@WebServlet("/api/favorite")
public class FavoriteApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public FavoriteApiServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 로그인 확인: 안 되어 있으면 401 → 화면의 JS 가 로그인 화면으로 이동
        Member m = (Member) request.getSession().getAttribute(Config.LOGIN);
        if (m == null) {
            JsonUtil.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonUtil.error("로그인이 필요합니다."));
            return;
        }

        String recipeId_ = request.getParameter("recipeId");
        if (recipeId_ == null || !recipeId_.matches("\\d{1,9}")) {
            JsonUtil.write(response, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("잘못된 요청입니다."));
            return;
        }
        int recipeId = Integer.parseInt(recipeId_);

        FavoriteDAO favoriteDAO = new FavoriteDAO();
        try {
            boolean favorited;
            if ("remove".equals(request.getParameter("action"))) {
                favoriteDAO.remove(m.getId(), recipeId);
                favorited = false;
            } else {
                favorited = favoriteDAO.toggle(m.getId(), recipeId);
            }
            JsonUtil.write(response, "{\"ok\":true,\"favorited\":" + favorited + "}");
        } catch (SQLException e) {
            JsonUtil.write(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("저장 중 오류가 발생했습니다."));
        }
    }
}
