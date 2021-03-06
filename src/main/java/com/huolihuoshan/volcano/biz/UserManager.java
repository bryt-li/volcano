package com.huolihuoshan.volcano.biz;

import javax.servlet.http.HttpSession;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.weixin.spi.WxLogin;
import org.nutz.weixin.spi.WxResp;

import com.huolihuoshan.volcano.bean.Delivery;
import com.huolihuoshan.volcano.bean.User;

@IocBean(singleton = true)
public class UserManager {

	protected final Log LOG = Logs.getLog(this.getClass());

	/** 注入与属性同名的一个ioc对象 */
	@Inject
	protected Dao dao;

	//configured in nutzwx project, location:
	//      org/nutz/plugins/weixin/weixin.js
	//@Inject("java:$wxLogin.configure($conf,'weixin.')")
	@Inject
	protected WxLogin wxLogin;
	
	public boolean wechatLogin(String auth_code) throws Exception{
		LOG.debugf("auth_code='%s'", auth_code);
		WxResp resp = this.wxLogin.access_token(auth_code);
		if (resp.ok()) {
			String openid = resp.getString("openid");
			String token = resp.getString("access_token");
			LOG.debugf("openid='%s', token='%s'", openid,token);
			
			resp = this.wxLogin.userinfo(openid, token);
			if (resp.ok()) {
				openid = resp.getString("openid");
				String nickname = filterUtf8mb4(resp.getString("nickname"));
				String sex = resp.getString("sex");
				String country = resp.getString("country");
				String province = resp.getString("province");
				String city = resp.getString("city");
				String headImageUrl = resp.getString("headimgurl");

				saveMe(openid, nickname, sex, 
						headImageUrl, 
						country, province, city);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public int logout(){
		User me = getMe();
		if(me==null){
			return -1;
		}
		
		LOG.debugf("User logout. %s", me.getNickname());
		
		HttpSession session = Mvcs.getHttpSession(false);
		if(session!=null)
			session.invalidate();
		
		return me.getId();
	}
	
	public User getMe() {
		HttpSession session = Mvcs.getHttpSession(false);
		if(session!=null){
			Object attr = session.getAttribute("me");
			if(attr==null)
				return null;
			return (User) attr;
		}else
			return null;
	}

	public static String filterUtf8mb4(String str) {
        final int LAST_BMP = 0xFFFF;
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            int codePoint = str.codePointAt(i);
            if (codePoint < LAST_BMP) {
                sb.appendCodePoint(codePoint);
            } else {
                i++;
            }
        }
        return sb.toString();
    }
	
	private User saveMe(String openid, String nickname, 
			String sex, String headImageUrl, 
			String country, String province, String city) throws Exception {
		
		boolean add_new = false;
		User user = dao.fetchLinks(dao.fetch(User.class, openid),"delivery");
		
		if (user == null) {
			user = new User();
			add_new = true;
		}

		user.setOpenid(openid);
		user.setNickname(nickname);
		user.setSex(sex);
		user.setHeadImageUrl(headImageUrl);
		user.setCountry(country);
		user.setProvince(province);
		user.setCity(city);

		if(add_new){
			user = dao.insert(user);
			LOG.debug("Create a new user. nickname="+nickname);
		}
		else{
			dao.update(user);
			LOG.debug("Update an existed user. nickname="+nickname);
		}
		
		Mvcs.getHttpSession(true).setAttribute("me", user);
		return user;
	}

	public int saveDelivery(String name, String phone, 
			String address, String city, String location, 
			float lat, float lng) {

		User me = this.getMe();
		if(me==null){
			return -1;
		}
		
		boolean add_new = false;
		Delivery delivery = dao.fetch(Delivery.class, me.getId());
		if (delivery == null) {
			delivery = new Delivery();
			add_new = true;
			delivery.setId(me.getId());
		}
		
		delivery.setName(name);
		delivery.setAddress(address);
		delivery.setCity(city);
		delivery.setLocation(location);
		delivery.setPhone(phone);
		delivery.setLat(lat);
		delivery.setLng(lng);
		
		if(add_new){
			delivery = dao.insert(delivery);
			LOG.debug("Create a new delivery. id="+delivery.getId());
		}
		else{
			dao.update(delivery);
			LOG.debug("Update an existed delivery. name="+name);
		}
		
		//save to session
		me.setDelivery(delivery);		
		Mvcs.getHttpSession(true).setAttribute("me", me);
		
		return delivery.getId();
	}
	
}
