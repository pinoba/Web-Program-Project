package com.ac.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 재료 자동완성 (AJAX): GET /api/ingredients?q=두 → ["두부","두유",...] */
@WebServlet("/api/ingredients")
public class IngredientSuggestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public IngredientSuggestServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
}
