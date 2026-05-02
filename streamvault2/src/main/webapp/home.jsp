<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>StreamVault – Browse</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav>
  <span class="nav-brand">StreamVault</span>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/home">Browse</a>
    <a href="${pageContext.request.contextPath}/dashboard">My Dashboard</a>
    <c:if test="${sessionScope.role == 'admin'}">
      <a href="${pageContext.request.contextPath}/admin-analytics">Analytics</a>
    </c:if>
    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Sign Out</a>
  </div>
</nav>

<div class="container">
  <div class="page-header">
    <h1>Browse Content</h1>
    <p>Welcome back, <strong><c:out value="${sessionScope.user.fullName}"/></strong></p>
  </div>

  <!-- ── Filter Bar ──────────────────────────────────────────────────── -->
  <form id="filter-form" method="get" action="${pageContext.request.contextPath}/home">
    <div class="filter-bar">

      <input type="text" name="search" placeholder="Search titles…"
             value="<c:out value='${search}'/>">

      <select name="type">
        <option value="">All Types</option>
        <option value="Movie"         <c:if test="${type == 'Movie'}">selected</c:if>>Movie</option>
        <option value="Series"        <c:if test="${type == 'Series'}">selected</c:if>>Series</option>
        <option value="Documentary"   <c:if test="${type == 'Documentary'}">selected</c:if>>Documentary</option>
        <option value="Podcast"       <c:if test="${type == 'Podcast'}">selected</c:if>>Podcast</option>
      </select>

      <select name="genre">
        <option value="">All Genres</option>
        <c:forEach var="g" items="${genres}">
          <option value="${g}" <c:if test="${genre == g}">selected</c:if>>
            <c:out value="${g}"/>
          </option>
        </c:forEach>
      </select>

      <select name="language">
        <option value="">All Languages</option>
        <c:forEach var="lang" items="${languages}">
          <option value="${lang}" <c:if test="${language == lang}">selected</c:if>>
            <c:out value="${lang}"/>
          </option>
        </c:forEach>
      </select>

      <select name="sort">
        <option value="title"  <c:if test="${empty sort || sort == 'title'}">selected</c:if>>A – Z</option>
        <option value="rating" <c:if test="${sort == 'rating'}">selected</c:if>>Top Rated</option>
      </select>

      <button type="submit">Search</button>
      <a href="${pageContext.request.contextPath}/home" class="reset">Reset</a>
    </div>
  </form>

  <!-- ── Content Grid ────────────────────────────────────────────────── -->
  <c:choose>
    <c:when test="${empty items}">
      <div style="text-align:center;padding:4rem;color:var(--muted);">
        <div style="font-size:3rem;margin-bottom:1rem;">🎬</div>
        <p>No content found. Try a different search or filter.</p>
        <a href="${pageContext.request.contextPath}/home"
           class="btn btn-secondary" style="display:inline-block;margin-top:1rem;width:auto;padding:.5rem 1.5rem;">
          Clear Filters
        </a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="content-grid">
        <c:forEach var="item" items="${items}">
          <div class="content-card" data-id="${item.contentId}"
               onclick="location.href='${pageContext.request.contextPath}/content?id=${item.contentId}'"
               style="cursor:pointer;">
            <div class="card-thumb">
              <img src="${pageContext.request.contextPath}/${item.posterUrl}"
                   alt="${item.title}"
                   style="width:100%; height:100%; object-fit:cover; display:block;">
            </div>
            <div class="card-body">
              <div class="card-title"><c:out value="${item.title}"/></div>
              <div class="card-meta">
                ${item.releaseYear} · <c:out value="${item.language}"/> · ${item.contentType}
              </div>
              <div class="card-rating">⭐ ${item.avgRating} · <c:out value="${item.studioName}"/></div>
              <div class="card-genre"><c:out value="${item.genres}"/></div>
            </div>
          </div>
        </c:forEach>
      </div>

      <!-- ── Pagination ─────────────────────────────────────────────── -->
      <div class="pagination">
        <c:if test="${page > 1}">
          <a href="?page=${page - 1}&search=${search}&type=${type}&genre=${genre}&language=${language}&sort=${sort}">
            ‹ Prev
          </a>
        </c:if>
        <span class="active">Page ${page}</span>
        <c:if test="${items.size() >= pageSize}">
          <a href="?page=${page + 1}&search=${search}&type=${type}&genre=${genre}&language=${language}&sort=${sort}">
            Next ›
          </a>
        </c:if>
      </div>
    </c:otherwise>
  </c:choose>

</div>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
