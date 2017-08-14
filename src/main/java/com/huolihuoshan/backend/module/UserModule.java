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
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.weixin.spi.WxLogin;
import org.nutz.weixin.spi.WxResp;

import com.huolihuoshan.backend.bean.Delivery;
import com.huolihuoshan.backend.bean.User;
import com.huolihuoshan.backend.biz.UserManager;

@IocBean
@At("/user")
@Ok("json")
@Fail("http:500")
public class UserModule extends BaseModule{

	@Inject
	private UserManager userManager;

	@Inject("java:$conf.get('hlhs_frontend_url')")
	private String hlhs_frontend_url;
		
	//// this is called by client and redirected by wechat open platform
	//// wechat/wxlogin?code=CODE&state=STATE
	@At
	public void wxlogin(@Param("code") String code, @Param("state") String state, 
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		LOG.debugf("enter /wechat/wxlogin: code=%s; state=%s", code, state);
		String dest = state.replace("777", "/");
		dest = String.format("%s%s", hlhs_frontend_url,dest);
		String home = String.format("%s", hlhs_frontend_url);
	
		if(userManager.wechatLogin(code)){
			response.sendRedirect(dest);		
		}else{
			response.sendRedirect(home);
		}
	}
	
	@At
	public Object logout(HttpSession session) throws Exception {
		int id = userManager.logout();
		if(id<0){
			return err("no user signed in");
		}else{
			return ok(new NutMap().setv("id", id));			
		}
	}
	
	@At
	public Object me() throws Exception{
		User me = this.userManager.getMe();
		if(me==null){
			String msg = "no user signed in";
			LOG.debug(msg);
			return err(msg);
		}
		else{
			LOG.debugf("return signed-in user: id=%d",me.getId());
			return ok(me);
		}
	}
	
	@At("/delivery/save")
	@POST
	public Object saveDelivery(@Param("name") String name, 
			@Param("phone") String phone, @Param("address") String address,
			@Param("city") String city, @Param("location") String location,
			@Param("lat") float lat, @Param("lng") float lng,
			HttpSession session) throws Exception{
		int id = this.userManager.saveDelivery(name,phone,address,city,location,lat,lng);
		if(id<0){
			return err("save delivery failed");
		}else{
			return ok(new NutMap().setv("id", id));
		}
	}
	
}
