gizwits_noti_demo
==================
v0.1.0

Gizwits noti client demo.

## developing

## configuration
* log config: `src/mian/resources/log4j2.xml`

## Change log: v0.1.0 (2016/08)
* First version 

## 编译
* mvn compile
* ant compile

## 运行
* 修改代码里main方法传入的eid，esecret
* 打包代码和库，在target文件下生成`gizwits_noti_demo-0.1.0-jar-with-dependencies.jar`
    * `mvn clean package`
* 运行demo，打印接受到的设备消息
    * `java -jar target/gizwits_noti_demo-0.1.0-jar-with-dependencies.jar`
* 与eid关联的pk下的设备，上下线，即可查看设备消息    
     