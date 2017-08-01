package com.huolihuoshan.backend.biz;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.RandomStringGenerator;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Xmls;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.weixin.bean.WxPayUnifiedOrder;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.util.WxPaySign;

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
	private static final int PAYMENT_TIMEOUT = 60;
	
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
		this.KEY = API_KEY;		
		this.isRunning = true;
		this.start();
	}
	public void shutdown() throws InterruptedException {
		this.setRunning(false);
		this.join();
	}
	
	public void run() {
		int s = 100;
		while (isRunning()) {
			try {
				sleep(s);
				if(intervalTicked(s)){
					checkPayment();
				}
			} catch (Exception e) {
			}
		}
	}
	
	@Inject
	protected Dao dao;
	
	@Inject
	private WxApi2 wxApi2;

	@Inject("java:$conf.get('weixin.appid')")
	private String APP_ID;

	@Inject("java:$conf.get('wechat.api.key')")
	private String API_KEY;
	
	@Inject("java:$conf.get('wechat.pay.mch_id')")
	private String MCH_ID;

	@Inject("java:$conf.get('wechat.pay.notify_url')")
	private String NOTIFY_URL;
	
	private String KEY;
	private boolean SANDBOX = false;
	
	//定时主动查询没有任何响应的订单的状态
	public synchronized void checkPayment(){
		List<Payment> list = dao.query(Payment.class, Cnd.where("trade_state","=","NULL"));
		long now = new Date().getTime()/1000;
		for(Payment payment : list){
			long timeout = now - payment.getCreate_time().getTime()/1000;
			if(timeout>=PAYMENT_TIMEOUT){
				WxPayUnifiedOrder wxPayUnifiedOrder = new WxPayUnifiedOrder();
				wxPayUnifiedOrder.setAppid(APP_ID);
				wxPayUnifiedOrder.setMch_id(MCH_ID);
				wxPayUnifiedOrder.setOut_trade_no(payment.getCode());
				
				NutMap map = this.query_order(KEY, wxPayUnifiedOrder);
				if(!processWechatPaymentQueryReturn(map)){
					//主动查到不成功的订单，就直接删除了
					dao.delete(payment);
					LOG.debugf("delete payment, id=%d",payment.getId());
				}
			}
		}
	}
	
	
	//被动通知订单状态
	public synchronized boolean processWechatPaymentQueryReturn(NutMap map) {
		String ret_sign = map.getString("sign");
		String sign = WxPaySign.createSign(KEY, map);
		if(sign.equals(sign)){
			LOG.debug("sign verified.");
		}else
		{
			String err = String.format("sign verify failed: recv=%s, expect=%s",ret_sign,sign);
			LOG.errorf(err);
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
		if(trade_state.equals("SUCCESS")){
			order.setStatus(OrderStatus.PAID.toCode());
	        dao.update(order);
	        return true;
		}else
		{
			return false;
		}
	}
	
	
	//被动通知订单状态
	public synchronized boolean processWechatPaymentNotification(NutMap map) {
		String ret_sign = map.getString("sign");
		String sign = WxPaySign.createSign(KEY, map);
		if(sign.equals(sign)){
			LOG.debug("sign verified.");
		}else
		{
			String err = String.format("sign verify failed: recv=%s, expect=%s",ret_sign,sign);
			LOG.errorf(err);
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
	
	public NutMap createWechatPayment(User user, Order order, String ip) {
		if(SANDBOX){
			//获取沙箱密钥
			WxPayUnifiedOrder wxPayUnifiedOrder = new WxPayUnifiedOrder();
			wxPayUnifiedOrder.setMch_id(MCH_ID);
			NutMap map = this.getsignkey(API_KEY, wxPayUnifiedOrder);
			String sandbox_signkey = map.getString("sandbox_signkey");
			if(null == sandbox_signkey){
				LOG.debugf("get sandbox sign key failed, use API key: %s", API_KEY);
				this.KEY = API_KEY;
			}
			else{
				LOG.debugf("get sandbox sign key: %s",sandbox_signkey);
				this.KEY = sandbox_signkey;
			}
		}
		
		Payment payment = new Payment(order.getId());
		
		WxPayUnifiedOrder wxPayUnifiedOrder = new WxPayUnifiedOrder();
		wxPayUnifiedOrder.setAppid(APP_ID);
		wxPayUnifiedOrder.setMch_id(MCH_ID);
		wxPayUnifiedOrder.setBody("活力火山健康轻食");
		wxPayUnifiedOrder.setOut_trade_no(payment.getCode());
		wxPayUnifiedOrder.setTotal_fee(order.getTotal_price());
		wxPayUnifiedOrder.setSpbill_create_ip(ip);
		wxPayUnifiedOrder.setNotify_url(NOTIFY_URL);
		wxPayUnifiedOrder.setTrade_type("JSAPI");
		wxPayUnifiedOrder.setOpenid(user.getOpenid());

		NutMap args = this.pay_jsapi(KEY, wxPayUnifiedOrder);
		if(null == args){
			return null;
		}
		
		//生成payment记录
		dao.insert(payment);
		return args;
	}
	
	
	
    /**
     * 微信支付公共POST方法（不带证书）
     *
     * @param url    请求路径
     * @param key    商户KEY
     * @param params 参数
     * @return
     */
    //@Override
    public NutMap postPay(String url, String key, Map<String, Object> params) {
        params.remove("sign");
        String sign = WxPaySign.createSign(key, params);
        params.put("sign", sign);
        Request req = Request.create(url, METHOD.POST);
        String xml = Xmls.mapToXml(params);
        LOG.debug(xml);
        req.setData(xml);
        Response resp = Sender.create(req).send();
        
        //这里微信支付服务器会返回201，不能用isOK=200来判断 
        if (resp.isServerError()) 
            throw new IllegalStateException("postPay, resp code=" + resp.getStatus());
        xml = resp.getContent("UTF-8");
        LOG.debug(xml);
        return Xmls.xmlToMap(xml);
    }
    
    
    /**
     * 统一下单
     *
     * @param key               商户KEY
     * @param wxPayUnifiedOrder 交易订单内容
     * @return
     */
    //@Override
    public NutMap pay_unifiedorder(String key, WxPayUnifiedOrder wxPayUnifiedOrder) {		
    	String url;
    	if(!SANDBOX)
    		url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    	else
    		url = "https://api.mch.weixin.qq.com/sandboxnew/pay/unifiedorder";
    	
		wxPayUnifiedOrder.setNonce_str(R.UU32());
    	Map<String, Object> params = Lang.obj2map(wxPayUnifiedOrder);
        return this.postPay(url, key, params);
    }
    
    /**
     * 获取沙箱密钥
     *
     * @param wxPayUnifiedOrder 交易订单内容
     * @return
     */
    //@Override
    public NutMap getsignkey(String key, WxPayUnifiedOrder wxPayUnifiedOrder) {
    	String url;
    	if(!SANDBOX)
    		url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    	else
    		url = "https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey";
    	
		wxPayUnifiedOrder.setNonce_str(R.UU32());
    	Map<String, Object> params = Lang.obj2map(wxPayUnifiedOrder);
        return this.postPay(url, key, params);
    }

    /**
     * 微信公众号JS支付
     * @param key 商户KEY
     * @param wxPayUnifiedOrder 交易订单内容
     * @return 客户端JSAPI可以直接调用的参数
     */
    //@Override
    public NutMap pay_jsapi(String key, WxPayUnifiedOrder wxPayUnifiedOrder) {
        NutMap map = this.pay_unifiedorder(key, wxPayUnifiedOrder);
        
        //没拿到prepay_id，就失败了
        String prepay_id = map.getString("prepay_id");
        if(null == prepay_id)
        	return null;
        
        NutMap params = NutMap.NEW();
        params.put("appId", wxPayUnifiedOrder.getAppid());
        params.put("timeStamp", String.valueOf((int) (System.currentTimeMillis() / 1000)));
        params.put("nonceStr", R.UU32());
        params.put("package", "prepay_id=" + map.getString("prepay_id"));
        params.put("signType", "MD5");
        String sign = WxPaySign.createSign(key, params);
        params.put("paySign", sign);
        return params;
    }
    
    //查询订单
    public NutMap query_order(String key, WxPayUnifiedOrder wxPayUnifiedOrder) {
    	String url;
    	if(!SANDBOX)
    		url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    	else
    		url = "https://api.mch.weixin.qq.com/sandboxnew/pay/orderquery";
    	
		wxPayUnifiedOrder.setNonce_str(R.UU32());
    	Map<String, Object> params = Lang.obj2map(wxPayUnifiedOrder);
        return this.postPay(url, key, params);
    }
}