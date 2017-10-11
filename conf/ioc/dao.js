var ioc = {
	db : {
		type : "org.nutz.ioc.impl.PropertiesProxy",
		fields : {
			paths : [ "custom/db.properties" ]
		}
	},
	dataSource : {
		type : "com.alibaba.druid.pool.DruidDataSource",
		events : {
			create : "init",
			depose : 'close'
		},
		fields : {
			url : {
				java : "$db.get('db.url')"
			},
			username : {
				java : "$db.get('db.username')"
			},
			password : {
				java : "$db.get('db.password')"
			},
			testWhileIdle : true,
			validationQuery : {
				java : "$db.get('db.validationQuery')"
			},
			maxActive : {
				java : "$db.get('db.maxActive')"
			},
			filters : "mergeStat",
			connectionProperties : "druid.stat.slowSqlMillis=2000"
		}
	},
	activiti.dataSource : {
		type : "com.alibaba.druid.pool.DruidDataSource",
		events : {
			create : "init",
			depose : 'close'
		},
		fields : {
			url : {
				java : "$db.get('activiti.db.url')"
			},
			username : {
				java : "$db.get('db.username')"
			},
			password : {
				java : "$db.get('db.password')"
			},
			testWhileIdle : true,
			validationQuery : {
				java : "$db.get('db.validationQuery')"
			},
			maxActive : {
				java : "$db.get('db.maxActive')"
			},
			filters : "mergeStat",
			connectionProperties : "druid.stat.slowSqlMillis=2000"
		}
	},
	dao : {
		type : "org.nutz.dao.impl.NutDao",
		args : [ {
			refer : "dataSource"
		} ]
	}
};
