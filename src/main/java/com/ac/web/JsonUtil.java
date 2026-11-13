package com.ac.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * AJAX 응답(JSON)을 직접 문자열로 만들 때 쓰는 도구.
 * 예) {"ok":true,"message":"저장했습니다."}
 */
public class JsonUtil {

    /** 문자열을 JSON 문자열 값으로 바꾼다 ("" 로 감싸고 특수문자 처리) */
    public static String quote(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '<':  sb.append("\\u003c"); break; // HTML 안에 넣어도 안전하게
                default:
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
            }
        }
        return sb.append('"').toString();
    }

    /** 문자열 목록 → ["두부","두유"] */
    public static String array(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(quote(list.get(i)));
        }
        return sb.append(']').toString();
    }

    /** {"ok":false,"message":"..."} */
    public static String error(String message) {
        return "{\"ok\":false,\"message\":" + quote(message) + "}";
    }

    /** JSON 을 응답으로 보낸다 */
    public static void write(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        PrintWriter out = response.getWriter();
        out.print(json);
    }

    public static void write(HttpServletResponse response, String json) throws IOException {
        write(response, HttpServletResponse.SC_OK, json);
    }
}
