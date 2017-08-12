package com.huolihuoshan.backend.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.weixin.spi.WxApi2;

@IocBean
@At("/wechat/jsapi")
@Ok("json")
@Fail("http:500")
public class WechatJsapiModule {

	@Inject
	private WxApi2 wxApi2;

	@At
	@POST
	public Object cfg(@Param("url") final String url, 
			@Param(value="jsApiList") final String[] jsApiList){
		return wxApi2.genJsSDKConfig(url, jsApiList );
	}
	
}
