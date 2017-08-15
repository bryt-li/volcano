package com.huolihuoshan.backend.module;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
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
	@GET
	public Object cfg(){
		String jsApiList = "chooseWXPay,onMenuShareTimeline,onMenuShareAppMessage";
		
		//因为使用了SPA的HashHistory，而微信支付说URL不包含#及其后面部分
		//所以直接把hlhs_frontend_url传递进去做为URL签名即可
		NutMap args = wxApi2.genJsSDKConfig(hlhs_frontend_url+"/?hlhs", jsApiList.split(",") );
				
		if(null == args)
			return err("generate JsSDK Config fail");
		else
			return ok(args);
	}
}
