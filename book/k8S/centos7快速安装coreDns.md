#  centos7快速安装coreDns

# centos7 快速安装 coreDns

## 1、下载二进制文件

```bash
wget https://github.com/coredns/coredns/releases/download/v1.5.0/coredns_1.5.0_linux_amd64.tgz
tar zxf coredns_1.5.0_linux_amd64.tgz -C /usr/bin/
```

## 2、创建用户

```bash
useradd coredns -s /sbin/nologin   
```

## 3、编辑 /etc/coredns/Corefile

```bash
[root@master ~]# mkdir /etc/coredns/
[root@master ~]# vim /etc/coredns/Corefile
.:53 {
  # 绑定interface ip
  bind 127.0.0.1
  # 先走本机的hosts
  # https://coredns.io/plugins/hosts/
  hosts {
    # 自定义sms.service search.service 的解析
    # 因为解析的域名少我们这里直接用hosts插件即可完成需求
    # 如果有大量自定义域名解析那么建议用file插件使用 符合RFC 1035规范的DNS解析配置文件
    10.0.0.201 sms.service
    10.0.0.202 search.service
    # ttl
    ttl 60
    # 重载hosts配置
    reload 1m
    # 继续执行
    fallthrough
  }
  # file enables serving zone data from an RFC 1035-style master file.
  # https://coredns.io/plugins/file/
  # file service.signed service
  # 最后所有的都转发到系统配置的上游dns服务器去解析
  forward . /etc/resolv.conf
  # 缓存时间ttl
  cache 120
  # 自动加载配置文件的间隔时间
  reload 6s
  # 输出日志
  log
  # 输出错误
  errors
}
```

## 4、编辑 /usr/lib/systemd/system/coredns.service

```bash
[root@master coredns]# vim /usr/lib/systemd/system/coredns.service
[Unit]
Description=CoreDNS DNS server
Documentation=https://coredns.io
After=network.target
 
[Service]
PermissionsStartOnly=true
LimitNOFILE=1048576
LimitNPROC=512
CapabilityBoundingSet=CAP_NET_BIND_SERVICE
AmbientCapabilities=CAP_NET_BIND_SERVICE
NoNewPrivileges=true
User=coredns
ExecStart=/usr/bin/coredns -conf=/etc/coredns/Corefile
ExecReload=/bin/kill -SIGUSR1 $MAINPID
Restart=on-failure
 
[Install]
WantedBy=multi-user.target
```

## 5、启动 coredns

```bash
systemctl enable coredns
systemctl start coredns
systemctl status coredns
```

## 6、测试

### 创建一个简单的 centos,busybox

```bash
[root@master yml_files]# cat centos.yaml
apiVersion: v1
kind: Pod
metadata:
  name: centoschao
  namespace: default
spec:
  containers:
  - image: centos
    command:
      - sleep
      - "3600"
    imagePullPolicy: IfNotPresent
    name: centoschao
  restartPolicy: Always
```

### 创建：

```css
kubectl create -f centos.yaml   
```

### 测试：

```bash
[root@master ~]# kubectl exec -it centoschao -- bash
[root@centoschao /]# bash <(curl -sSL https://linuxmirrors.cn/main.sh)
[root@centoschao /]# nslookup kubernetes
Server:         10.1.0.10
Address:        10.1.0.10#53
 
Name:   kubernetes.default.svc.cluster.local
Address: 10.1.0.1
```