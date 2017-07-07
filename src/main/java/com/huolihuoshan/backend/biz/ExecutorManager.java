package com.huolihuoshan.backend.biz;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

@IocBean(singleton = true)
public class ExecutorManager {

	private final Log LOG = Logs.getLog(this.getClass());

	@Inject
	protected Dao dao;


}
