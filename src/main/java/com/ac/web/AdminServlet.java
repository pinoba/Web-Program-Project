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
 * 관리자 (관리자 계정만 접근, 아니면 메인으로)
 *  GET  /admin         관리자 화면 → admin.jsp
 *  POST /admin/load    API 적재 시작 (AJAX)
 *  GET  /admin/status  적재 진행 상황 (AJAX, 0.5초마다 조회)
 */
@WebServlet(urlPatterns = { "/admin", "/admin/load", "/admin/status" })
public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public AdminServlet() {
        super();
    }

    /** 관리자인지 확인 */
    private boolean isAdmin(HttpServletRequest request) {
        Member m = (Member) request.getSession().getAttribute(Config.LOGIN);
        return m != null && m.isAdmin();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/main");
            return;
        }

        RecipeDAO recipeDAO = new RecipeDAO();
        int recipeCount;
        try {
            recipeCount = recipeDAO.count();
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if ("/admin/status".equals(request.getServletPath())) {
            JsonUtil.write(response, RecipeLoader.statusJson(recipeCount));
            return;
        }

        request.setAttribute("recipeCount", recipeCount);
        request.setAttribute("running", RecipeLoader.isRunning());
        request.setAttribute("message", RecipeLoader.getMessage());
        request.setAttribute("total", RecipeLoader.getTotal());
        request.setAttribute("inserted", RecipeLoader.getInserted());
        request.setAttribute("updated", RecipeLoader.getUpdated());
        request.setAttribute("failed", RecipeLoader.getFailed());
        request.setAttribute("mismatch", RecipeLoader.getMismatch());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            JsonUtil.write(response, HttpServletResponse.SC_FORBIDDEN, JsonUtil.error("관리자만 사용할 수 있습니다."));
            return;
        }
        if (!"/admin/load".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        boolean started = RecipeLoader.start();
        String message = started ? "적재를 시작했습니다." : "이미 적재가 진행 중입니다.";
        JsonUtil.write(response, "{\"ok\":" + started + ",\"message\":" + JsonUtil.quote(message) + "}");
    }
}
