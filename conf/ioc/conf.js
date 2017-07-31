var ioc = {
	// 读取配置文件
	conf : {
		type : "org.nutz.ioc.impl.PropertiesProxy",
		fields : {
			paths : [ 
			          "custom/misc.properties",
			          "custom/wechat.properties",
			          "custom/wechat.login.properties",
			          "custom/wechat.pay.properties",
			        ]
		}
	}
};
