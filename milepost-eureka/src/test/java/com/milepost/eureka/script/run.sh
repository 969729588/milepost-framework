#!/bin/sh
JAR_NAME=milepost-eureka-1.0.0.100.jar
start() {
    echo "start process...";
    rm -rf ./logs
    nohup java -Xmx256m -Xms256m \
	-jar ${JAR_NAME} \
    --spring.profiles.active=test \
    --server.port=8761 \
    --eureka.instance.ip-address=192.168.223.129 >/dev/null 2>&1 &
}

#java后加“-Dssl=true”开启https。
#java后加“-Xmx256m -Xms256m”配置内存，支持的m、g单位。

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
