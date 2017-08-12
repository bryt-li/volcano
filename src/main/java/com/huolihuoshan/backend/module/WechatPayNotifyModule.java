package com.huolihuoshan.backend.module;

import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import com.huolihuoshan.backend.biz.OrderManager;

@IocBean
@At("/wechat/pay/notify")
@Ok("json")
@Fail("http:500")
public class WechatPayNotifyModule extends BaseModule{
	
	@Inject
	private OrderManager orderManager;

	//注意：这个支付通知由微信支付平台调用
	//
	@At
	@POST
	public String recv(HttpServletRequest request, HttpServletResponse response) throws Exception{
		InputStreamReader isr = new InputStreamReader(request.getInputStream(), Charset.forName("UTF-8"));
		String content = Streams.readAndClose(isr);
		LOG.debug(content);
		
		NutMap map = Xmls.xmlToMap(content);
		
		NutMap params = NutMap.NEW();
		if(this.orderManager.processWechatPaymentNotification(map)){
			params.put("return_code", "SUCCESS");
			params.put("return_msg", "OK");
		}else{
			params.put("return_code", "FAIL");
			params.put("return_msg", "ERR");
		}
		String xml = Xmls.mapToXml(params);
		return xml;
	}
}
