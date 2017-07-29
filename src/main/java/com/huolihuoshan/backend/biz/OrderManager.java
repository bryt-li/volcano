package com.huolihuoshan.backend.biz;

import org.apache.commons.text.RandomStringGenerator;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.weixin.bean.WxPayUnifiedOrder;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.util.WxPaySign;

import com.huolihuoshan.backend.bean.Order;
import com.huolihuoshan.backend.bean.OrderStatus;
import com.huolihuoshan.backend.bean.User;

@IocBean(singleton = true)
public class OrderManager {

	private final Log LOG = Logs.getLog(this.getClass());

	@Inject
	protected Dao dao;

	
	@Inject
	private WxApi2 wxApi2;

	@Inject("java:$conf.get('wechat.api.key')")
	private String API_KEY;

	@Inject("java:$conf.get('appid')")
	private String APP_ID;
	
	@Inject("java:$conf.get('wechat.pay.mch_id')")
	private String MCH_ID;

	@Inject("java:$conf.get('wechat.pay.notify_url')")
	private String NOTIFY_URL;
	
	public synchronized String processWechatPaymentNotification(NutMap map) {
		if(map.getString("return_code") != "SUCCESS"){
			LOG.fatal("通信错误");
			return null;
		}
			
		String ret_sign = map.getString("sign");
		String sign = WxPaySign.createSign(API_KEY, map);
		if(sign.equals(sign)){
			LOG.debug("sign verified.");
		}else
		{
			String err = String.format("sign verify failed: recv=%s, expect=%s",ret_sign,sign);
			LOG.errorf(err);
			return err;
		}
		
		//是否支付成功？
		String result_code = map.getString("result_code");
		if(!result_code.equals("SUCCESS")){
			LOG.fatalf("支付通知消息显示支付失败。 result_code=%s", result_code);
			return null;
		}
		
		//验证订单号
		String order_code = map.getString("out_trade_no");
		Order order = dao.fetch(Order.class,order_code);
		if(order==null){
			LOG.fatalf("支付通知消息找不到对应的订单。 recv order_code=%s", order_code);
			return null;
		}
		
		//验证订单状态
		OrderStatus status = OrderStatus.fromCode(order.getStatus()); 
		if(status != OrderStatus.CREATED){
			LOG.debugf("订单已支付完成，status=%s",status);
			return null;
		}
		
		//验证订单金额
		int total_fee = Integer.parseInt(map.getString("total_fee"));
		if(total_fee != order.getTotal_price()){
			LOG.fatalf("支付通知消息对应的订单金额不一致。 recv=%d expect=%d", total_fee, order.getTotal_price() );
			return null;
		}
				
		//验证用户
		User user = dao.fetch(User.class,order.getId_user());
		if(user==null){
			LOG.debugf("找不到订单对应的用户记录。 id=%d", order.getId_user());
			return null;
		}
		String openid = map.getString("openid");
		if(!user.getOpenid().equals(openid)){
			LOG.fatalf("订单归属用户的OpenID不一致。 recv=%s expect=%s", openid, user.getOpenid());
			return null;
		}
		
		//修改订单状态为：已支付
		order.setStatus(OrderStatus.PAID.toCode());
		int count = dao.update(order);
		if(count!=1){
			LOG.fatalf("订单状态更新失败 update count=%d", count);
			return null;
		}
		
		return null;
	}

	
	public String createWechatPayment(String openid, String order_code, int total_fee, String ip) {
		// 32位随机字符串
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('A', 'Z').build();
		String nonce_str = generator.generate(32);

		WxPayUnifiedOrder wxPayUnifiedOrder = new WxPayUnifiedOrder();
		wxPayUnifiedOrder.setAppid(APP_ID);
		wxPayUnifiedOrder.setMch_id(MCH_ID);
		wxPayUnifiedOrder.setNonce_str(nonce_str);
		wxPayUnifiedOrder.setBody("活力火山健康轻食");
		wxPayUnifiedOrder.setOut_trade_no(order_code);
		wxPayUnifiedOrder.setTotal_fee(total_fee);
		wxPayUnifiedOrder.setSpbill_create_ip(ip);
		wxPayUnifiedOrder.setNotify_url(NOTIFY_URL);
		wxPayUnifiedOrder.setTrade_type("JSAPI");
		wxPayUnifiedOrder.setOpenid(openid);

		NutMap resp = this.wxApi2.pay_unifiedorder(API_KEY, wxPayUnifiedOrder);
		String prepay_id = resp.getString("prepay_id");
		return prepay_id;
	}
	
}