package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 재료로 검색
 *  GET /ingredient?ing=두부&ing=애호박  재료로 검색 → ingredient.jsp
 *  GET /api/ingredients?q=두           재료 자동완성 (AJAX) → ["두부","두유",...]
 */
@WebServlet(urlPatterns = { "/ingredient", "/api/ingredients" })
public class IngredientServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_INGREDIENTS = 10;

    public IngredientServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("/api/ingredients".equals(request.getServletPath())) {
            suggest(request, response);
            return;
        }

        // 같은 이름(ing)으로 여러 개 전달된 파라미터는 getParameterValues 로 받는다
        List<String> ingredients = new ArrayList<>();
        String[] params = request.getParameterValues("ing");
        if (params != null) {
            for (String p : params) {
                if (p != null && !p.isBlank() && !ingredients.contains(p.trim())
                        && ingredients.size() < MAX_INGREDIENTS) {
                    ingredients.add(p.trim());
                }
            }
        }
        request.setAttribute("ingredients", ingredients);

        if (!ingredients.isEmpty()) {
            int page = 1;
            String page_ = request.getParameter("page");
            if (page_ != null && page_.matches("\\d{1,5}")) {
                page = Math.max(1, Integer.parseInt(page_));
            }
            RecipeDAO recipeDAO = new RecipeDAO();
            try {
                request.setAttribute("result", recipeDAO.searchByIngredients(ingredients, page));
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("ingredient.jsp");
        dispatcher.forward(request, response);
    }

    /** 재료 자동완성 (AJAX): 입력 글자가 들어간 재료 이름 10개를 JSON 배열로 */
    private void suggest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = request.getParameter("q");
        if (q == null || q.isBlank()) {
            JsonUtil.write(response, "[]");
            return;
        }
        IngredientDAO ingredientDAO = new IngredientDAO();
        try {
            JsonUtil.write(response, JsonUtil.array(ingredientDAO.suggest(q.trim(), 10)));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
