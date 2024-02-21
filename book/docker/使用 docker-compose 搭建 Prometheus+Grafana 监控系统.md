# 使用 docker-compose 搭建 Prometheus+Grafana 监控系统

以前所在的公司监控系统主要使用的 zabbix, 应用级的监控使用的是 Pinpoint 的二次开发，所以对于 Prometheus 的了解还只是听闻，尽管很想使用，但是公司的 zabbix 已经非常成熟，包括后来小米的 open-falcon 也是没有机会去使用。直到今天我才想着要不测试环境的服务都用 Prometheus 吧，因现在的公司并没有监控系统，服务挂了以后都是使用的时候才会发现。我个人是比较喜欢 docker 的，于是就记录分享一下。

## 环境准备

| 主机     | IP         | 角色             | 软件                                         |
| -------- | ---------- | ---------------- | -------------------------------------------- |
| docker01 | 10.0.0.101 | 普罗米修斯服务端 | Prometheus、node-exporter、cadvisor、Grafana |
| docker02 | 10.0.0.102 | 普罗米修斯客户端 | node-exporter、cadvisor                      |

## 角色分配

- Prometheus 采集数据
- Grafana 用于图表展示
- node-exporter 用于收集操作系统和硬件信息的 metrics
- cadvisor 用于收集 docker 的相关 metrics

## 安装 Docker

我通常使用如下命令安装最新版的 docker

```bash
wget -O /etc/yum.repos.d/docker-ce.repo https://download.docker.com/linux/centos/docker-ce.repo && yum install -y docker-ce && systemctl enable docker.service && service docker start   
```

## 安装 Docker-Compose

可以使用如下命令安装最新版的 docker-compose

```bash
curl -L https://github.com/docker/compose/releases/download/1.24.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

在安装过程中下载时间需要 40 分钟，仅仅 15M 的东西，所以可以通过如下方式手动下载：

```bash
echo "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-`uname -s`-`uname -m`"   
```

输出如下：

```bash
https://github.com/docker/compose/releases/download/1.24.1/docker-compose-Linux-x86_64   
```

点击下载，完成后上传到服务器上，然后执行如下命令：

```bash
mv docker-compose-Linux-x86_64 /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

## 部署 Prometheus 和 Grafana

### 新增 Prometheus 配置文件 (docker01)

首先，创建 `/data/prometheus/` 目录，然后创建 `prometheus.yml`，填入如下内容：

```bash
global:
  scrape_interval:     15s
  evaluation_interval: 15s
 
alerting:
  alertmanagers:
  - static_configs:
    - targets: ['10.0.0.101:9093']
 
rule_files:
  - "node_down.yml"
 
scrape_configs:
 
  - job_name: 'prometheus'
    static_configs:
    - targets: ['10.0.0.101:9090']
 
  - job_name: 'node'
    scrape_interval: 8s
    static_configs:
    - targets: ['10.0.0.101:9100', '10.0.0.102:9100']
 
  - job_name: 'cadvisor'
    scrape_interval: 8s
    static_configs:
    - targets: ['10.0.0.101:8088', '10.0.0.102:8088']
```

接着进行创建 `node_down.yml`, 添加如下内容：

```bash
groups:
- name: node_down
  rules:
  - alert: InstanceDown
    expr: up == 0
    for: 1m
    labels:
      user: test
    annotations:
      summary: "Instance {{ $labels.instance }} down"
      description: "{{ $labels.instance }} of job {{ $labels.job }} has been down for more than 1 minutes."
```

### 创建服务端的 docker-compose (docker01)

继续在 docker01 中 `/data/prometheus/` 目录中创建 `docker-compose-prometheus.yml`, 添加如下内容：

```bash
version: '2'
 
networks:
    monitor:
        driver: bridge
 
services:
    prometheus:
        image: prom/prometheus
        container_name: prometheus
        hostname: prometheus
        restart: always
        volumes:
            - /data/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
            - /data/prometheus/node_down.yml:/etc/prometheus/node_down.yml
        ports:
            - "9090:9090"
        networks:
            - monitor
 
    grafana:
        image: grafana/grafana
        container_name: grafana
        hostname: grafana
        restart: always
        ports:
            - "3000:3000"
        networks:
            - monitor
 
    node-exporter:
        image: quay.io/prometheus/node-exporter
        container_name: node-exporter
        hostname: node-exporter
        restart: always
        ports:
            - "9100:9100"
        networks:
            - monitor
 
    cadvisor:
        image: google/cadvisor:latest
        container_name: cadvisor
        hostname: cadvisor
        restart: always
        volumes:
            - /:/rootfs:ro
            - /var/run:/var/run:rw
            - /sys:/sys:ro
            - /var/lib/docker/:/var/lib/docker:ro
        ports:
            - "8088:8080"
        networks:
            - monitor
```

### 创建客户端的 docker-compose (docker02)

继续在 `/data/prometheus/` 目录中创建 `docker-compose.yml`, 添加如下内容：

```bash
version: '2'
 
networks:
    monitor:
        driver: bridge
 
services:
    node-exporter:
        image: quay.io/prometheus/node-exporter
        container_name: node-exporter
        hostname: node-exporter
        restart: always
        ports:
            - "9100:9100"
        networks:
            - monitor
 
    cadvisor:
        image: google/cadvisor:latest
        container_name: cadvisor
        hostname: cadvisor
        restart: always
        volumes:
            - /:/rootfs:ro
            - /var/run:/var/run:rw
            - /sys:/sys:ro
            - /var/lib/docker/:/var/lib/docker:ro
        ports:
            - "8088:8080"
        networks:
            - monitor
```

## 启动 docker-compose

使用下面的命令启动 docker-compose 定义的容器

```bash
# docker01
docker-compose -f /data/prometheus/docker-compose-prometheus.yml up -d
 
# docker02
docker-compose up -d
```

输入如下内容即代表启动成功：

```java
Creating network "prometheus_monitor" with driver "bridge"
Creating cadvisor       ... done
Creating prometheus     ... done
Creating node-exporter  ... done
Creating redis_exporter ... done
Creating grafana        ... done
```

也可通过 `docker ps` 命令查看是否启动成功。如果要关闭并删除以上 5 个容器，只需要执行如下命令即可：

```java
docker-compose -f /data/prometheus/docker-compose-prometheus.yml down   
```

同样也会输出如下日志：

```java
Stopping cadvisor       ... done
Stopping node-exporter  ... done
Stopping grafana        ... done
Stopping redis_exporter ... done
Stopping prometheus     ... done
Removing cadvisor       ... done
Removing node-exporter  ... done
Removing grafana        ... done
Removing redis_exporter ... done
Removing prometheus     ... done
Removing network prometheus_monitor
```

打开 http://10.0.0.101:9090/targets，如果 State 都是 UP 即代表 Prometheus 工作正常，如下图所示：



![image-20230915182707336](http://www.xiaoyuanwiki.com/image/image-20230915182707336.png)



当然，我第一次安装时只有一个是 UP 的，造成这个问题的主要原因是因为 CentOS7 的防火墙 firewall 导致的，将对应的端口添加到防火墙策略里即可：

```java
firewall-cmd --zone=public --add-port=9100/tcp --permanent
firewall-cmd --zone=public --add-port=8088/tcp --permanent
firewall-cmd --zone=public --add-port=9121/tcp --permanent
firewall-cmd --zone=public --add-port=3000/tcp --permanent
firewall-cmd --zone=public --add-port=9090/tcp --permanent
firewall-cmd --reload
```

可通过如下命令查看端口策略是否已经生效

```java
firewall-cmd --permanent --zone=public --list-ports   
```

## 配置 Grafana

打开 http://10.0.101:3000 使用默认账号密码 admin/admin 登录并修改密码后，默认进来是创建数据库的页面，在如下图所示中，选择 Prometheus。

修改密码为 yl1234



![image-20230915165109287](http://www.xiaoyuanwiki.com/image/image-20230915165109287.png)





![image-20230915165129333](http://www.xiaoyuanwiki.com/image/image-20230915165129333.png)



选择完成后，打开新的页面，在 HTTP 的 URL 中输入 Prometheus 的地址 http://10.0.0.101:9090 并且将 Access 由 Server 更换为 Browser，因为跨域的问题 Server 无法使用。点击保存并测试。



![image-20230915165318999](http://www.xiaoyuanwiki.com/image/image-20230915165318999.png)

现在数据源已经接通了，只剩下漂亮的报表了，在 `Dashboard`模版，并将其 json 文件下载下来。

在 Grafana 菜单栏中第一个 + 号中，选择 `import`



![image-20230915191153705](http://www.xiaoyuanwiki.com/image/image-20230915191153705.png)



推荐使用 1860、12633、893

**本次使用的是 1860 与 893**



![image-20230915191216688](http://www.xiaoyuanwiki.com/image/image-20230915191216688.png)





![image-20230915191305719](http://www.xiaoyuanwiki.com/image/image-20230915191305719.png)



将其上传后，选择 prom 为 `Prometheus` 也就是配置 `Prometheus` 是的 `Name` 的值，点击保存即可。等待一会儿就会出现如下界面：就说明成功了。



![image-20230915191013572](http://www.xiaoyuanwiki.com/image/image-20230915191013572.png)





![image-20230915191515935](http://www.xiaoyuanwiki.com/image/image-20230915191515935.png)



