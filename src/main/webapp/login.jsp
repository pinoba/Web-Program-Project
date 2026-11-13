<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/login" /></c:if>
<c:set var="pageTitle" value="로그인" />
<%@ include file="inc/header.jsp" %>

<section class="auth-box">
  <h1>로그인</h1>

  <c:if test="${not empty sessionScope.flash}">
    <p class="form-msg ok"><c:out value="${sessionScope.flash}" /></p>
    <c:remove var="flash" scope="session" />
  </c:if>
  <c:if test="${not empty error}">
    <p class="form-msg err"><c:out value="${error}" /></p>
  </c:if>

  <form action="${ctx}/login" method="post" class="auth-form">
    <input type="hidden" name="redirect" value="<c:out value='${redirect}'/>">
    <label class="field">아이디
      <input type="text" name="userId" value="<c:out value='${userId}'/>" required autofocus autocomplete="username">
    </label>
    <label class="field">비밀번호
      <input type="password" name="password" required autocomplete="current-password">
    </label>
    <button type="submit" class="btn btn-primary btn-block">로그인</button>
  </form>

  <p class="auth-foot">아직 회원이 아니신가요? <a href="${ctx}/join">회원가입 →</a></p>
</section>

<%@ include file="inc/footer.jsp" %>
</body>
</html>
