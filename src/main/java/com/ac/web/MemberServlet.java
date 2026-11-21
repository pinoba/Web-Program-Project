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
 * 회원 (로그인 · 회원가입 · 아이디 중복 확인 · 로그아웃)
 *  GET  /login        로그인 화면 → login.jsp
 *  POST /login        로그인 → 성공하면 원래 보던 화면으로, 실패하면 오류 문구와 함께 login.jsp
 *  GET  /join         회원가입 화면 → join.jsp
 *  POST /join         회원가입 (화면의 JS 검사와 별개로 서버에서도 한 번 더 검사) → 로그인 화면으로
 *  GET  /api/idcheck  아이디 중복 확인 (AJAX) → {"available":true,"message":"..."}
 *  POST /logout       로그아웃: 세션 종료(최근 본 레시피도 함께 초기화) 후 메인으로
 */
@WebServlet(urlPatterns = { "/login", "/join", "/api/idcheck", "/logout" })
public class MemberServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final String ID_PATTERN = "^[A-Za-z0-9]{4,20}$";

    public MemberServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/login":
                loginForm(request, response);
                break;
            case "/join":
                RequestDispatcher dispatcher = request.getRequestDispatcher("join.jsp");
                dispatcher.forward(request, response);
                break;
            case "/api/idcheck":
                idCheck(request, response);
                break;
            default: // 로그아웃은 POST 로만
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        switch (request.getServletPath()) {
            case "/login":
                login(request, response);
                break;
            case "/join":
                join(request, response);
                break;
            case "/logout":
                logout(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    // ---------------- 로그인 ----------------

    private void loginForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    // ---------------- 회원가입 ----------------

    private void join(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    /** 아이디 중복 확인 (AJAX) */
    private void idCheck(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = trim(request.getParameter("userId"));

        boolean available;
        String message;
        if (!userId.matches(ID_PATTERN)) {
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

    // ---------------- 로그아웃 ----------------

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/main");
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
