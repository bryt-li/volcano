package com.huolihuoshan.backend;

import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

import com.huolihuoshan.backend.bean.User;

@IocBean
public class EntrySetup implements Setup {
	private final Log LOG = Logs.getLog(this.getClass());

	public void init(NutConfig conf) {
		Ioc ioc = conf.getIoc();
		Dao dao = ioc.get(Dao.class);

		Daos.createTablesInPackage(dao, "com.huolihuoshan.backend.bean", true);

		// 初始化用户
/*
		if (dao.count(User.class) == 0) {
			User user = new User();
			user.setEmail("admin@huolihuoshan.com");
			user.setPassword("admin");
			user.setName("Lixin");
			dao.insert(user);
			LOG.debug("Create default user. email='admin@huolihuoshan.com' password='admin'");
		}
*/
	}

	public void destroy(NutConfig conf) {

	}

}
