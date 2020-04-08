#!/bin/sh

DELIMITER='==================================================================================================='

start() {

	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-auth;
	chmod +x run.sh;
    ./run.sh start;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/authentication-service;
	chmod +x run.sh;
    ./run.sh start;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/authentication-ui;
	chmod +x run.sh;
    ./run.sh start;

    echo ${DELIMITER}
	cd /opt/root/milepost/milepost-turbine;
	chmod +x run.sh;
    ./run.sh start;

	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-admin;
	chmod +x run.sh;
    ./run.sh start;

	echo ${DELIMITER}
}

stop() {  

	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-auth;
	chmod +x run.sh;
    ./run.sh stop;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/authentication-service;
	chmod +x run.sh;
    ./run.sh stop;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/authentication-ui;
	chmod +x run.sh;
    ./run.sh stop;

	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-turbine;
	chmod +x run.sh;
    ./run.sh stop;

	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-admin;
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
	cd /opt/root/milepost/milepost-auth;
	chmod +x run.sh;
    ./run.sh status;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/authentication-service;
	chmod +x run.sh;
    ./run.sh status;
	
	echo ${DELIMITER}
	cd /opt/root/milepost/authentication-ui;
	chmod +x run.sh;
    ./run.sh status;

    echo ${DELIMITER}
	cd /opt/root/milepost/milepost-turbine;
	chmod +x run.sh;
    ./run.sh status;

	echo ${DELIMITER}
	cd /opt/root/milepost/milepost-admin;
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
