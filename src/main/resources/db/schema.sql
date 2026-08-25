CREATE DATABASE IF NOT EXISTS tianxin_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE tianxin_platform;

CREATE TABLE IF NOT EXISTS sys_user (
  id CHAR(36) NOT NULL,
  username VARCHAR(32) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  password_hash VARCHAR(256) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
  role_code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  permissions VARCHAR(2000) NOT NULL,
  PRIMARY KEY (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id CHAR(36) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  PRIMARY KEY (user_id, role_code),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_code) REFERENCES sys_role(role_code) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系';
