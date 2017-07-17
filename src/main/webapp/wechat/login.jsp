<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
	import="org.nutz.weixin.spi.WxResp" %>
<%
WxResp resp = (WxResp)request.getAttribute("obj");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>微信登录</title>
</head>
<body>
<ul>
<% if(resp.ok()){ %>
		<li>access_token:<%= resp.get("access_token") %></li>
		<li>expires_in:<%= resp.get("expires_in") %></li>
		<li>refresh_token:<%= resp.get("refresh_token") %></li>
		<li>openid:<%= resp.get("openid") %></li>
		<li>scope:<%= resp.get("scope") %></li>
		<li>state<%= resp.get("state") %></li>
<% }else{ %>
		<li>errcode:<%= resp.errcode() %></li>
		<li>errmsg:<%= resp.errmsg() %></li>
		<li>state<%= resp.get("state") %></li>
<% } %>
</ul>
</body>
</html>
