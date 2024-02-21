# 使用 Rancher 部署管理 K8S 集群

### 主机规划

| 主机名称 | 角色             | IP 地址    | 基础软件  |
| -------- | ---------------- | ---------- | --------- |
| rancher  | 管理 k8s 集群    | 10.0.0.203 | docker-ce |
| master   | k8s 集群主节点   | 10.0.0.200 | docker-ce |
| node1    | k8s 集群从节点 1 | 10.0.0.201 | docker-ce |
| node2    | k8s 集群从节点 2 | 10.0.0.202 | docker-ce |

其余三台机器的 k8s 基础安装参考 [TP](https://www.xiaoyuanwiki.com/?p=412)

## 部署 docker-ce（rancher）

```bash
yum install -y yum-utils device-mapper-persistent-data lvm2
 
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
 
yum makecache fast
 
yum -y install docker-ce
 
systemctl enable docker
 
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://yours.mirror.aliyuncs.com"]
}
EOF
systemctl daemon-reload
systemctl restart docker
```

## 部署 rancher

```bash
[root@rancher ~]# docker run -d --privileged  --restart=unless-stopped -p 80:80 -p 443:443 rancher/rancher   
```

使用火狐浏览器访问 10.0.0.203



![image-20230919164723961](https://www.xiaoyuanwiki.com/image/image-20230919164723961.png)



```bash
# 查看密码
[root@rancher ~]# docker logs 21d4f68d1b00   2>&1 | grep "Bootstrap Password:"
2023/09/19 08:47:00 [INFO] Bootstrap Password: rdq8vhw7sdbnhwk9r7cvw5x89vlq9gkt6gf9pwbv8nrkbk6dvq58xn
```



![image-20230919165154215](https://www.xiaoyuanwiki.com/image/image-20230919165154215.png)



设置中文



![image-20230919165823333](https://www.xiaoyuanwiki.com/image/image-20230919165823333.png)



创建集群



![image-20230919171512213](https://www.xiaoyuanwiki.com/image/image-20230919171512213.png)





![image-20230919171532059](https://www.xiaoyuanwiki.com/image/image-20230919171532059.png)



```bash
[root@master ~]# curl --insecure -sfL https://10.0.0.203/v3/import/zpjf8npjp8qpbj45mzz8zplcdfzkk2zwskcd47th982bmzswh5jnv8_c-m-m8s758hr.yaml | kubectl apply -f -
clusterrole.rbac.authorization.k8s.io/proxy-clusterrole-kubeapiserver unchanged
clusterrolebinding.rbac.authorization.k8s.io/proxy-role-binding-kubernetes-master unchanged
namespace/cattle-system unchanged
serviceaccount/cattle unchanged
clusterrolebinding.rbac.authorization.k8s.io/cattle-admin-binding unchanged
secret/cattle-credentials-e7121b5 created
clusterrole.rbac.authorization.k8s.io/cattle-admin unchanged
deployment.apps/cattle-cluster-agent configured
service/cattle-cluster-agent unchanged
```



![image-20230919173445746](https://www.xiaoyuanwiki.com/image/image-20230919173445746.png)



