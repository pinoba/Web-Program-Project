package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** 음식 상세: 영양 막대, 재료, 조리 순서, 즐겨찾기 상태, 최근 본 레시피 기록 → recipe.jsp */
@WebServlet("/recipe")
public class RecipeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int RECENT_MAX = 5;

    public RecipeServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = -1;
        String id_ = request.getParameter("id");
        if (id_ != null && id_.matches("\\d{1,9}")) {
            id = Integer.parseInt(id_);
        }

        RecipeDAO recipeDAO = new RecipeDAO();
        try {
            Recipe recipe = recipeDAO.findById(id);
            if (recipe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("recipe", recipe);

            // 로그인한 경우 즐겨찾기 버튼 상태
            HttpSession session = request.getSession();
            Member m = (Member) session.getAttribute(Config.LOGIN);
            if (m != null) {
                request.setAttribute("favorited", new FavoriteDAO().exists(m.getId(), id));
            }
            addRecent(session, recipe);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("recipe.jsp");
        dispatcher.forward(request, response);
    }

    /** 세션에 최근 본 레시피 기록: 같은 레시피는 맨 앞으로, 최대 5개 */
    @SuppressWarnings("unchecked")
    private void addRecent(HttpSession session, Recipe recipe) {
        LinkedList<Recipe> recent = (LinkedList<Recipe>) session.getAttribute("recentRecipes");
        if (recent == null) recent = new LinkedList<>();

        Recipe summary = new Recipe(); // 세션에는 목록에 필요한 정보만 저장
        summary.setId(recipe.getId());
        summary.setName(recipe.getName());
        summary.setCategory(recipe.getCategory());
        summary.setKcal(recipe.getKcal());

        recent.removeIf(r -> r.getId() == recipe.getId());
        recent.addFirst(summary);
        while (recent.size() > RECENT_MAX) recent.removeLast();
        session.setAttribute("recentRecipes", recent);
    }
}
