package com.balabala.homework.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器 - 用于推送批改状态通知
 */
@Slf4j
@Component
public class CorrectionWebSocketHandler extends TextWebSocketHandler {

    // 存储用户会话: userId -> session
    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket连接建立 - 用户ID: {}, 当前在线: {}", userId, userSessions.size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket连接关闭 - 用户ID: {}, 当前在线: {}", userId, userSessions.size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理心跳或其他消息
        log.debug("收到WebSocket消息: {}", message.getPayload());
    }

    /**
     * 发送批改完成通知给用户
     */
    public void sendCorrectionCompleteNotification(Long userId, Long homeworkId,
                                                  Long submissionId, String homeworkTitle, String notifyType) {
        if (userId == null) return;

        WebSocketSession session = userSessions.get(String.valueOf(userId));
        if (session != null && session.isOpen()) {
            try {
                // 根据通知类型生成不同的消息内容
                String messageContent;
                if ("CORRECTION_COMPLETE_TEACHER".equals(notifyType)) {
                    messageContent = String.format("学生作业《%s》已完成AI批改，请查看", homeworkTitle);
                } else {
                    messageContent = String.format("作业《%s》批改完成", homeworkTitle);
                }

                String message = String.format(
                    "{\"type\":\"%s\",\"homeworkId\":%d,\"submissionId\":%d,\"studentId\":%d,\"title\":\"%s\",\"message\":\"%s\"}",
                    notifyType != null ? notifyType : "CORRECTION_COMPLETE",
                    homeworkId != null ? homeworkId : 0, submissionId != null ? submissionId : 0,
                    userId != null ? userId : 0,
                    homeworkTitle != null ? homeworkTitle : "", messageContent
                );
                session.sendMessage(new TextMessage(message));
                log.info("发送批改完成通知 - 用户ID: {}, 作业ID: {}, 类型: {}", userId, homeworkId, notifyType);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        } else {
            log.debug("用户不在线，无法发送通知 - 用户ID: {}", userId);
        }
    }

    /**
     * 发送学生提交作业通知给老师
     */
    public void sendSubmissionNotification(Long teacherId, Long homeworkId, 
                                          Long studentId, String homeworkTitle, String studentName) {
        if (teacherId == null) return;

        WebSocketSession session = userSessions.get(String.valueOf(teacherId));
        if (session != null && session.isOpen()) {
            try {
                String message = String.format(
                    "{\"type\":\"HOMEWORK_SUBMITTED\",\"homeworkId\":%d,\"studentId\":%d,\"title\":\"%s\",\"studentName\":\"%s\",\"message\":\"学生%s提交了作业《%s》\"}",
                    homeworkId != null ? homeworkId : 0, studentId != null ? studentId : 0,
                    homeworkTitle, studentName != null ? studentName : "学生" + studentId,
                    studentName != null ? studentName : "学生" + studentId, homeworkTitle
                );
                session.sendMessage(new TextMessage(message));
                log.info("发送作业提交通知 - 老师ID: {}, 作业: {}", teacherId, homeworkTitle);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        } else {
            log.debug("老师不在线，无法发送通知 - 老师ID: {}", teacherId);
        }
    }

    /**
     * 发送作业发布通知给学生
     */
    public void sendHomeworkPublishedNotification(Long studentId, Long homeworkId, 
                                                   String homeworkTitle, String subject) {
        if (studentId == null) return;

        WebSocketSession session = userSessions.get(String.valueOf(studentId));
        if (session != null && session.isOpen()) {
            try {
                String message = String.format(
                    "{\"type\":\"HOMEWORK_PUBLISHED\",\"homeworkId\":%d,\"title\":\"%s\",\"subject\":\"%s\",\"message\":\"%s老师发布了新作业《%s》，请及时完成\"}",
                    homeworkId != null ? homeworkId : 0, homeworkTitle, subject != null ? subject : "",
                    subject != null ? subject : "", homeworkTitle
                );
                session.sendMessage(new TextMessage(message));
                log.info("发送作业发布通知 - 学生ID: {}, 作业: {}", studentId, homeworkTitle);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        } else {
            log.debug("学生不在线，无法发送通知 - 学生ID: {}", studentId);
        }
    }

    /**
     * 发送学生加入班级通知给老师
     */
    public void sendStudentJoinedClassNotification(Long teacherId, Long studentId, 
                                                    String studentName, String className) {
        if (teacherId == null) return;

        WebSocketSession session = userSessions.get(String.valueOf(teacherId));
        if (session != null && session.isOpen()) {
            try {
                String message = String.format(
                    "{\"type\":\"STUDENT_JOINED_CLASS\",\"studentId\":%d,\"studentName\":\"%s\",\"className\":\"%s\",\"message\":\"学生%s加入了班级《%s》\"}",
                    studentId != null ? studentId : 0, 
                    studentName != null ? studentName : "学生" + studentId,
                    className != null ? className : "",
                    studentName != null ? studentName : "学生" + studentId,
                    className != null ? className : ""
                );
                session.sendMessage(new TextMessage(message));
                log.info("发送学生加入班级通知 - 老师ID: {}, 学生: {}, 班级: {}", teacherId, studentName, className);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        } else {
            log.debug("老师不在线，无法发送通知 - 老师ID: {}", teacherId);
        }
    }

    /**
     * 从会话中获取用户ID
     */
    private String getUserIdFromSession(WebSocketSession session) {
        // 从查询参数或header中获取用户ID
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            return query.replaceAll(".*userId=([^&]*).*", "$1");
        }
        return null;
    }

    /**
     * 发送转班申请通知给老师
     */
    public void sendClassTransferRequestNotification(Long teacherId, Long studentId,
                                                      String studentName, String className, String reason) {
        if (teacherId == null) return;

        WebSocketSession session = userSessions.get(String.valueOf(teacherId));
        if (session != null && session.isOpen()) {
            try {
                String message = String.format(
                    "{\"type\":\"CLASS_TRANSFER_REQUEST\",\"studentId\":%d,\"studentName\":\"%s\",\"className\":\"%s\",\"reason\":\"%s\",\"message\":\"学生%s申请加入班级《%s》\"}",
                    studentId != null ? studentId : 0,
                    studentName != null ? studentName : "学生" + studentId,
                    className != null ? className : "",
                    reason != null ? reason : "",
                    studentName != null ? studentName : "学生" + studentId,
                    className != null ? className : ""
                );
                session.sendMessage(new TextMessage(message));
                log.info("发送转班申请通知 - 老师ID: {}, 学生: {}, 班级: {}", teacherId, studentName, className);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        } else {
            log.debug("老师不在线，无法发送转班申请通知 - 老师ID: {}", teacherId);
        }
    }

    /**
     * 发送通知消息给学生
     */
    public void sendNotification(Long studentId, String title, String content, String type, Long homeworkId) {
        if (studentId == null) return;

        WebSocketSession session = userSessions.get(String.valueOf(studentId));
        if (session != null && session.isOpen()) {
            try {
                String message = String.format(
                    "{\"type\":\"NOTIFICATION\",\"title\":\"%s\",\"content\":\"%s\",\"notificationType\":\"%s\",\"homeworkId\":%d,\"timestamp\":%d}",
                    title != null ? title : "新通知",
                    content != null ? content : "",
                    type != null ? type : "NOTICE",
                    homeworkId != null ? homeworkId : 0,
                    System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(message));
                log.info("发送通知消息 - 学生ID: {}, 标题: {}", studentId, title);
            } catch (IOException e) {
                log.error("发送WebSocket通知失败", e);
            }
        } else {
            log.debug("学生不在线，无法发送实时通知 - 学生ID: {}", studentId);
        }
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
}
