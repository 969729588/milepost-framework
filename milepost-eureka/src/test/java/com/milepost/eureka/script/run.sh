#!/bin/sh
JAR_NAME=milepost-eureka-1.0.0.100.jar
start() {
    echo "start process...";
    rm -rf ./logs
    
    #nohup java -Xmx1g -Xms1g \
    nohup java -Xmx256m -Xms256m \
    -jar ${JAR_NAME} \
    --spring.profiles.active=prod
    --discovery.server.address="http://192.168.55.46:8761/eureka/" \
    --eureka.instance.metadataMap.tenant=trident  \
    --server.host=192.168.55.46 \
    --server.port=60050 \
    --spring.profiles.active=prod \
    --spring.activemq.broker-url="failover:(tcp://192.168.55.46:61616)" \
    --spring.datasource.dataSourceClassName="com.mysql.jdbc.Driver" \
    --spring.datasource.url="jdbc:mysql://192.168.55.50:3306/sre?useUnicode=true&amp;characterEncoding=utf-8&autoReconnect=true" \
    --spring.datasource.username=root \
    --spring.datasource.password='root' \
    --spring.datasource.platform=mysql \
    --spring.datasource.validation-query="select 1" \
	--spring.dynamicdatasource.enable=true \
	--spring.dynamicdatasource.names=bomc,bomcbp \
	--spring.dynamicdatasource.bomc.dataSourceClassName=oracle.jdbc.driver.OracleDriver \
	--spring.dynamicdatasource.bomc.url=jdbc:oracle:thin:@192.168.55.30:1521:bomc \
	--spring.dynamicdatasource.bomc.username=bomc \
	--spring.dynamicdatasource.bomc.password=bomc \
	--spring.dynamicdatasource.bomcbp.dataSourceClassName=oracle.jdbc.driver.OracleDriver \
	--spring.dynamicdatasource.bomcbp.url=jdbc:oracle:thin:@192.168.55.30:1521:bomc \
	--spring.dynamicdatasource.bomcbp.username=bomcbp \
	--spring.dynamicdatasource.bomcbp.password=bomcbp \
	--sync-datasource.province=hubei \
	--sync-datasource.cmdb-type=old \
	--sync-datasource.userinfo-type=dynamic \
	--sync-datasource.cilist-url=http://192.168.55.46:50010/cmdbservice/api/v1/cmdb/info/cilist \
	--sync-datasource.agent-username=root \
	--sync-datasource.agent-password="1qaz)P(O" \
	--tsd.url=http://192.168.55.41:4242 \
    --spring.redis.host=192.168.55.46 \
    --spring.redis.port=6379  >/dev/null 2>&1 &
        
}

stop() {    
    while true
    do
        process=`ps aux | grep ${JAR_NAME} | grep -v grep`;
        if [ "$process" = "" ]; then
            echo "no process";
            break;
        else
            echo "kill process...";
            ps -ef | grep ${JAR_NAME} | grep -v grep | awk '{print $2}' | xargs kill -9
            sleep 3
        fi
    done    

}

restart() {
    stop;
    start;

}
case "$1" in
    'start')
        start
        ;;
    'stop')
        stop
        ;;
    'status')
        status
        ;;
    'restart')
        restart
        ;;
    *)
    echo "usage: $0 {start|stop|restart}"
    exit 1
        ;;
    esac
