/*
 Navicat MySQL Data Transfer

 Source Server         : 兵哥机器
 Source Server Type    : MySQL
 Source Server Version : 80014
 Source Host           : 192.168.3.20:3306
 Source Schema         : promise-provider

 Target Server Type    : MySQL
 Target Server Version : 80014
 File Encoding         : 65001

 Date: 22/12/2020 13:14:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_provider
-- ----------------------------
DROP TABLE IF EXISTS `biz_provider`;
CREATE TABLE `biz_provider`  (
  `provider_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息提供端业务表ID',
  `provider_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息提供端业务表名称',
  `del_flag` int(1) NULL DEFAULT NULL COMMENT '逻辑删除',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `modify_time` timestamp(0) NULL DEFAULT NULL COMMENT '修改时间',
  `modify_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `version` int(10) NULL DEFAULT NULL COMMENT '乐观锁'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '提供端业务表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
