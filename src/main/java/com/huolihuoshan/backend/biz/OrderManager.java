package com.huolihuoshan.backend.biz;

import java.util.Map;

import org.apache.commons.text.RandomStringGenerator;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.http.Request.METHOD;
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
import com.huolihuoshan.backend.bean.User;

@IocBean(singleton = true)
public class OrderManager {

	private final Log LOG = Logs.getLog(this.getClass());

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
	
	public synchronized String processWechatPaymentNotification(NutMap map) {
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
		
		String return_code = map.getString("return_code");
		String result_code = map.getString("result_code");
		
		if(	!return_code.equals("SUCCESS") || !result_code.equals("SUCCESS")){
			LOG.fatal("返回码错误");
			return null;
		}
		
		//验证订单号
		String pay_code = map.getString("out_trade_no");
		Order order = dao.fetch(Order.class, Cnd.where("pay_code", "=", pay_code));
		if(order==null){
			LOG.fatalf("支付通知消息找不到对应的订单。 recv pay_code=%s", pay_code);
			return null;
		}
		
		//验证订单状态
		if(order.getStatus() != OrderStatus.CREATED.toCode()){
			LOG.debugf("订单已支付完成，status=%d",order.getStatus());
			return null;
		}
		
		//验证订单金额
		int total_fee = map.getInt("total_fee");
		if(total_fee != order.getTotal_price()){
			LOG.fatalf("支付通知消息对应的订单金额不一致。 recv=%d expect=%d", total_fee, order.getTotal_price() );
			return null;
		}

		//验证用户
		String openid = map.getString("openid");
		User user = dao.fetch(User.class,order.getId_user());
		if(user==null){
			LOG.debugf("找不到订单对应的用户记录。 id=%d", order.getId_user());
			return null;
		}
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

	
	public NutMap createWechatPayment(String openid, String pay_code, int total_fee, String ip) {
		// 32位随机字符串
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('A', 'Z').build();
		String nonce_str = generator.generate(32);

		//获取沙箱密钥
		WxPayUnifiedOrder wxPayUnifiedOrder = new WxPayUnifiedOrder();
		wxPayUnifiedOrder.setMch_id(MCH_ID);
		wxPayUnifiedOrder.setNonce_str(nonce_str);
		NutMap map = this.getsignkey(API_KEY, wxPayUnifiedOrder);
		String sandbox_signkey = map.getString("sandbox_signkey");
		if(null == sandbox_signkey)
			return null;

		wxPayUnifiedOrder = new WxPayUnifiedOrder();
		wxPayUnifiedOrder.setAppid(APP_ID);
		wxPayUnifiedOrder.setMch_id(MCH_ID);
		wxPayUnifiedOrder.setNonce_str(nonce_str);
		wxPayUnifiedOrder.setBody("活力火山健康轻食");
		wxPayUnifiedOrder.setOut_trade_no(pay_code);
		wxPayUnifiedOrder.setTotal_fee(total_fee);
		wxPayUnifiedOrder.setSpbill_create_ip(ip);
		wxPayUnifiedOrder.setNotify_url(NOTIFY_URL);
		wxPayUnifiedOrder.setTrade_type("JSAPI");
		wxPayUnifiedOrder.setOpenid(openid);

		//return this.pay_jsapi(API_KEY, wxPayUnifiedOrder);
		return this.pay_jsapi(sandbox_signkey, wxPayUnifiedOrder);
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
        req.setData(Xmls.mapToXml(params));
        Response resp = Sender.create(req).send();
        
        //这里微信支付服务器会返回201，不能用isOK=200来判断 
        if (resp.isServerError()) 
            throw new IllegalStateException("postPay, resp code=" + resp.getStatus());
        String xml = resp.getContent("UTF-8");
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
    	//String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    	String url = "https://api.mch.weixin.qq.com/sandboxnew/pay/unifiedorder";
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
    	//String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    	String url = "https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey";
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

}