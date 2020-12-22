/*
 Navicat MySQL Data Transfer

 Source Server         : 兵哥机器
 Source Server Type    : MySQL
 Source Server Version : 80014
 Source Host           : 192.168.3.20:3306
 Source Schema         : promise-message

 Target Server Type    : MySQL
 Target Server Version : 80014
 File Encoding         : 65001

 Date: 22/12/2020 13:14:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for message_deliver
-- ----------------------------
DROP TABLE IF EXISTS `message_deliver`;
CREATE TABLE `message_deliver`  (
  `msg_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息ID',
  `msg_status` int(1) NULL DEFAULT NULL COMMENT '消息状态：\r\n0-发送中\r\n1-MQ服务器签收成功\r\n2-MQ服务器签收失败\r\n3-消费端签收成功\r\n4-消费端签收失败',
  `exchange` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交换机名称',
  `routing_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由Key',
  `content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息内容',
  `error_cause` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误原因',
  `max_retry` int(1) NULL DEFAULT NULL COMMENT '最多重试次数',
  `current_retry` int(1) NULL DEFAULT NULL COMMENT '当前重试次数',
  `del_flag` int(1) NULL DEFAULT NULL COMMENT '逻辑删除',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `modify_time` timestamp(0) NULL DEFAULT NULL COMMENT '修改时间',
  `modify_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `version` int(10) NULL DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`msg_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息投递表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
