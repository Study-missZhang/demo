package com.zky.demo.controller;

import com.zky.demo.controller.vo.ChatRequestVO;
import com.zky.demo.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "聊天接口", description = "基于 DeepSeek 的流式对话")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Operation(summary = "流式对话", description = "发送问题，流式返回 AI 回答，支持多轮对话")
    @PostMapping(value = "stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream(@RequestBody ChatRequestVO chatRequestVO){
        SseEmitter sseEmitter = new SseEmitter(60_000L);  // 60 秒超时

        // 注册 SseEmitter 的生命周期回调
        // 正常完成
        sseEmitter.onCompletion(() -> {
            System.out.println("SseEmitter completed");
        });

        // 超时处理
        sseEmitter.onTimeout(() -> {
            System.out.println("SseEmitter timeout");
            sseEmitter.complete();
        });

        // 网络异常处理
        sseEmitter.onError((throwable) -> {
            System.out.println("SseEmitter error");
            sseEmitter.complete();
        });


        chatService.stream(chatRequestVO, sseEmitter);

        return sseEmitter;

    }
}
