package com.huolihuoshan.backend.module;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
@At("/wechat_login_mock")
@Ok("json")
@Fail("http:500")
public class WechatLoginMockModule {
	@At
	public void authorize(@Param("appid") String appid,
			@Param("redirect_uri") String redirect_uri,
			@Param("state") String state,
			HttpServletResponse response) throws IOException{
		
		response.sendRedirect(String.format("%s?code=xxxx&state=%s", redirect_uri,state));
	}
	
	@At
	public Object access_token(){
		return new NutMap().
				setv("openid", "007").
				setv("access_token", "access token 12345");
	}
	
	@At
	public Object userinfo(){
		return new NutMap().
				setv("openid", "007").
				setv("nickname", "HLHS").
				setv("sex", 1).
				setv("country", "china").
				setv("province", "hunan").
				setv("city", "changsha").
				setv("headimgurl", "http://0.0.0.0:9090/hlhs-backend/volcano.png");				
	}
	
}
