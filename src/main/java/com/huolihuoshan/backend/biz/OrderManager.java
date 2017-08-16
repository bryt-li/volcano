package com.huolihuoshan.backend.biz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.weixin.spi.WxApi2;

import com.huolihuoshan.backend.bean.Order;
import com.huolihuoshan.backend.bean.OrderStatus;
import com.huolihuoshan.backend.bean.Payment;
import com.huolihuoshan.backend.bean.User;

@IocBean(singleton = true)
public class OrderManager extends Thread{

	private final Log LOG = Logs.getLog(this.getClass());

	//检查间隔
	private static final int INTERVAL = 5;
	
	//支付订单超时
	private static final int PAYMENT_TIMEOUT = 120;
	
	private int tick = 0;
	private boolean intervalTicked(int elapsed){
		this.tick += elapsed;
		if(this.tick >= INTERVAL*1000){
			this.tick =0;
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isRunning = false;
	public synchronized boolean isRunning() {
		return isRunning;
	}
	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	public void startup() throws Exception {
		this.isRunning = true;
		this.start();
		LOG.debug("Order Manager startup.");
	}
	public void shutdown() throws InterruptedException {
		this.setRunning(false);
		this.join();
		LOG.debug("Order Manager stop.");
	}
	
	public void run() {
		int s = 100;
		while (isRunning()) {
			try {
				sleep(s);
				if(intervalTicked(s)){
					//checkPayment();
				}
			} catch (Exception e) {
			}
		}
	}
	
	@Inject
	protected Dao dao;
	
	@Inject
	private UserManager userManager;
	
	@Inject
	private WxApi2 wxApi2;

	//被动通知订单状态
	public synchronized boolean processWechatPaymentNotification(NutMap map) {
		if(!this.wxApi2.is_sign_correct(map)){
			LOG.errorf("sign verify failed");
			return false;
		}
		
		String return_code = map.getString("return_code");
		String result_code = map.getString("result_code");
		
		if(	!return_code.equals("SUCCESS") || !result_code.equals("SUCCESS")){
			LOG.fatal("返回码错误");
			return false;
		}
		
		//验证订单号
		String out_trade_no = map.getString("out_trade_no");
		Payment payment = dao.fetchLinks(dao.fetch(Payment.class, out_trade_no), "order");

		if(payment==null || payment.getOrder()==null){
			LOG.fatalf("支付通知消息找不到对应的记录。 recv out_trade_no=%s", out_trade_no);
			return true;
		}
		
		//验证订单状态
		Order order = payment.getOrder();
		if(order.getStatus() != OrderStatus.CREATED.toCode()){
			LOG.debugf("订单已支付完成，忽略重复通知。status=%d",order.getStatus());
			return true;
		}
		
		//验证订单金额
		int total_fee = map.getInt("total_fee");
		if(total_fee != order.getTotal_price()){
			LOG.fatalf("支付通知消息对应的订单金额不一致。 recv=%d expect=%d", total_fee, order.getTotal_price() );
			return true;
		}
		
		String err_code = map.getString("err_code");
		String err_code_des = map.getString("err_code_des");
		String openid = map.getString("openid");
		String transaction_id = map.getString("transaction_id");
		String time_end = map.getString("time_end");
		String trade_state_desc = map.getString("trade_state_desc");
		String trade_state = "SUCCESS";

		//保存payment支付记录
		payment.setField(return_code, result_code, trade_state, err_code, err_code_des, openid, total_fee, transaction_id, time_end, trade_state_desc);
        dao.update(payment);
		
        //如果支付成功，修改订单状态为：已支付
		order.setStatus(OrderStatus.PAID.toCode());
        dao.update(order);

		return true;
	}
	
	public NutMap createWechatPayment(int id, String ip) {
		User me = this.userManager.getMe();
		if(me==null)
			return null;

		Order order = dao.fetch(Order.class, id);
		if (order == null) {
			return null;
		}
		
		Payment payment = new Payment(order.getId());
		NutMap args = wxApi2.get_pay_jsapi_args(
				"活力火山健康轻食", 
				me.getOpenid(), ip, 
				payment.getCode(),
				order.getTotal_price());

		if(null == args){
			return null;
		}
		
		//生成payment记录
		dao.insert(payment);
		
		LOG.debugf("return args: '%s'", Json.toJson(args));
		return args;
	}
    
	public Order getOrder(int id) {
		User me = this.userManager.getMe();
		if(me==null){
			return null;
		}

		return dao.fetch(Order.class,id);
	}
	
	public List<Order> getOrderList(){
		User me = this.userManager.getMe();
		if(me==null)
			return null;

		me = dao.fetchLinks(me,"orders");
		return me.getOrders();
	}
	
	
	public int createOrder(String date, String time, String items, String delivery, int items_price, int advance_price,
			int delivery_price, int total_price, int payment) throws Exception {

		User me = this.userManager.getMe();
		if(me==null)
			return -1;

		//把date从字符串转为Date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	    Date dt = sdf.parse(date); 
	    
		// 获得最大ID，用于生成订单号code
		Sql sql = Sqls.create("SELECT MAX(id) FROM hlhs_Order;");
		dao.execute(sql);
		int max = sql.getInt(0);
		max++;

		Order order = new Order(max, me.getId(), dt, time, items, delivery, items_price, advance_price,
				delivery_price, total_price, payment);

		order = dao.insert(order);
		LOG.debugf("Create a new order. id=%d, code=%s", order.getId(), order.getCode());
		return order.getId();
	}
	
	
	
	public List<Order> getAdminOrderList(OrderStatus status) {
		User me = this.userManager.getMe();
		if(me==null)
			return null;

		return dao.query(Order.class, Cnd.where("status","=",status.toCode()));
	}
	
	public boolean updateOrderState(int id, int status) {
		User me = this.userManager.getMe();
		if(me==null)
			return false;

		Order o = dao.fetch(Order.class,id);
		if(null==o)
			return false;
		
		o.setStatus(status);
		return dao.update(o)==1;
	}

/*
 * 这个主动检查的逻辑暂时先不添加，先依赖wechat的notification
 * 以后发现确实会出问题再说
 * 
	//定时主动查询没有任何响应的订单的状态
	public synchronized void checkPayment(){
		List<Payment> list = dao.query(Payment.class, Cnd.where("trade_state","=","NULL"));
		long now = new Date().getTime()/1000;
		for(Payment payment : list){
			long timeout = now - payment.getCreate_time().getTime()/1000;
			if(timeout>=PAYMENT_TIMEOUT){
				NutMap map = this.wxApi2.query_payment(payment.getCode());
				
				if(!processWechatPaymentQueryReturn(map)){
					//主动查到不成功的订单，就直接删除了
					dao.delete(payment);
					LOG.debugf("delete payment, id=%d",payment.getId());
				}
			}
		}
	}
	
	
	//处理微信返回的订单状态信息
	public synchronized boolean processWechatPaymentQueryReturn(NutMap map) {
		if(!this.wxApi2.is_sign_correct(map)){
			LOG.errorf("sign verify failed");
			return false;
		}
		
		String return_code = map.getString("return_code");
		String result_code = map.getString("result_code");
		
		if(	!return_code.equals("SUCCESS") || !result_code.equals("SUCCESS")){
			LOG.fatal("返回码错误");
			return false;
		}
		
		//验证订单号
		String out_trade_no = map.getString("out_trade_no");
		Payment payment = dao.fetchLinks(dao.fetch(Payment.class, out_trade_no), "order");

		if(payment==null || payment.getOrder()==null){
			LOG.fatalf("支付通知消息找不到对应的记录。 recv out_trade_no=%s", out_trade_no);
			return false;
		}
		
		//验证订单状态
		Order order = payment.getOrder();
		if(order.getStatus() != OrderStatus.CREATED.toCode()){
			LOG.debugf("订单已支付完成，忽略重复通知。status=%d",order.getStatus());
			return false;
		}
		
		//验证订单金额
		int total_fee = map.getInt("total_fee");
		if(total_fee != order.getTotal_price()){
			LOG.fatalf("支付通知消息对应的订单金额不一致。 recv=%d expect=%d", total_fee, order.getTotal_price() );
			return false;
		}
		
		String trade_state = map.getString("trade_state");
		String err_code = map.getString("err_code");
		String err_code_des = map.getString("err_code_des");
		String openid = map.getString("openid");
		String transaction_id = map.getString("transaction_id");
		String time_end = map.getString("time_end");
		String trade_state_desc = map.getString("trade_state_desc");
		
		//保存payment支付记录
		payment.setField(return_code, result_code, trade_state, err_code, err_code_des, openid, total_fee, transaction_id, time_end, trade_state_desc);
        dao.update(payment);
		
        //如果支付成功，修改订单状态为：已支付
		if(null!=trade_state && trade_state.equals("SUCCESS")){
			order.setStatus(OrderStatus.PAID.toCode());
	        dao.update(order);
	        return true;
		}else
		{
			return false;
		}
	}
	
	*/
	
}
