<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>微信登录返回</title>
	<script>
	var msg = '<%=request.getAttribute("obj")%>';
	window.parent.postMessage(
		{
			sender: 'hlhs-backend',
			message: msg
		},
		'*'
	);
	console.log('send message to parent window.');
	console.log(msg);	
	</script>
</head>
<body>
	<p><%=request.getAttribute("obj")%></p>
</body>
</html>
