<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>StreamVault – Login</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-wrap">
  <div class="auth-box">
    <span class="logo">StreamVault</span>
    <h1>Sign In</h1>
    <p style="text-align:center;color:var(--muted);margin-bottom:1.5rem;font-size:.9rem;">
      Stream anything. Anywhere.
    </p>

    <c:if test="${not empty error}">
      <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty success}">
      <div class="alert alert-success"><c:out value="${success}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="post">
      <div class="form-group">
        <label for="email">Email Address</label>
        <input class="form-control" type="email" id="email" name="email"
               placeholder="you@example.com" required autocomplete="email">
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input class="form-control" type="password" id="password" name="password"
               placeholder="••••••••" required>
      </div>
      <button type="submit" class="btn btn-primary" style="margin-top:.5rem;">
        Sign In
      </button>
    </form>

    <p style="text-align:center;margin-top:1.5rem;font-size:.9rem;color:var(--muted);">
      New to StreamVault?
      <a href="${pageContext.request.contextPath}/register">Create account</a>
    </p>
  </div>
</div>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
