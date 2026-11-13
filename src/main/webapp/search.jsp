<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- JSP 주소로 바로 열면(서블릿을 거치지 않으면) 서블릿으로 보냄 --%>
<c:if test="${empty requestScope['javax.servlet.forward.request_uri']}"><c:redirect url="/search" /></c:if>
<c:set var="pageTitle" value="검색 결과" />
<%@ include file="inc/header.jsp" %>

<div class="search-layout">
  <%-- 1. 필터: 선택 상자에서 고르고 적용 --%>
  <aside class="filter-box">
    <h2 class="filter-title">필터</h2>
    <form id="filterForm" action="${ctx}/search" method="get">
      <input type="hidden" name="q" value="<c:out value='${cond.q}'/>">

      <label class="field">요리종류
        <select name="cat">
          <option value="">전체</option>
          <c:forEach var="cat" items="밥,국&찌개,반찬,일품,후식,기타">
            <option value="<c:out value='${cat}'/>" ${cond.cat == cat ? 'selected' : ''}><c:out value="${fn:replace(cat, '&', '·')}" /></option>
          </c:forEach>
        </select>
      </label>

      <label class="field">칼로리
        <select name="kcal">
          <option value="0">전체</option>
          <c:forEach var="v" items="200,400,600">
            <option value="${v}" ${cond.kcalMax == v ? 'selected' : ''}>${v}kcal 이하</option>
          </c:forEach>
        </select>
      </label>

      <label class="field">단백질
        <select name="protein">
          <option value="0">전체</option>
          <c:forEach var="v" items="10,20,30">
            <option value="${v}" ${cond.proteinMin == v ? 'selected' : ''}>${v}g 이상</option>
          </c:forEach>
        </select>
      </label>

      <label class="field">나트륨
        <select name="sodium">
          <option value="0">전체</option>
          <c:forEach var="v" items="300,600,1000">
            <option value="${v}" ${cond.sodiumMax == v ? 'selected' : ''}>${v}mg 이하</option>
          </c:forEach>
        </select>
      </label>

      <button type="submit" class="btn btn-primary btn-block">적용</button>
      <a class="btn btn-ghost btn-block" href="<c:url value='/search'><c:param name='q' value='${cond.q}'/></c:url>">초기화</a>
    </form>
  </aside>

  <section class="search-main">
    <div class="result-head">
      <h1 class="result-title">
        <c:choose>
          <c:when test="${not empty cond.q}">‘<c:out value="${cond.q}" />’ 검색 결과</c:when>
          <c:when test="${not empty cond.cat}"><c:out value="${fn:replace(cond.cat, '&', '·')}" /> 레시피</c:when>
          <c:otherwise>전체 레시피</c:otherwise>
        </c:choose>
        <span class="count"><fmt:formatNumber value="${result.total}" />건</span>
      </h1>

      <%-- 2. 정렬: 바꾸면 적용한 필터를 그대로 유지한 채 다시 검색 --%>
      <label class="sort-box">정렬
        <select name="sort" form="filterForm" onchange="this.form.submit()">
          <option value="" ${empty cond.sort ? 'selected' : ''}>기본순</option>
          <option value="kcal" ${cond.sort == 'kcal' ? 'selected' : ''}>칼로리 낮은순</option>
          <option value="protein" ${cond.sort == 'protein' ? 'selected' : ''}>단백질 높은순</option>
          <option value="name" ${cond.sort == 'name' ? 'selected' : ''}>이름순</option>
        </select>
      </label>
    </div>

    <c:choose>
      <c:when test="${result.total == 0}">
        <div class="empty">조건에 맞는 레시피가 없습니다. 검색어나 필터를 바꿔 보세요.</div>
      </c:when>
      <c:otherwise>
        <div class="grid grid-3">
          <c:forEach var="r" items="${result.items}">
            <%@ include file="inc/recipeCard.jsp" %>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>

    <%-- 3. 페이징: 12개씩, 검색 조건 유지 --%>
    <c:url var="pageBase" value="/search">
      <c:param name="q" value="${cond.q}" />
      <c:param name="cat" value="${cond.cat}" />
      <c:param name="kcal" value="${cond.kcalMax}" />
      <c:param name="protein" value="${cond.proteinMin}" />
      <c:param name="sodium" value="${cond.sodiumMax}" />
      <c:param name="sort" value="${cond.sort}" />
    </c:url>
    <%@ include file="inc/paging.jsp" %>
  </section>
</div>

<%@ include file="inc/footer.jsp" %>
</body>
</html>
