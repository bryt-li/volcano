package com.huolihuoshan.backend.module;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.weixin.spi.WxHandler;
import org.nutz.weixin.util.Wxs;

@IocBean
@At("/wechat/msg")
@Ok("json")
@Fail("http:500")
public class WechatMsgModule {
	
	@Inject
	protected WxHandler wxHandler;
	
	@At
	public View in(HttpServletRequest req) throws Exception{
		// 最后面的default,可以不写,只是个标识符.
        return Wxs.handle(wxHandler, req, "default");
	}
}
