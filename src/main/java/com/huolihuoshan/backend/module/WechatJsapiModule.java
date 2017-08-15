package com.huolihuoshan.backend.module;

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

	@At
	@POST
	public Object cfg(	@Param("url") final String url, 
						@Param("jsApiList") final String jsApiList){
		LOG.debugf("url='%s', jsApiList='%s'", url, jsApiList);

		//微信支付说URL不包含#及其后面部分
		String path = url.split("#")[0];
		NutMap args = wxApi2.genJsSDKConfig(path, jsApiList.split(",") );
		if(null == args)
			return err("generate JsSDK Config fail");
		else
			return ok(args);
	}
}
