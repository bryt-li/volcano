<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>wechat login return</title>
	<script>
	window.parent.postMessage(
		{
			sender: 'hlhs-backend',
			message: '<%=request.getAttribute("obj")%>'
		},
		'*'
	);
	</script>
</head>
<body>
</body>
</html>
