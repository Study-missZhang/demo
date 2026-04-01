package com.zky.demo.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

    /**
     * 聊天方法
     * @param question
     * @param sseEmitter
     */
    void stream(String question, SseEmitter sseEmitter);
}
