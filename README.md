# Rcon插件

本项目是[Mirai](https://github.com/mamoe/mirai)机器人的插件  
使用[RCON](https://wiki.vg/RCON)协议与Minecraft服务器通信,并管理服务器  
支持在不同QQ群内管理不同的Minecraft服务器

## 插件用法

### 安装

下载[Release](https://github.com/SkipM4/Mirai-mc-rcon/releases)中的jar文件  
复制到Mirai的plugins文件夹中  
重载或重启Mirai以加载本插件

### Minecraft服务器设置

修改`server.properties`中如下三行：

```
enable-rcon=true
rcon.password=
rcon.port=25575
```

将`enable-rcon`后填入`true`以启用RCON功能  
设置密码`rcon.password`  
rcon端口`rcon.port`

### 插件设置

配置文件中各项说明如下：

```yaml
# 命令前缀 可根据需求与喜好填写
prefixes:
- \
- 'sudo '

# Minecraft服务器地址
serverAddr: '127.0.0.1'

# Minecraft服务器RCON协议的端口
serverPort: 25575

# RCON密码
password: 'passwd'

# 启用的QQ群号
groupID: 12345678

# 有权限执行RCON命令的群成员QQ号
canPerform:
- 123456
```

### 使用

在指令前加上`prefixes`中的任意一项并发送  
例如`\list`或者`sudo list`以显示在线玩家列表