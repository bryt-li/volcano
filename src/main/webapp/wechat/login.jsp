<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>完成微信登录</title>
	<script>
	function isWeiXin(){
	    var ua = window.navigator.userAgent.toLowerCase();
	    if(ua.match(/MicroMessenger/i) == 'micromessenger'){
	        return true;
	    }else{
	        return false;
	    }
	}
	
	var msg = '<%=request.getAttribute("obj")%>';
	console.log(msg);	
	if(isWeiXin()){
		if(msg=='err')
			window.parent.window.onSignInFailed();
		else
			window.parent.window.onSignedIn(msg);
	}else{
		window.parent.postMessage(
			{
				sender: 'hlhs-backend',
				message: msg
			},
			'*'
		);
	}
	</script>
</head>
<body>
</body>
</html>
