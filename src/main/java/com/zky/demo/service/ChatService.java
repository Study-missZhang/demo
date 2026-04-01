package com.zky.demo.service;

import com.zky.demo.controller.vo.ChatRequestVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

    /**
     * 聊天方法
     *
     * @param question
     * @param sseEmitter
     * @param sessionId 会话id
     */
    void stream(ChatRequestVO chatRequestVO, SseEmitter sseEmitter);
}
