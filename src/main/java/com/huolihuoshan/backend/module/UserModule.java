package com.huolihuoshan.backend.module;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.weixin.spi.WxLogin;
import org.nutz.weixin.spi.WxResp;

import com.huolihuoshan.backend.bean.Delivery;
import com.huolihuoshan.backend.bean.User;

@IocBean
@At("/user")
@Ok("json")
@Fail("http:500")
public class UserModule extends BaseModule{

	//configured in nutzwx project, location:
	//      org/nutz/plugins/weixin/weixin.js
	@Inject("java:$wxLogin.configure($conf,'weixin.')")
	protected WxLogin wxLogin;
	
	@Inject("java:$conf.get('wechat.login.access_token_url')")
	private String access_token_url;

	@Inject("java:$conf.get('wechat.login.userinfo_url')")
	private String userinfo_url;

	//// this is called by client and redirected by wechat open platform
	//// wechat/wxlogin?code=CODE&state=STATE
	@At
	@GET
	@Ok("jsp:/wechat/login")
	public String wxlogin(@Param("code") String code, @Param("state") String state, 
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		LOG.debugf("enter /wechat/wxlogin: code=%s; state=%s", code, state);

		WxResp resp = wxLogin.access_token(access_token_url, code);
		if (resp.ok()) {
			String openid = resp.getString("openid");
			String token = resp.getString("access_token");

			resp = wxLogin.userinfo(userinfo_url, openid, token);
			if (resp.ok()) {
				openid = resp.getString("openid");
				String nickname = resp.getString("nickname");
				String sex = resp.getString("sex");
				String country = resp.getString("country");
				String province = resp.getString("province");
				String city = resp.getString("city");
				String headImageUrl = resp.getString("headimgurl");

				User me = saveMe(openid, nickname, sex, 
						headImageUrl, 
						country, province, city);
				
				String json = Json.toJson(me).replace("\n", " ");
				LOG.debugf("return user json: %s",json);
				return json;
			} else {
				LOG.debugf("userinfo failed redirect to /: openid=%s; token=%s; err=[%d]%s", openid, token, resp.errcode(),
						resp.errmsg());
				return "err";
			}
		} else {
			LOG.debugf("access_token failed redirect to /: code=%s; err=[%d]%s", code, resp.errcode(), resp.errmsg());
			return "err";
		}
	}
	
	@At
	public Object logout(HttpSession session) throws Exception {
		User me = getMe();
		if(me==null){
			return err(new NutMap().setv("errmsg", "no user signed in"));
		}
		
		LOG.debugf("User logout. %s", me.getNickname());
		session.invalidate();
		
		return ok(new NutMap().setv("id", me.getId()));
	}
	
	@At
	public Object me() throws Exception{
		User me = this.getMe();
		if(me==null){
			String msg = "no user signed in";
			LOG.debug(msg);
			return err(new NutMap().setv("errmsg", msg));
		}
		else{
			LOG.debugf("return signed-in user: id=%d",me.getId());
			return ok(me);
		}
	}
	
	@At("/delivery/save")
	@POST
	public Object saveDelivery(@Param("id") int id, @Param("name") String name, 
			@Param("phone") String phone, @Param("address") String address,
			@Param("city") String city, @Param("location") String location,
			@Param("lat") float lat, @Param("lng") float lng,
			HttpSession session) throws Exception{
		User me = this.getMe();
		if(me==null){
			return err(new NutMap().setv("errmsg", "no user signed in"));
		}
		
		boolean add_new = false;
		Delivery delivery = dao.fetch(Delivery.class, id);
		if (delivery == null) {
			delivery = new Delivery();
			add_new = true;
		}
		
		delivery.setId(me.getId());
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
		session.setAttribute("me", me);
		
		return ok(new NutMap().setv("id", delivery.getId()));
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

	
	/*
	//the following is for pure html/js frontend request
	
	@At
	@POST
	public Object login(String account, String password) {
		User user = dao.fetch(User.class, Cnd.where("account", "=", account).and("password", "=", password));
		if (user == null) {
			return new NutMap().setv("ok", false).setv("err", "用户名和密码不匹配");
		} else {
//			this.saveMe(user.getId());
			return new NutMap().
					setv("ok", true).
					setv("data", new NutMap().
							setv("id", user.getId()).
							setv("name",user.getName()).
							setv("role", "管理员"));
		}
	}
	 */
		
	/*
	//the following is for traditional JSP request
	//I disabled them as our frontend is a react-redux base app.
	
	@GET
	@At("/login")
	@Ok("re:jsp:user.login")
	public String loginPage(ViewModel model) {
		model.setv("js", "user/login.js");
		model.setv("css", "user/login.css");
		
		if (getMe() == null)
			return null;
		else {
			String url = getSavedUrl();
			if(url == null)
				return "redirect:dashboard";
			else
				return "redirect:" + url;
		}
	}

	@GET
	@At
	@Ok("re:jsp:user.profile")
	public String profile() {
		if (getMe() != null)
			return null;
		else
			return redirectToLoginPage();
	}

	@GET
	@At
	@Ok("re:jsp:dashboard")
	public String dashboard() {
		if (getMe() != null)
			return null;
		else
			return redirectToLoginPage();
	}

	@At
	@POST
	public Object login(@Param("account") String account, @Param("password") String password, HttpSession session) {		
		User user = dao.fetch(User.class, Cnd.where("account", "=", account).and("password", "=", password));
		if (user == null) {
			return false;
		} else {
			session.setAttribute("me", user.getId());
			String url = getSavedUrl();
			if(url == null)
				return "dashboard";
			else
				return url;
		}
	}

	@At
	@Ok(">>:/")
	public void logout(HttpSession session) {
		session.invalidate();
	}

	@At
	public Object delete(@Param("id") int id, @Attr("me") int me) {
		if (me == id) {
			return new NutMap().setv("ok", false).setv("msg", "不能删除当前用户!!");
		}
		dao.delete(User.class, id); // 再严谨一些的话,需要判断是否为>0
		return new NutMap().setv("ok", true);
	}

	@At
	public int count() {
		return dao.count(User.class);
	}
	*/
}
