package com.ac.web;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** 밥심 설정값 (인증키, DB 위치, 관리자 계정) */
public class Config {

    // 식약처 조리식품 레시피 DB(COOKRCP01) 인증키
    // src/main/resources/api.properties 의 apiKey 값을 읽는다 (GitHub 에 올리지 않는 파일)
    public static final String API_KEY = loadApiKey();
    public static final String API_BASE = "http://openapi.foodsafetykorea.go.kr/api";
    public static final int API_PAGE_SIZE = 1000; // 한 번에 받을 건수 (API 최대 1000)

    // H2 내장형 DB 파일 위치 (~ 는 사용자 홈 폴더, 예: C:\Users\이름\bapsim)
    // DB_CLOSE_DELAY=-1: 서버가 켜져 있는 동안 DB 를 열어 둠.
    //   (없으면 요청마다 DB 를 다시 열어서 AUTO_SERVER 준비에 매번 약 5초가 걸림)
    public static final String DB_URL = "jdbc:h2:~/bapsim/bapsimdb;MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
    public static final String DB_USER = "sa";
    public static final String DB_PASSWORD = "";

    // 서버 첫 실행 시 자동으로 만들어지는 관리자 계정
    public static final String ADMIN_ID = "admin";
    public static final String ADMIN_PASSWORD = "admin1234";

    // 세션에 로그인 회원을 저장할 때 쓰는 이름
    public static final String LOGIN = "loginMember";

    /** 클래스패스의 api.properties 에서 apiKey 를 읽는다. 없으면 "" */
    private static String loadApiKey() {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("api.properties")) {
            if (in == null) return "";
            Properties p = new Properties();
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return p.getProperty("apiKey", "").trim();
        } catch (Exception e) {
            return "";
        }
    }
}
