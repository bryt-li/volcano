package com.huolihuoshan.backend.module;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import com.huolihuoshan.backend.bean.Order;
import com.huolihuoshan.backend.bean.User;
import com.huolihuoshan.backend.biz.OrderManager;

@IocBean
@At("/order")
@Ok("json")
@Fail("http:500")
public class OrderModule extends BaseModule {
	
	@Inject
	private OrderManager orderManager;
	
	@At
	@POST
	public Object create(@Param("date") Date date, @Param("time") String time, @Param("order_items") String order_items,
			@Param("delivery") String delivery, @Param("items_price") int items_price,
			@Param("advance_price") int advance_price, @Param("delivery_price") int delivery_price,
			@Param("total_price") int total_price, @Param("payment") int payment) throws Exception {
		User me = this.getMe();
		if (me == null) {
			return err(new NutMap().setv("errmsg", "no user signed in"));
		}

		// 获得最大ID，用于生成订单号code
		Sql sql = Sqls.create("SELECT MAX(id) FROM hlhs_Order;");
		dao.execute(sql);
		int max = sql.getInt(0);
		max++;

		Order order = new Order(max, me.getId(), date, time, order_items, delivery, items_price, advance_price,
				delivery_price, total_price, payment);

		order = dao.insert(order);
		LOG.debugf("Create a new order. id=%d, code=%s", order.getId(), order.getCode());

		return ok(new NutMap().setv("id", order.getId()).setv("code", order.getCode()));
	}
	
	@At("/pay/wechat")
	@POST
	public Object wechatPay(@Param("order_code") String order_code, @Param("total_fee") int total_fee,
			HttpServletRequest request) throws Exception {
		User me = this.getMe();
		if (me == null) {
			return err(new NutMap().setv("errmsg", "no user signed in"));
		}
		Order order = dao.fetch(Order.class, order_code);
		if (order == null) {
			return err(new NutMap().setv("errmsg", "no order found"));
		}

		// 终端IP地址
		String ip = getIpAddr(request);
		
		//创建微信付款订单
		String prepay_id = this.orderManager.createWechatPayment(
				me.getOpenid(),
				order_code,
				total_fee,
				ip);

		if(null!=prepay_id){
			return ok(new NutMap().
					setv("order_code", order_code).
					setv("prepay_id", prepay_id));
		}else{
			return err(new NutMap().setv("errmsg", "create wechat prepay failed"));
		}
	}
	
	//注意：这个支付通知由微信支付平台调用
	//依赖主动订单查询确定订单状态，不依赖这个调用处理订单状态
	@At("/pay/wechat/notify")
	@POST
	public String wechatPayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception{
		InputStreamReader isr = new InputStreamReader(request.getInputStream(), Charset.forName("UTF-8"));
		String content = Streams.readAndClose(isr);
		NutMap map = Xmls.xmlToMap(content);
		
		String error = this.orderManager.processWechatPaymentNotification(map);
		NutMap params = NutMap.NEW();
		if(null == error){
			params.put("return_code", "SUCCESS");
			params.put("return_msg", "OK");
		}else{
			params.put("return_code", "FAIL");
			params.put("return_msg", error);
		}
		String xml = Xmls.mapToXml(params);
		return xml;				
	}

	//返回客户端的IP地址
	private String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
