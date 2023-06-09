# 免责声明  

本软件是作者本人自用的。使用本软件并不会导致世界💥。就算真的因为使用本软件导致世界直接或间接地💥了，我也不会负责🥰。  

# 介绍  

## 功能  

+ 静默发短信，无需看系统脸色（绕过系统权限控制，实测MIUI 13/14 均可用）  
+ 支持远程连接（基于 WebSocket 技术实现，用于接收发送短信的任务）  
+ 支持选择默认发短信的SIM卡（卡槽可选）  
+ 实现短信功能的后台服务（其实是前台服务）的通知自动存档，可在 “通知日志” 界面查看昨天和今天内的通知流水  
+ 发完短信自动存档，可在“短信日志”界面查看昨天和今天内的短信流水。  
+ 内置 DNS 解析，使用 DoH 抗 DNS 劫持。海外域名随便使用。  

## 食用方法 & 开发指南  

+ 界面操作  
  + APP 界面的右下角有个始终显示的悬浮按钮，点击它可以弹出侧滑导航栏。  
    导航栏里可以点击选择您想要转跳的界面。  
  + 首页中央大圆圈按钮
    + 颜色状态说明
      + 红色：软件基础功能未全部正常工作。  
      + 黄色：软件尝试恢复基础功能。  
      + 绿色：软件基础功能全部工作良好。  
    + 点击逻辑
      + 无论何时，点击即重新创建 WebSocket ，包括重新登录鉴权。
+ 权限说明  
  + 启动 APP 进入软件后， 如果没有完整授予需要的所有权限，会自动转跳到 “权限管理” 界面，您需要授予全部权限才能使软件所有功能正常工作。  
+ 远程连接说明  
  + 本软件远程登录（WebSocket）功能的鉴权机制对接 [Sa-Token](https://github.com/dromara/Sa-Token) v1.34.0 版本（SpringBoot3 也可正常工作，其它版本未做测试）。  
  + 为启用远程连接功能，您需要在 “设置” 界面填上 “WS登录URL” 和 “口令”。  
    一旦这两项被设置（不是留空状态）软件将自动根据您的设置启动 WebSocket 连接（每次APP启动都会检查并自动连接）。  
  + WS登录URL  
    + 这必须是个正经的URL，必须是使用 HTTP 协议。这个 URL 将会用于远程登录鉴权以及 ***软件内部自动生成 WebSocket 连接使用的 URL （URL开头是ws）*** ！！！  
      + HTTP/HTTPS  
        + 如果您的 URL 使用 SSL 加密协议，那么软件内部自动生成 WebSocket URL 将会也使用 SSL 加密协议。也就是说：  
          + 如果您提供的 URL 是以 `http://` 开头，软件内部自动生成的 WebSocket URL 将会以 `ws://` 开头。  
          + 如果您提供的 URL 是以 `https://` 开头，软件内部自动生成的 WebSocket URL 将会以 `wss://` 开头。  
      + WebSocket URL  
        + 软件内部会自动根据您提供的登录 URL 使用 `Java` 里的 `java.net.URL#URL(java.net.URL, java.lang.String)` 生成 WebSocket URL ，转跳到相对路径 `./ws-connect` 。举例如下：
          + `http://www.sombody.xxx/aa/bb/cc/login`  
            生成的 WebSocket URL： `ws://www.sombody.xxx/aa/bb/cc/ws-connect`
          + `https://www.sombody.xxx/aa/bb/abcdefg-login-1234567`  
            生成的 WebSocket URL： `wss://www.sombody.xxx/aa/bb/ws-connect`
          + `https://www.sombody.xxx/aa/`  
            生成的 WebSocket URL： `wss://www.sombody.xxx/aa/ws-connect`
      + 自定义 WebSocket URL ？这个暂时没做成可修改的。什么时候安排？咕咕咕。。。（逃
  + 口令  
    + 这个 WebSocket 口令将会以浏览器默认的数据打包方式打包（ MIME 类型是 `application/x-www-form-urlencoded` ，键名是 `accessKeySecret` ） 使用 HTTP `POST` 方法向远端发送请求（用以鉴权）。远端鉴权框架务必是 [Sa-Token](https://github.com/dromara/Sa-Token) 或者您自己在远端实现完整的 [Sa-Token](https://github.com/dromara/Sa-Token) 鉴权机制。软件会自动保存鉴权 Cookie ，用以断线重连。  
+ 重连机制说明  
  + WebSocket 客户端参数  
    + 读超时/写超时：3秒  
    + Ping/Pong心跳：2秒  
    + 连接超时：6秒  
  + 自定义？这个暂时没做成可修改的。什么时候安排？咕咕咕。。。（逃  

# 写在最后  

这个项目代码质量呢……呃……就是一坨 ~~***sh\*t*** （消音）！~~  😅  
欢迎~~重做~~，提交 pull request ！！！  
