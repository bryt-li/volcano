package com.huolihuoshan.backend.module;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.weixin.util.WxPaySign;

@IocBean
@At("/wechat/cfg")
@Ok("json")
@Fail("http:500")
public class WechatCfgModule {

	/*
	@At
	@POST
	public Object jsapi(@Param("url") String url, @Param("jsApiList") String jsApiList){
        NutMap params = NutMap.NEW();
        params.put("appId", appId);
        params.put("timeStamp", String.valueOf((int) (System.currentTimeMillis() / 1000)));
        params.put("nonceStr", R.UU32());
        params.put("jsapi_ticket", ticket);
        params.put("url", url);
        
        String sign = WxPaySign.createSign(key, params);
        params.put("jsApiList", jsApiList);
        params.put("signature", sign);
        return params;
	}*/
}
