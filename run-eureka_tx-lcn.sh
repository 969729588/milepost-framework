#!/bin/sh

DELIMITER='==================================================================================================='

start() {
	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-eureka;
	chmod +x run.sh;
    ./run.sh start;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/tx-lcn-5.0.2.RELEASE;
	chmod +x run.sh;
    ./run.sh start;
	
	
	echo ${DELIMITER}
}

stop() {  
	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-eureka;
	chmod +x run.sh;
    ./run.sh stop;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/tx-lcn-5.0.2.RELEASE;
	chmod +x run.sh;
    ./run.sh stop;
	
	
	echo ${DELIMITER}
}


restart() {
    stop;
    start;
}

status() {
	echo ${DELIMITER}
    cd /opt/root/milepost/milepost-eureka;
	chmod +x run.sh;
    ./run.sh status;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/tx-lcn-5.0.2.RELEASE;
	chmod +x run.sh;
    ./run.sh status;
	
	
	echo ${DELIMITER}
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
