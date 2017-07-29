package com.huolihuoshan.backend.biz;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxOutMsg;
import org.nutz.weixin.impl.BasicWxHandler;
import org.nutz.weixin.util.Wxs;

@IocBean(create="init", name="wxHandler")
public class HlhsWxHandler extends BasicWxHandler {
	@Inject
	protected PropertiesProxy conf; // 注入配置信息加载类

	public void init() {
	    // 将读取 weixin.token/weixin.aes/weixin.appid, 他们通常会写在weixin.properties或从数据库读取.
	    configure(conf, "weixin.");
	}

	public WxOutMsg text(WxInMsg msg) {
	    if ("1".equals(msg.getContent())) {
	        return Wxs.respText("广告法说不能自称第一");
	    }
	    else if ("2".equals(msg.getContent())) {
	        return Wxs.respText("就是这么2");
	    }
	    return super.text(msg);
	}
}
