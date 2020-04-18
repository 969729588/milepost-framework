#!/bin/sh
JAR_NAME=milepost-admin-1.0.0.100.jar
start() {
    echo "start process...";
    rm -rf ./logs
    nohup java -Xmx64m -Xms32m \
    -jar ${JAR_NAME} \
    --spring.profiles.active=test \
    --server.port=8768 \
    --eureka.client.service-url.defaultZone='http://192.168.186.131:8761/eureka/' \
    --eureka.instance.ip-address=192.168.186.131 \
    --spring.boot.admin.ui.title=milepost-admin \
    --spring.boot.admin.discovery.ignored-services='[test-appName-1, test-appName-1]' \
    --spring.rabbitmq.host=192.168.186.131 \
    --spring.rabbitmq.port=5672 \
    --spring.rabbitmq.username=admin \
    --spring.rabbitmq.password=admin \
    --track.enabled=true \
    --track.sampling=1 \
    --multiple-tenant.tenant=tenant1 \
    >/dev/null 2>&1 &
}

#--spring.mail.username=m18310891237@163.com \
#--spring.mail.password=sqm123456qweasd \
#--spring.mail.host=smtp.163.com \
#--spring.boot.admin.notify.mail.to=969729588@qq.com \


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
