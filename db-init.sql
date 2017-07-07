
/*
确保
/etc/mysql/my.cnf文件的[mysql]段中有以下配置：
default-character-set=utf8
以避免中文乱码
*/

/*
DROP DATABASE IF EXISTS hlhs;
*/
CREATE DATABASE hlhs CHARACTER SET utf8;
CREATE USER 'hlhs'@'localhost' IDENTIFIED BY 'password@hlhs';
GRANT ALL PRIVILEGES ON hlhs.* TO 'hlhs'@'localhost';
