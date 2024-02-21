# DockerFile 部署 wordpress 实践

## 部署 wordpress

```bash
# 创建dockerfile目录
[root@docker02 ~]# mkdir /Dockerfile
 
# 进入dockerfile目录
[root@docker02 ~]# cd /Dockerfile
 
# 编写Docker File
[root@docker02 Dockerfile]# vim Dockerfile 
FROM centos:7
 
# 安装依赖库和工具
RUN yum -y update && \
    yum -y install epel-release && \
    yum -y install wget && \
    yum -y install tar && \
    yum -y install openssl && \
    yum -y install numactl-libs && \
    yum -y install libaio && \
    yum -y install perl && \
    yum clean all
 
WORKDIR /app
# 下载并安装 MySQL
COPY mysql-5.7.42-linux-glibc2.12-x86_64.tar.gz /app/mysql-5.7.42-linux-glibc2.12-x86_64.tar.gz
RUN tar xf mysql-5.7.42-linux-glibc2.12-x86_64.tar.gz && \
    useradd mysql -s /sbin/nologin -M && \
    mv mysql-5.7.42-linux-glibc2.12-x86_64 /app/mysql-5.7.42 && \
    rm -f mysql-5.7.42-linux-glibc2.12-x86_64.tar.gz && \
    ln -s /app/mysql-5.7.42 /app/mysql && \
    chown mysql.mysql -R /app/mysql* && \
    cp /app/mysql/support-files/mysql.server /etc/init.d/mysqld && \
    cd /app/mysql/bin/ && \
    ./mysqld --initialize-insecure --user=mysql --basedir=/app/mysql --datadir=/app/mysql/data
 
# 部署wordpress
RUN mkdir /code && \
    yum install -y nginx php-fpm php php-mysql && \
    groupadd www -g 666 && \
    useradd www -u 666 -g 666 -s /sbin/nologin/ -M && \
    sed -i 's#user nginx#user www#' /etc/nginx/nginx.conf && \
    sed -i 's#user = apache#user = www#' /etc/php-fpm.d/www.conf && \
    sed -i 's#group = apache#group = www#' /etc/php-fpm.d/www.conf && \
    cd /code && \
    wget https://cn.wordpress.org/wordpress-5.0.3-zh_CN.tar.gz && \
    tar xf wordpress-5.0.3-zh_CN.tar.gz && \
    chown -R www.www /code/* 
 
# 复制wordpress配置文件
COPY wp.conf /etc/nginx/conf.d/
 
# 设置环境变量
ENV PATH=/app/mysql/bin:$PATH
ENV PHP_FPM_CONF_FILE=/etc/php-fpm.conf
ENV NGINX_CONF_FILE=/etc/nginx/nginx.conf
 
# 创建wordpress用户并复制 MySQL 配置文件
COPY my.cnf /etc/my.cnf
RUN /etc/init.d/mysqld start && \
    /app/mysql/bin/mysql -e "create database wp charset utf8;" && \
    /app/mysql/bin/mysql -e "grant all on wp.* to wp_user@'%' identified by '123';"
# 创建数据目录
VOLUME /app/mysql/data
 
# 暴露 MySQL 端口
EXPOSE 3306 80
 
# 启动服务
CMD service mysqld start && nginx -c $NGINX_CONF_FILE && php-fpm -c $PHP_FPM_CONF_FILE
```

## 准备 Dockerfile 所需文件

```bash
# mysql配置文件
[root@docker02 Dockerfile]# cat my.cnf 
[mysqld]
basedir=/app/mysql
datadir=/app/mysql/data
 
# wordpress数据文件
[root@docker02 Dockerfile]# cat wp.sql 
-- MySQL dump 10.13  Distrib 5.7.42, for linux-glibc2.12 (x86_64)
--
-- Host: localhost    Database: wp
-- ------------------------------------------------------
-- Server version       5.7.42
 
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
 
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
 
-- Dump completed on 2023-09-10  9:20:32
 
# wordpress的nginx配置文件
[root@docker02 Dockerfile]# cat wp.conf 
server{
        listen 80;
        server_name _;
        root /code/wordpress;
 
        location / {
                index index.php index.html;
        }
        location ~ \.php$ {
                fastcgi_pass 127.0.0.1:9000;
                fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
                include /etc/nginx/fastcgi_params;
        }
}
 
# 上传mysql-5.7.42安装包
mysql-5.7.42-linux-glibc2.12-x86_64.tar.gz
```

## 开始制作成镜像

```bash
# 使用docker build执行dockerfile并指定镜像名与标签，最后的点是当前目录
[root@docker02 Dockerfile]# docker build -t wordpress:1 .
 
# 使用docker run运行并进入容器
[root@docker02 Dockerfile]# docker run --rm --name wordpress -p 80:80 -it wordpress:1 bash
## 启动mysql
[root@d1fd726876f9 app]# /etc/init.d/mysqld start
## 启动nginx
[root@d1fd726876f9 app]# nginx -c /etc/nginx/nginx.conf
## 后台启动php-fpm
[root@d1fd726876f9 app]# php-fpm -c /etc/php-fpm.conf &
```

## 访问网页



![image-20230910193257756](https://www.xiaoyuanwiki.com/image/image-20230910193257756.png)





![image-20230910193356848](https://www.xiaoyuanwiki.com/image/image-20230910193356848.png)





![image-20230910193418634](https://www.xiaoyuanwiki.com/image/image-20230910193418634.png)





![image-20230910193443925](https://www.xiaoyuanwiki.com/image/image-20230910193443925.png)



## 将 wordpress 打包成镜像

```bash
# 将容器制作成镜像
[root@docker02 ~]# docker commit d1fd726876f9 wordpress:v1
 
# 导出镜像
[root@docker02 ~]# docker save wordpress:v1 > /opt/wordpress.tgz
 
# 拷贝镜像
[root@docker02 ~]# scp /opt/wordpress.tgz 172.16.1.101:/opt
 
# 导入镜像
[root@docker01 ~]# docker load < /opt/wordpress.tgz
 
# 启动
[root@docker01 ~]# docker run --rm --name wordpress -p 80:80 -it wordpress:v1 bash
## 启动mysql
[root@0e5546c39cb7 app]# /etc/init.d/mysqld start
## 启动nginx
[root@0e5546c39cb7 app]# nginx -c /etc/nginx/nginx.conf
## 后台启动php-fpm
[root@0e5546c39cb7 app]# php-fpm -c /etc/php-fpm.conf &
```



![image-20230910202700284](https://www.xiaoyuanwiki.com/image/image-20230910202700284.png)





![image-20230910202713800](https://www.xiaoyuanwiki.com/image/image-20230910202713800.png)





![image-20230910202727481](https://www.xiaoyuanwiki.com/image/image-20230910202727481.png)



