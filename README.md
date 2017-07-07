# ETL脚本执行backend后端应用

# 编译
	
	mvn compile 

# 打包

	mvn package
	
# 测试
	
	mvn test

# 运行

	mvn tomcat7:run
	
# Deploy
      modify db.properties to use password in remote server.
	mvn package
	visit /manager to stop and undeploy the app 
	scp target/backend.war root@huolihuoshan.com:/var/lib/tomcat7/webapps/
	use /manager to start the app