<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/join" /></c:if>
<c:set var="pageTitle" value="회원가입" />
<%@ include file="inc/header.jsp" %>

<section class="auth-box">
  <h1>회원가입</h1>

  <c:if test="${not empty error}">
    <p class="form-msg err"><c:out value="${error}" /></p>
  </c:if>

  <form id="joinForm" action="${ctx}/join" method="post" class="auth-form" novalidate>
    <label class="field">아이디
      <span class="inline-row">
        <input type="text" id="userId" name="userId" value="<c:out value='${userId}'/>"
               placeholder="영문·숫자 4~20자" maxlength="20" autocomplete="username">
        <button type="button" id="idCheckBtn" class="btn btn-outline">중복확인</button>
      </span>
    </label>
    <p id="idMsg" class="form-msg" aria-live="polite"></p>

    <label class="field">비밀번호
      <input type="password" id="password" name="password" placeholder="6~30자" maxlength="30" autocomplete="new-password">
    </label>
    <label class="field">비밀번호 확인
      <input type="password" id="password2" name="password2" maxlength="30" autocomplete="new-password">
    </label>
    <label class="field">닉네임
      <input type="text" id="nickname" name="nickname" value="<c:out value='${nickname}'/>" maxlength="15">
    </label>
    <p id="joinMsg" class="form-msg err" aria-live="polite"></p>

    <button type="submit" class="btn btn-primary btn-block">가입하기</button>
  </form>

  <p class="auth-foot"><a href="${ctx}/login">← 로그인 화면으로</a></p>
</section>

<%@ include file="inc/footer.jsp" %>
<script src="${ctx}/js/join.js"></script>
</body>
</html>
