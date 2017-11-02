package com.huolihuoshan.volcano;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

import com.huolihuoshan.volcano.biz.OrderManager;

@IocBean
public class EntrySetup implements Setup {
	private final Log LOG = Logs.getLog(this.getClass());

	@Inject
	private OrderManager orderManager;
	
	public void init(NutConfig conf) {
		Ioc ioc = conf.getIoc();
		Dao dao = ioc.get(Dao.class);
        
		//=========================================================================
        // activiti ioc configuration
        ioc.get(ProcessEngine.class);
        RepositoryService repositoryService = ioc.get(RepositoryService.class);
        // Deploy the process definition
        repositoryService.createDeployment()
        .addClasspathResource("model.bpmn20.xml")
        .deploy();
        LOG.debug("BPMN process model is deployed.");
        //====================================================================

        
		Daos.createTablesInPackage(dao, "com.huolihuoshan.volcano.bean", false);

		try {
			this.orderManager.startup();
		} catch (Exception e) {
			LOG.fatal(e);
			//throw runtime exception to stop the webapp
			//but this behavior depends on container middleware
			throw new RuntimeException();
		}

	}

	public void destroy(NutConfig conf) {
		try {
			this.orderManager.shutdown();
		} catch (Exception e) {
			LOG.fatal(e);
			throw new RuntimeException();
		}
	}

}
