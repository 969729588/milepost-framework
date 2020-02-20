
CREATE TABLE oauth_access_token (
  token_id varchar(256) DEFAULT NULL,
  token blob,
  authentication_id varchar(128) NOT NULL,
  user_name varchar(256) DEFAULT NULL,
  client_id varchar(128) DEFAULT NULL,
  authentication blob,
  refresh_token varchar(256) DEFAULT NULL,
  PRIMARY KEY (authentication_id)
) ;

CREATE TABLE oauth_approvals (
  userId varchar(256) DEFAULT NULL,
  clientId varchar(256) DEFAULT NULL,
  scope varchar(256) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  expiresAt datetime DEFAULT NULL,
  lastModifiedAt datetime DEFAULT NULL
) ;

CREATE TABLE oauth_client_details (
  client_id varchar(128) NOT NULL,
  resource_ids varchar(256) DEFAULT NULL,
  client_secret varchar(256) DEFAULT NULL,
  scope varchar(256) DEFAULT NULL,
  authorized_grant_types varchar(256) DEFAULT NULL,
  web_server_redirect_uri varchar(256) DEFAULT NULL,
  authorities varchar(256) DEFAULT NULL,
  access_token_validity int(11) DEFAULT NULL,
  refresh_token_validity int(11) DEFAULT NULL,
  additional_information varchar(256) DEFAULT NULL,
  autoapprove varchar(256) DEFAULT NULL,
  PRIMARY KEY (client_id)
) ;
INSERT INTO oauth_client_details VALUES ('test-client-id', null, '123456', 'test', 'implicit,refresh_token,password,authorization_code', null, null, '7200', '7200', '{\"a\":\"b\"}', 'true');

CREATE TABLE oauth_client_token (
  token_id varchar(256) DEFAULT NULL,
  token blob,
  authentication_id varchar(128) NOT NULL,
  user_name varchar(256) DEFAULT NULL,
  client_id varchar(128) DEFAULT NULL,
  PRIMARY KEY (authentication_id)
) ;

CREATE TABLE oauth_code (
  code varchar(256) DEFAULT NULL,
  authentication blob
) ;

CREATE TABLE oauth_refresh_token (
  token_id varchar(256) DEFAULT NULL,
  token blob,
  authentication blob
) ;

CREATE TABLE user (
  id varchar(50) NOT NULL,
  username varchar(100) NOT NULL COMMENT '用户名，登录名，是唯一键',
  truename varchar(100) DEFAULT NULL COMMENT '用户真是姓名',
  mobile varchar(20) DEFAULT NULL COMMENT '手机号码',
  email varchar(50) DEFAULT NULL COMMENT '邮箱',
  password varchar(100) DEFAULT NULL COMMENT '密码',
  activated tinyint(1) DEFAULT NULL COMMENT '激活状态，1激活，0未激活',
  PRIMARY KEY (id),
  UNIQUE KEY user_username_uq (username) USING BTREE
) ;
-- 初始用户，admin/123456
INSERT INTO user(id, username, truename, mobile, email, password, activated) values('1', 'admin', 'Huarf', '18310891237', '969729588@qq.com', 'e10adc3949ba59abbe56e057f20f883e', '1');
