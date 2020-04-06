#!/bin/sh

DELIMITER='==============================================='

#1、mongodb
echo ${DELIMITER} '1、mongodb' ${DELIMITER};
/opt/root/module/mongodb/bin/mongod -f /etc/mongodb.conf;

#2、redis
echo ${DELIMITER} '2、redis' ${DELIMITER};
#/usr/local/bin/redis-server /opt/root/module/redis-3.0.4/myredis.conf;
#/opt/root/module/redis-5.0.7/src/redis-server /opt/root/module/redis-5.0.7/my_redis.conf;
/opt/root/module/redis-5.0.7/src/redis-server /opt/root/module/redis-5.0.7/redis_stand-alone/redis_6379.conf;

#3、activemq
echo ${DELIMITER} '3、activemq' ${DELIMITER};
/opt/root/module/apache-activemq-5.8.0/bin/activemq start;

#4、elasticsearch
echo ${DELIMITER} '4、elasticsearch' ${DELIMITER};
# /opt/root/module/elasticsearch-2.4.6/bin/elasticsearch -d;
su - elasticsearch -c '/opt/root/module/elasticsearch-6.2.3/bin/elasticsearch -Enetwork.host=192.168.223.136 -d';

#5、kibana
echo ${DELIMITER} '5、kibana' ${DELIMITER};
nohup /opt/root/module/kibana-6.2.3-linux-x86_64/bin/kibana \
--elasticsearch=http://192.168.223.136:9200 \
--host=192.168.223.136 \
--log-file=/opt/root/module/kibana-6.2.3-linux-x86_64/kibana.log \
>/dev/null 2>&1 &

#6、vsftpd
echo ${DELIMITER} '6、vsftpd' ${DELIMITER};
systemctl start vsftpd;
setsebool tftp_home_dir on;

#7、Rabbitmq
echo ${DELIMITER} '7、Rabbitmq' ${DELIMITER};
rabbitmq-server -detached;

#8、mysql
#echo ${DELIMITER} '8、mysql' ${DELIMITER};
#systemctl start mysqld;
