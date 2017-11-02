package com.huolihuoshan.volcano.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
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
public class WechatJsapiModule extends BaseModule{

	@Inject
	private WxApi2 wxApi2;

	@Inject("java:$conf.get('hlhs_frontend_url')")
	private String hlhs_frontend_url;

	@At
	@POST
	public Object cfg(@Param("url") final String url){
		//微信支付说签名URL不包含#及其后面部分
		String path = url.split("#")[0];
		LOG.debugf("url='%s'", path);
		
		String jsApiList = "chooseWXPay,onMenuShareTimeline,onMenuShareAppMessage";
		NutMap args = wxApi2.genJsSDKConfig(path, jsApiList.split(",") );
				
		if(null == args)
			return err("generate JsSDK Config fail");
		else
			return ok(args);
	}
}
