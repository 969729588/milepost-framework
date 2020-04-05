#!/bin/sh
JAR_NAME=milepost-auth-1.0.0.100.jar
start() {
    echo "start process...";
    rm -rf ./logs
    nohup java -Xmx128m -Xms128m \
    -jar ${JAR_NAME} \
    --spring.profiles.active=test \
    --server.port=9999 \
    --info.app.description=JWT服务 \
    --spring.datasource.platform=mysql \
    --spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
    --spring.datasource.url='jdbc:mysql://192.168.223.136:3306/milepost_auth?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&serverTimezone=GMT%2B8' \
    --spring.datasource.username=root \
    --spring.datasource.password='ENC(tXVsX2fiUQfrNM9Gqey3pmRiLgw+Znp/ISEaZCOMDo0=)' \
    --spring.datasource.hikari.connection-timeout=30000 \
    --spring.datasource.hikari.idle-timeout=600000 \
    --spring.datasource.hikari.max-lifetime=1800000 \
    --spring.datasource.hikari.maximum-pool-size=10 \
    --spring.datasource.hikari.minimum-idle=10 \
    --eureka.client.service-url.defaultZone='http://192.168.223.136:8761/eureka/' \
    --eureka.instance.ip-address=192.168.223.136 \
    --multiple-tenant.tenant=tenant1 \
    --multiple-tenant.weight=1 \
    --multiple-tenant.label-and=aa,bb,cc \
    --multiple-tenant.label-or=dd,ee,ff \
    --auth.client-detail.access-token-validity=7300 \
    --auth.client-detail.refresh-token-validity=7300 \
    --track.enabled=true \
    --track.sampling=1 \
    >/dev/null 2>&1 &
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
            ps -ef | grep ${JAR_NAME} | grep -v grep | awk '{print $2}' | xargs kill -15
            sleep 3
        fi
    done

}

restart() {
    stop;
    start;
}

status() {
    ps -ef | grep ${JAR_NAME}
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
    echo "usage: $0 {start|stop|restart|status}"
    exit 1
        ;;
    esac
