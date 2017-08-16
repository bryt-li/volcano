package com.huolihuoshan.backend.module;

import java.util.List;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import com.huolihuoshan.backend.bean.Order;
import com.huolihuoshan.backend.bean.OrderStatus;
import com.huolihuoshan.backend.biz.OrderManager;

@IocBean
@Ok("json")
@Fail("http:500")
public class AdminModule extends BaseModule{
	@Inject
	private OrderManager orderManager;

	@At("/admin/order/list")
	@POST
	public Object list(@Param("status") String status){
		OrderStatus s = OrderStatus.fromString(status);
		List<Order> orders = this.orderManager.getAdminOrderList(s);
		if(orders==null){
			return err("no order found");
		}
		return ok(orders);
	}
	
	@At("/admin/order/update/status")
	@POST
	public Object update_status(@Param("id") int id, @Param("status") int status){
		boolean ret = this.orderManager.updateOrderState(id, status);
		if(ret)
			return ok(new NutMap().setv("status", status));
		else
			return err("update fail");
	}
}
