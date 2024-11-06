# mypan

### 项目介绍
本项目为一个简单的springboot网盘项目,支持分片上传和下载,当前为后端服务器代码,前端请到https://github.com/zzk-sir/mypan-

### 项目展示
http://www.zsir.site:1024

- [ ]  如果遇到问题，请联系我，2098755297@qq.com


### 项目主要技术栈
* springboot,mysql,mybatis-plus,redis,rabbitmq,ffmpeg

### 相关环境配置
* 操作系统 windows系统[(linux系统兼容)](#linux)
* JDK 18.0.2
* MySQL 5.7.44 创建数据库名mypan,然后运行src/main/resource/Mypan1.sql
* Redis 7.2.4
* ffmpeg 下载后解压的文件夹配置环境变量（系统变量Path添加这个ffmpeg文件下载的路径，
如:你下载在D:\ffmpeg文件夹下,就添加值D:\ffmpeg\ffmpeg-2023-04-17-git-65e537b833-full_build\bin）


### 相关资源下载
* ffmpeg(windows)
  链接：https://pan.quark.cn/s/92351a4351bb
  提取码：9Did
* <span id="linux">ffmpeg(linux)</span>
  *  1、启用EPEL存储库
  ```shell
  yum install epel-release
  ```
  * 2、安装rpm软件包来启用Nux存储库
  ```shell
  rpm -v --import http://li.nux.ro/download/nux/RPM-GPG-KEY-nux.ro
  rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm
  ```
  * 3、安装FFmpeg
  ```shell
  yum install ffmpeg ffmpeg-devel
  ```
  * 4、测试
  ```shell
  ffmpeg -version
  ffprobe -version
  ```
### bug 修复
* 修复了分享数据获取错误
* 优化邮箱验证码生成