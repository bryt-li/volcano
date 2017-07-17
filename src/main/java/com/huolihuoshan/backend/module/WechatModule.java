package com.huolihuoshan.backend.module;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.weixin.spi.WxLogin;
import org.nutz.weixin.spi.WxResp;

import com.huolihuoshan.backend.bean.User;

@At("/wechat")
@IocBean
@Ok("json")
@Fail("http:500")
public class WechatModule extends BaseModule{
    
	@Inject("java:$wxLogin.configure($conf,null)")
	protected WxLogin wxLogin;
	
	////wecat/login?code=CODE&state=STATE
	@At
	public void login(String code, String state, HttpSession session, HttpServletResponse response) throws Exception{
		
		WxResp resp = wxLogin.access_token(code);
		if(resp.ok()){
			String openid = resp.getString("openid");
			String token = resp.getString("access_token");
			if(openid==null || token==null)
				throw new Exception();
			
			resp = wxLogin.userinfo(openid, token);
			if(resp.ok()){
				openid = resp.getString("openid");
				if(openid==null)
					throw new Exception();		
				
				String nickname = resp.getString("nickname");
				String sex = resp.getString("sex");
				String name = "未知";
				if(sex.equals("1"))
					name = "先生";
				if(sex.equals("2"))
					name = "女士";		
				String country = resp.getString("country");
				String province = resp.getString("province");
				String city = resp.getString("city");
				String headImageUrl = resp.getString("headimgurl");
				
				saveMe(openid, nickname, 
						sex, name, headImageUrl, 
						country, province, city);
			}
		}
		
		if(state==null||state.isEmpty())
			state = "/";
		response.sendRedirect(state);
	}
	
	@At
	public Object user() throws Exception{
		User me = this.getMe();
		if(me==null){
			/*this.saveMe("openid","lixin",
					"1","bryt","http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46",
					"china","hunan","changsha");
			*/
			return new NutMap().
					setv("ok", false).
					setv("err", "not sign in");
		}
		else{
			return new NutMap().
				setv("ok", true).
				setv("payload", Json.toJson(me));
		}
	}
}
