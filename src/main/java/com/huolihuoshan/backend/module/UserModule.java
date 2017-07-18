package com.huolihuoshan.backend.module;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.weixin.spi.WxLogin;
import org.nutz.weixin.spi.WxResp;

import com.huolihuoshan.backend.bean.User;

@IocBean
@At("/user")
@Ok("json")
@Fail("http:500")
public class UserModule extends BaseModule {


	@Inject("java:$wxLogin.configure($conf,null)")
	protected WxLogin wxLogin;


	protected User saveMe(String openid, String nickname, 
			String sex, String name, String headImageUrl, 
			String country, String province, String city) throws Exception {
		
		boolean add_new = false;
		User user = dao.fetch(User.class, openid);
		if (user == null) {
			user = new User();
			add_new = true;
		}

		user.setOpenid(openid);
		user.setNickname(nickname);
		user.setSex(sex);
		user.setName(name);
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

	protected User getMe() {
		Object attr = Mvcs.getHttpSession(true).getAttribute("me");
		if(attr==null)
			return null;
		return (User) attr;
	}
	
	@At
	public Object queryMe() throws Exception{
		User me = this.getMe();
		if(me==null){
			/*this.saveMe("openid","lixin",
					"1","bryt","http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46",
					"china","hunan","changsha");
			*/
			return new NutMap().
					setv("ok", false).
					setv("payload", "{\"errmsg\":\"no user signed in.\"}");
		}
		else{
			return new NutMap().
				setv("ok", true).
				setv("payload", Json.toJson(me));
		}
	}
	
	//// wecat/login?caller=url&referer=url
	@At
	@GET
	public void login(@Param("caller") String caller, @Param("referer") String referer, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		LOG.debugf("enter /wechat/login: caller=%s; referer=%s", caller, referer);

		User me = this.getMe();
		if (me != null) {
			// redirect back with qs:user
			LOG.debug("user already signed in, redirect back to caller with me");
			redirect_to_caller(caller, me);
			return;
		} else {
			// redirect to weixin login
			String state = String.format("%s111%s", caller.replace("/", "777").replace("#", "888"),
					referer.replace("/", "777").replace("#", "888"));
			String wx_login_url = wxLogin.authorize("/user/wxlogin", "snsapi_userinfo", state);
			LOG.debugf("no user signed in, redirect to wechat: wx_login_url=%s", wx_login_url);
			response.sendRedirect(wx_login_url);
			return;
		}
	}

	//// wecat/wxlogin?code=CODE&state=STATE
	@At
	@GET
	public void wxlogin(@Param("code") String code, @Param("state") String state, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		LOG.debugf("enter /wechat/wxlogin: code=%s; state=%s", code, state);

		String caller = state.split("111")[0].replace("777", "/").replace("888", "#");
		String referer = state.split("111")[1].replace("777", "/").replace("888", "#");

		WxResp resp = wxLogin.access_token(code);
		if (resp.ok()) {
			String openid = resp.getString("openid");
			String token = resp.getString("access_token");

			resp = wxLogin.userinfo(openid, token);
			if (resp.ok()) {
				openid = resp.getString("openid");
				String nickname = resp.getString("nickname");
				String sex = resp.getString("sex");
				String name = "未知";
				if (sex.equals("1"))
					name = "先生";
				if (sex.equals("2"))
					name = "女士";
				String country = resp.getString("country");
				String province = resp.getString("province");
				String city = resp.getString("city");
				String headImageUrl = resp.getString("headimgurl");

				User me = saveMe(openid, nickname, sex, name, headImageUrl, country, province, city);

				// redirect back with qs:user
				LOG.debugf("user already signed in, redirect back to caller with me;");
				redirect_to_caller(caller, me);
				return;
			} else {
				LOG.debugf("userinfo failed: openid=%s; token=%s; err=[%d]%s", openid, token, resp.errcode(),
						resp.errmsg());
				redirect_to_referer(referer);
				return;
			}
		} else {
			LOG.debugf("access_token failed: code=%s; err=[%d]%s", code, resp.errcode(), resp.errmsg());
			redirect_to_referer(referer);
			return;
		}
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
	
	@At
	@GET
	public void logout(HttpSession session) throws Exception {
		LOG.debug("User logout.");
		session.invalidate();
		redirect_to_referer("/?logout=true");
	}
	
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
