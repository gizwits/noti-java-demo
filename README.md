gizwits_noti_demo
==================
v0.1.0

Gizwits noti client demo.

## Change log: v0.1.0 (2016/08)
* First version 


## 修改配置
* log config: `src/main/resources/log4j2.xml`
* 修改代码里main方法传入的eid，esecret

## maven编译 & 运行
* 打包代码和库，在target文件下生成`gizwits-noti-demo-0.1.0-jar-with-dependencies.jar`
    * `mvn clean package`
* 运行demo，打印接受到的设备消息
    * `java -jar target/gizwits-noti-demo-0.1.0-jar-with-dependencies.jar`
* 与eid关联的pk下的设备，上下线，即可查看设备消息。在当前logs文件下可查看日志

## ant编译 & 运行
* 打包代码和库，在build文件下生成`gizwitsi-noti-demo-0.1.0.jar`
    * `ant dist`
* 运行demo，打印接受到的设备消息
    * `java -jar build/gizwitsi-noti-demo-0.1.0.jar`
* 与eid关联的pk下的设备，上下线，即可查看设备消息。在当前logs文件下可查看日志
