<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- 공통 헤더: 각 화면에서 pageTitle 을 정한 뒤 include --%>
<%-- 숫자 표시 형식을 한국 기준으로 고정 (브라우저 언어 설정과 상관없이 1,234.5 형식) --%>
<fmt:setLocale value="ko_KR" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="me" value="${sessionScope.loginMember}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${pageTitle}" /> | 밥심</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css">
<link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body data-ctx="${ctx}">
<header class="topbar">
  <div class="wrap topbar-inner">
    <a class="logo" href="${ctx}/main">밥<span>심</span></a>
    <form class="top-search" action="${ctx}/search" method="get">
      <input type="search" name="q" placeholder="레시피 검색 (메뉴명·해시태그)" value="<c:out value='${param.q}'/>" aria-label="레시피 검색">
      <button type="submit" aria-label="검색">검색</button>
    </form>
    <nav class="topnav">
      <a href="${ctx}/ingredient">재료로 검색</a>
      <c:choose>
        <c:when test="${not empty me}">
          <c:if test="${me.admin}"><a href="${ctx}/admin" class="nav-admin">관리자</a></c:if>
          <a href="${ctx}/mypage" class="nav-me"><c:out value="${me.nickname}" /> 님</a>
        </c:when>
        <c:otherwise>
          <a href="${ctx}/login" class="nav-login">로그인</a>
        </c:otherwise>
      </c:choose>
    </nav>
  </div>
</header>
<main class="wrap page">
