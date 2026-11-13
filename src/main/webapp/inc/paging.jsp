<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- 페이지 번호. 사용 전 pageBase(마지막에 page= 를 붙일 수 있는 URL)와 result 를 준비 --%>
<c:if test="${result.totalPages > 1}">
  <nav class="paging" aria-label="페이지">
    <c:choose>
      <c:when test="${result.page > 1}"><a href="${pageBase}&page=${result.page - 1}" aria-label="이전">‹</a></c:when>
      <c:otherwise><span class="disabled">‹</span></c:otherwise>
    </c:choose>
    <c:if test="${result.startPage > 1}">
      <a href="${pageBase}&page=1">1</a>
      <c:if test="${result.startPage > 2}"><span class="dots">…</span></c:if>
    </c:if>
    <c:forEach var="i" begin="${result.startPage}" end="${result.endPage}">
      <c:choose>
        <c:when test="${i == result.page}"><span class="current">${i}</span></c:when>
        <c:otherwise><a href="${pageBase}&page=${i}">${i}</a></c:otherwise>
      </c:choose>
    </c:forEach>
    <c:if test="${result.endPage < result.totalPages}">
      <c:if test="${result.endPage < result.totalPages - 1}"><span class="dots">…</span></c:if>
      <a href="${pageBase}&page=${result.totalPages}">${result.totalPages}</a>
    </c:if>
    <c:choose>
      <c:when test="${result.page < result.totalPages}"><a href="${pageBase}&page=${result.page + 1}" aria-label="다음">›</a></c:when>
      <c:otherwise><span class="disabled">›</span></c:otherwise>
    </c:choose>
  </nav>
</c:if>
