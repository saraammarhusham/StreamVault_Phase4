<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>StreamVault – Error</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div style="display:flex;align-items:center;justify-content:center;min-height:100vh;
            flex-direction:column;gap:1rem;text-align:center;">
  <div style="font-size:4rem;">⚠️</div>
  <h1 style="font-size:2rem;">Something went wrong</h1>
  <p style="color:var(--muted);">We could not process your request.</p>
  <a href="${pageContext.request.contextPath}/home"
     class="btn btn-primary" style="display:inline-block;width:auto;padding:.6rem 2rem;">
    Go Home
  </a>
</div>
</body>
</html>
