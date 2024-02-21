# Docker 代码上线实践

## docker 部署 gitlab

gitlab 基础操作 [TP](https://www.xiaoyuanwiki.com/?p=240)

```bash
# 在本机准备gitlab工作目录
mkdir -p /data/docker/gitlab/{config,data,logs}
以上在本机建立的3个目录是为了gitlab容器通过挂载本机目录启动后可以映射配置文件，数据文件，日志文件到本机，然后后续就可以直接在本机查看和编辑了，不用再进容器操作。
 
# 启动gitlab
docker run \
--name gitlab \
--hostname gitlab \
--restart always \
-p 4443:443 -p 8888:80 -p 2222:22 \
-v /data/docker/gitlab/config:/etc/gitlab \
-v /data/docker/gitlab/data:/var/opt/gitlab \
-v /data/docker/gitlab/logs:/var/log/gitlab \
-d gitlab/gitlab-ce:latest 
 
# 查看密码
[root@docker01 ~]# cat /data/docker/gitlab/config/initial_root_password
CetMIMU29ScEIGHM1jshlVLjKGHKdS+uZGt84VyC1hI=
```



![image-20230911193356533](https://www.xiaoyuanwiki.com/image/image-20230911193356533.png)



### 修改 git 拉代码地址

```bash
# 进入容器
[root@docker01 docker]# docker exec -it 97aaad08 bash
 
# 修改配置文件(IP)
## external_url配置项增加IP地址和端口配置
##端口是80，即容器里面的地址，而不是宿主机外面的8888，因此external_url中的端口80也可以不写
root@gitlab:/# cd /etc/gitlab/
root@gitlab:/etc/gitlab# vi gitlab.rb 
##! https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-data-retrieval.html
external_url 'http://10.0.0.101'
 
# 修改配置文件(代码地址)
root@gitlab:/etc/gitlab# vi gitlab.rb
gitlab_rails['gitlab_ssh_host'] = '10.0.0.101'    
## 这条需要自己添加
gitlab_rails['gitlab_shell_ssh_port'] = '2222'
 
# 在容器里面重新加载配置
gitlab-ctl reconfigure
 
# 重启服务
gitlab-ctl restarth　
 
# SSH
ssh://git@10.0.0.101:2222/root/hello-word.git
 
# HTTP
http://10.0.0.101/root/hello-word.git
```



![image-20230912165228511](https://www.xiaoyuanwiki.com/image/image-20230912165228511.png)



### 修改 gitlab 密码



![image-20230912175759591](https://www.xiaoyuanwiki.com/image/image-20230912175759591.png)



## 使用 docker 部署 jenkins

```bash
docker run \
--name jenkins \
-p 8080:8080 \
-p 50000:50000 \
--user=root \
--privileged=true \
-v /root/.ssh:/root/.ssh \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /usr/bin/docker:/usr/bin/docker \
-v /home/jenkins_home:/var/jenkins_home \
-v /etc/localtime:/etc/localtime \
-d jenkins/jenkins
 
[root@docker02 ~]# docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
437a889d68894b4b8424a6a76e5914f2
```



![image-20230911194009806](https://www.xiaoyuanwiki.com/image/image-20230911194009806.png)





![image-20230911194919271](https://www.xiaoyuanwiki.com/image/image-20230911194919271.png)



### 在容器内生成 ssh 公钥并与宿主机进行免密

```bash
# 进入容器
[root@docker01 docker]# docker exec -it jenkins bash
 
# 生成密钥
root@363c6d9d7d4e:/# ssh-keygen
 
# 查看密钥
root@363c6d9d7d4e:/# ls ~/.ssh
id_rsa  id_rsa.pub
 
# 查看公钥
root@363c6d9d7d4e:/# cat ~/.ssh/id_rsa.pub 
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCSfXXEIA/punotkFKfgzeSTG/jgsjw/0YKQbsYvI8WBhWW4XcL17r3MAs4xjiQBzAVazHAQ6UM55BkWxgvpNh9dSjYMrcqoXx9SNPVSsBcNmGCr7/t0+NyJQPhoF4KULiLQMKIAxYZMZnIqWx+CWjbedCUY+eX0FWGusukZBycQoDJAPIxUGLoN2mgza9rudDlLB7vruCG2VanFGYECyCgFMBGow87niYFSrgiNBQ5X4GAtEhIfCZHpupnHB4NXT35DHMVBdQG9gh8+gbXqy4TlSBXGq7LbufP1ImXT36lr4gtu3r6MGPFZ2JurmM3SzrHA3T+Em3y1/9THjOFojigj7K0/49GnFCkVCeLGkXoi1BRl11BWVIfn7EOtbBtJxRubh/MI05/x50SMU0irD6k1wkZc89x85WA23A4fRWiImsZyI2Q+xsh93Uy2jcglcqHP2bTYfOU66MchMnFR6zAJrPI3TQQ8OEgUrLg3Y+2HgubjHn2ejxDJkzQLD/HKE0= root@72b2dd4ead18
 
# 与宿主机进行免密
root@363c6d9d7d4e:/# ssh-copy-id -i ~/.ssh/id_rsa.pub root@10.0.0.101
 
# 尝试连接宿主机
root@363c6d9d7d4e:/# ssh root@10.0.0.101
输入 yes
```

### gitlab 配置公钥



![image-20230912211847272](https://www.xiaoyuanwiki.com/image/image-20230912211847272.png)



### jenkins 下拉代码

```bash
root@363c6d9d7d4e:/# git clone ssh://git@10.0.0.101:2222/root/code.git
Cloning into 'code'...
remote: Enumerating objects: 20, done.
remote: Counting objects: 100% (20/20), done.
remote: Compressing objects: 100% (19/19), done.
remote: Total 20 (delta 4), reused 0 (delta 0), pack-reused 0
Receiving objects: 100% (20/20), done.
Resolving deltas: 100% (4/4), done.
 
root@363c6d9d7d4e:/code# ls -l
total 16
-rw-r--r-- 1 root root 363 Sep 12 20:06 index.html
-rw-r--r-- 1 root root 227 Sep 12 20:06 main.js
-rw-r--r-- 1 root root 220 Sep 12 20:06 src.js
-rw-r--r-- 1 root root 928 Sep 12 20:06 style.css
```

### 配置 jenkins 自由风格



![image-20230912211633034](https://www.xiaoyuanwiki.com/image/image-20230912211633034.png)





![image-20230912211642735](https://www.xiaoyuanwiki.com/image/image-20230912211642735.png)





![image-20230912211650746](https://www.xiaoyuanwiki.com/image/image-20230912211650746.png)



```bash
cd $WORKSPACE
cat > Dockerfile << EOF
FROM nginx:alpine 
COPY index.html  main.js  style.css /usr/share/nginx/html/ 
EOF
docker build -t web:v1 .
docker save web:v1 > /tmp/web.tgz
scp /tmp/web.tgz root@172.16.1.101:/tmp/
ssh 172.16.1.101 docker load < /tmp/web.tgz
ssh 172.16.1.101 docker run -d -p 81:80 --name web web:v1
```



![image-20230912211914984](https://www.xiaoyuanwiki.com/image/image-20230912211914984.png)





![image-20230912211944753](https://www.xiaoyuanwiki.com/image/image-20230912211944753.png)



### 访问网页

10.0.0.101:81



![image-20230912220300772](https://www.xiaoyuanwiki.com/image/image-20230912220300772.png)



### jenkins 结合 Harbor 上传代码

```bash
# 进入Jenkins容器
[root@docker01 ~]# docker exec -it jenkins bash
 
# 下拉代码
root@363c6d9d7d4e:/# git clone ssh://git@10.0.0.101:2222/root/code.git
 
# 修改代码
root@363c6d9d7d4e:/# cd code/
root@363c6d9d7d4e:/code# ls -l
total 12
-rw-r--r-- 1 root root 363 Sep 13 18:53 index.html
-rw-r--r-- 1 root root 227 Sep 13 18:53 main.js
-rw-r--r-- 1 root root  65 Sep 13 18:53 style.css
root@363c6d9d7d4e:/code# cat > style.css << EOF
> body{                 
  background-color: pink;
}
#demo2{
    margin-top: 50px;
}
> EOF
 
# 添加一些信息
root@363c6d9d7d4e:/code# git config --global user.email "you@example.com"
root@363c6d9d7d4e:/code# git config --global user.name "Your Name"
 
# 上传代码
root@363c6d9d7d4e:/code# git add .
root@363c6d9d7d4e:/code# git commit -m 'v1'
root@363c6d9d7d4e:/code# git tag -a 'v1' -m 'v1'
root@363c6d9d7d4e:/code# git push --all
root@363c6d9d7d4e:/code# git push --tag
```

#### 配置 jenkins 自由风格

打开参数化构建并输入 tag



![image-20230913192030819](https://www.xiaoyuanwiki.com/image/image-20230913192030819.png)



与上方设置相同



![image-20230913192105939](https://www.xiaoyuanwiki.com/image/image-20230913192105939.png)



**重点：修改之前脚本内容**



![image-20230913192158772](https://www.xiaoyuanwiki.com/image/image-20230913192158772.png)



```bash
cd $WORKSPACE
cat > Dockerfile << EOF
FROM nginx:alpine 
COPY index.html  main.js  style.css /usr/share/nginx/html/ 
EOF
docker build -t web:$tag .
docker login -u admin -p 123 10.0.0.100
docker tag web:$tag 10.0.0.100/wordpress/web:$tag
docker push 10.0.0.100/wordpress/web:$tag
 
---------------## 永久登录harbor方法----------------
docker login -u admin -p 123 10.0.0.100
```



![image-20230913192442819](https://www.xiaoyuanwiki.com/image/image-20230913192442819.png)



#### docker02 下拉代码试验



![image-20230913192500610](https://www.xiaoyuanwiki.com/image/image-20230913192500610.png)



```bash
# 下拉代码
[root@docker02 ~]# docker pull 10.0.0.100/wordpress/web:v1
v1: Pulling from wordpress/web
59bf1c3509f3: Already exists 
f3322597df46: Already exists 
d09cf91cabdc: Already exists 
3a97535ac2ef: Already exists 
919ade35f869: Already exists 
40e5d2fe5bcd: Already exists 
3e1d39353b26: Pull complete 
Digest: sha256:0700cf62a22a287c0329fb907feb45ebff303ed2ad34ae7eaa19acc6f17e0038
Status: Downloaded newer image for 10.0.0.100/wordpress/web:v1
 
# 启动容器
[root@docker02 ~]# docker run -d -p 81:80 --name web 10.0.0.100/wordpress/web:v1
```



![image-20230913192421345](https://www.xiaoyuanwiki.com/image/image-20230913192421345.png)



赞赏