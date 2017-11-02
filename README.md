# Volcano: HLHS Java Web Backend Application

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
modify pom.xml version number      
	
	mvn tomcat7:undeploy
	mvn tomcat7:deploy

# Tomcat7-Admin

visit /manager/ to stop and undeploy the app 
	
	scp target/volcano.war root@huolihuoshan.com:/var/lib/tomcat7/webapps/

use /manager/ to start the app

# Druid

use /volcano/druid/ to view Druid report
