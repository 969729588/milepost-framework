#!/bin/sh

DELIMITER='==============================================='

#1、mongodb
echo ${DELIMITER} '1、mongodb' ${DELIMITER};
ps -ef | grep mongodb;

#2、redis
echo ${DELIMITER} '2、redis' ${DELIMITER};
ps -ef | grep redis-server;

#3、activemq
echo ${DELIMITER} '3、activemq' ${DELIMITER};
ps -ef | grep activemq;

#4、elasticsearch
echo ${DELIMITER} '4、elasticsearch' ${DELIMITER};
ps -ef | grep elasticsearch;

#5、kibana
echo ${DELIMITER} '5、kibana' ${DELIMITER};
ps -ef | grep kibana;

#6、vsftpd
echo ${DELIMITER} '6、vsftpd' ${DELIMITER};
ps -ef | grep vsftpd;

#7、Rabbitmq
echo ${DELIMITER} '7、Rabbitmq' ${DELIMITER};
#rabbitmqctl status;
ps -ef | grep rabbitmq;

#8、mysql
#echo ${DELIMITER} '8、mysql' ${DELIMITER};
#ps -ef | grep mysqld;
#systemctl status mysqld;
