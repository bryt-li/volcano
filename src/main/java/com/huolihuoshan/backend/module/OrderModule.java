package com.huolihuoshan.backend.module;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import com.huolihuoshan.backend.biz.UserManager;

@IocBean
@At("/order")
@Ok("json")
@Fail("http:500")
public class OrderModule extends BaseModule {
	
	@Inject
	private OrderManager orderManager;

	@At
	@POST
	public Object get(@Param("id") int id){
		Order order = this.orderManager.getOrder(id);
		if(order==null)
			return err("no order found");
		
		return ok(order);
	}
	
	@At
	@POST
	public Object list(){
		List<Order> orders = this.orderManager.getOrderList();
		if(orders==null){
			return err("no order found");
		}
		return ok(orders);
	}
	
	@At
	@POST
	public Object create(
			@Param("date") String date, 
			@Param("time") String time, 
			@Param("items") String items,
			@Param("delivery") String delivery, 
			@Param("items_price") int items_price,
			@Param("advance_price") int advance_price, 
			@Param("delivery_price") int delivery_price,
			@Param("total_price") int total_price, 
			@Param("payment") int payment) throws Exception {
		
		int id = this.orderManager.createOrder(
				date,time,
				items,delivery,
				items_price,advance_price,delivery_price,total_price,
				payment);
		
		if(id<0)
			return err("create order fail");
		else
			return ok(new NutMap().setv("id", id));
	}
	
	@At
	@POST
	public Object getWechatPayJsapiArgs(
			@Param("id") int id,
			HttpServletRequest request) throws Exception {

		String ip = this.getIpAddr(request);
		
		//创建微信预付款订单
		NutMap args = this.orderManager.createWechatPayment(id,ip);
		if(args==null)
			return err("create payment failed");
		else
			return ok(args);
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
