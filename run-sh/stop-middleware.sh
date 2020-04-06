#!/bin/sh

DELIMITER='==============================================='

#1、mongodb
echo ${DELIMITER} '1、mongodb' ${DELIMITER};
pkill -f mongodb;

#2、redis
echo ${DELIMITER} '2、redis' ${DELIMITER};
pkill -f redis-server;

#3、activemq
echo ${DELIMITER} '3、activemq' ${DELIMITER};
pkill -f activemq;

#4、elasticsearch
echo ${DELIMITER} '4、elasticsearch' ${DELIMITER};
pkill -f elasticsearch;

#5、kibana
echo ${DELIMITER} '5、kibana' ${DELIMITER};
pkill -f kibana;

#6、vsftpd
echo ${DELIMITER} '6、vsftpd' ${DELIMITER};
systemctl stop vsftpd;

#7、Rabbitmq
echo ${DELIMITER} '7、Rabbitmq' ${DELIMITER};
rabbitmqctl stop;

#8、mysql
#echo ${DELIMITER} '8、mysql' ${DELIMITER};
#systemctl stop mysqld;
