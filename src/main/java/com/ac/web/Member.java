package com.ac.web;

import java.io.Serializable;

/** 회원 자바빈 (세션에 저장, 비밀번호는 담지 않음) */
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String userId;
    private String nickname;
    private String role;

    public Member() {}

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
