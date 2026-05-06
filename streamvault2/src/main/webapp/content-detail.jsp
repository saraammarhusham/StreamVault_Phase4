<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>StreamVault – <c:out value="${item.title}"/></title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav>
  <span class="nav-brand">StreamVault</span>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/home">Browse</a>
    <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Sign Out</a>
  </div>
</nav>

<div class="container">

  <!-- ── Hero ─────────────────────────────────────────────────────────── -->
  <div class="detail-hero">
    <div class="detail-thumb">
      <c:choose>
        <c:when test="${item.contentType == 'Movie'}">🎬</c:when>
        <c:when test="${item.contentType == 'Series'}">📺</c:when>
        <c:when test="${item.contentType == 'Documentary'}">🎥</c:when>
        <c:otherwise>🎙</c:otherwise>
      </c:choose>
    </div>

    <div class="detail-info">
      <h1><c:out value="${item.title}"/></h1>

      <div class="detail-meta">
        <span class="badge">${item.contentType}</span>
        <span class="badge">${item.releaseYear}</span>
        <span class="badge"><c:out value="${item.language}"/></span>
        <span class="badge"><c:out value="${item.ageRating}"/></span>
        <span class="badge">⏱ ${item.durationMinutes} min</span>
        <span class="badge">⭐ ${item.avgRating}</span>
      </div>

      <p style="color:var(--muted);font-size:.9rem;margin-bottom:.75rem;">
        Studio: <strong style="color:var(--text)"><c:out value="${item.studioName}"/></strong>
        &nbsp;|&nbsp;
        Genres: <strong style="color:var(--text)"><c:out value="${item.genres}"/></strong>
      </p>

      <p class="synopsis"><c:out value="${item.synopsis}"/></p>

      <!-- ── Play Now ─────────────────────────────────────────────── -->
      <form id="play-form" action="${pageContext.request.contextPath}/content" method="post"
            style="margin-top:1rem;">
        <input type="hidden" name="action"    value="watch">
        <input type="hidden" name="contentId" value="${item.contentId}">
        <input type="hidden" name="episodeId" value="0">
        <input type="hidden" name="progress"  value="0">
        <input type="hidden" name="device"    value="Web" id="deviceField">
        <button type="submit" class="btn-play">▶ Play Now</button>
      </form>

      <c:if test="${param.playing == 'true'}">
        <div class="alert alert-success" style="margin-top:1rem;">
          ✅ Watch session recorded! Enjoy watching.
        </div>
      </c:if>
    </div>
  </div>

  <!-- ── Episodes (Series only) ──────────────────────────────────────── -->
  <c:if test="${item.contentType == 'Series' && not empty episodes}">
    <h2 style="margin-bottom:1rem;">Episodes</h2>
    <div class="episode-list">
      <c:forEach var="ep" items="${episodes}">
        <div class="episode-item">
          <div>
            <div class="ep-title">
              S<c:out value="${ep.seasonNumber}"/>E<c:out value="${ep.episodeNumber}"/>
              – <c:out value="${ep.title}"/>
            </div>
            <div class="ep-meta"><c:out value="${ep.synopsis}"/></div>
          </div>
          <div style="display:flex;align-items:center;gap:1rem;">
            <span class="badge">${ep.durationMinutes} min</span>
            <form action="${pageContext.request.contextPath}/content" method="post" style="margin:0;">
              <input type="hidden" name="action"    value="watch">
              <input type="hidden" name="contentId" value="${item.contentId}">
              <input type="hidden" name="episodeId" value="${ep.episodeId}">
              <input type="hidden" name="progress"  value="0">
              <input type="hidden" name="device"    value="Web">
              <button type="submit" class="btn btn-secondary"
                      style="padding:.3rem .8rem;font-size:.82rem;width:auto;">
                ▶ Play
              </button>
            </form>
          </div>
        </div>
      </c:forEach>
    </div>
  </c:if>

  <!-- ── Reviews ──────────────────────────────────────────────────────── -->
  <div class="reviews-section" style="margin-top:2.5rem;">
    <h2>Reviews &amp; Ratings</h2>
    <c:choose>
      <c:when test="${empty reviews}">
        <p style="color:var(--muted);margin-top:.75rem;">No reviews yet for this title.</p>
      </c:when>
      <c:otherwise>
        <c:forEach var="rev" items="${reviews}">
          <%-- rev[0]=name  rev[1]=rating  rev[2]=text  rev[3]=date --%>
          <div class="review-card">
            <div class="review-author"><c:out value="${rev[0]}"/></div>
            <div class="review-stars">
              ⭐ <c:out value="${rev[1]}"/> / 5
              <span style="color:var(--muted);font-size:.82rem;"> · <c:out value="${rev[3]}"/></span>
            </div>
            <div class="review-text"><c:out value="${rev[2]}"/></div>
          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- ══════════════════════════════════════════════════════════════════
       FIX 2 — RATE THIS TITLE
       Only shown to normal subscribers (not admins).
       - Shows success banner when ?rated=true is in the URL.
       - Shows error banner if the servlet sets ratingError.
       - Pre-fills the form if the user has already rated this title
         (userRating[0] = existing rating, userRating[1] = review text).
       - Button label changes to "Update Rating" on revisit.
       ══════════════════════════════════════════════════════════════════ -->
  <c:if test="${sessionScope.role != 'admin'}">
    <div class="reviews-section" style="margin-top:2rem;">
      <h2>Rate This Title</h2>

        <%-- Error banner (set by ContentDetailServlet on invalid input) --%>
      <c:if test="${not empty ratingError}">
        <div class="alert alert-error" style="margin-bottom:1rem;">
          ❌ <c:out value="${ratingError}"/>
        </div>
      </c:if>

        <%-- Success banner (redirect appends ?rated=true on save) --%>
      <c:if test="${param.rated == 'true'}">
        <div class="alert alert-success" style="margin-bottom:1rem;">
          ✅ Your rating was saved successfully!
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/content" method="post"
            style="max-width:480px;">
        <input type="hidden" name="action"    value="rate"/>
        <input type="hidden" name="contentId" value="${item.contentId}"/>

        <label style="display:block;margin-bottom:.5rem;font-weight:600;">
          Your Rating (0.5 – 5.0)
        </label>
        <input type="number" name="ratingValue"
               min="0.5" max="5.0" step="0.5" required
               style="width:120px;padding:.4rem .6rem;margin-bottom:1rem;
                      background:var(--surface);color:var(--text);
                      border:1px solid var(--border);border-radius:6px;"
               value="${not empty userRating ? userRating[0] : ''}"/>

        <label style="display:block;margin-bottom:.5rem;font-weight:600;">
          Review <span style="color:var(--muted);font-weight:400;">(optional)</span>
        </label>
        <textarea name="reviewText" rows="3"
                  style="width:100%;padding:.5rem .6rem;
                         background:var(--surface);color:var(--text);
                         border:1px solid var(--border);border-radius:6px;
                         resize:vertical;"><c:out value="${not empty userRating ? userRating[1] : ''}"/></textarea>

        <button type="submit" class="btn btn-secondary"
                style="margin-top:.75rem;padding:.5rem 1.2rem;">
          <c:choose>
            <c:when test="${not empty userRating}">✏ Update Rating</c:when>
            <c:otherwise>⭐ Submit Rating</c:otherwise>
          </c:choose>
        </button>
      </form>
    </div>
  </c:if>
  <!-- ── End FIX 2 ─────────────────────────────────────────────────── -->

</div>

<script src="${pageContext.request.contextPath}/js/main.js"></script>
<script>
  // Set device type on page load
  document.getElementById('deviceField').value =
          /Mobile|Android|iPhone/i.test(navigator.userAgent) ? 'Mobile' : 'Web';
</script>
</body>
</html>
