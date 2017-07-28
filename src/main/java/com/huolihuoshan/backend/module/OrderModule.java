package com.huolihuoshan.backend.module;

import java.util.Date;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import com.huolihuoshan.backend.bean.Order;
import com.huolihuoshan.backend.bean.User;

@IocBean
@At("/order")
@Ok("json")
@Fail("http:500")
public class OrderModule extends BaseModule{

	@At
	@POST
	public Object create(
			@Param("date") Date date, 
			@Param("time") String time, 
			@Param("order_items") String order_items, 
			@Param("delivery") String delivery, 
			@Param("items_price") float items_price,
			@Param("advance_price") float advance_price,
			@Param("delivery_price") float delivery_price,
			@Param("total_price") float total_price,
			@Param("payment") int payment
			) throws Exception{
		User me = this.getMe();
		if(me==null){
			return new NutMap().
					setv("ok", false).
					setv("payload", new NutMap().setv("errmsg", "no user signed in"));
		}
		Order order = new Order(me.getId(),
				date,time,
				order_items,delivery,
				items_price, advance_price, delivery_price,
				total_price, payment);
		
		order = dao.insert(order);
		LOG.debug("Create a new order. id="+order.getId());
		
		return new NutMap().
				setv("ok", true).
				setv("payload", new NutMap().setv("id", order.getId()));
	}

}
