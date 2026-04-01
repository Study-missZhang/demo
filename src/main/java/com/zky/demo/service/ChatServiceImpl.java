package com.zky.demo.service;

import com.zky.demo.controller.vo.ChatRequestVO;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT =
            "你是一名和蔼可亲的老师，总是可以用简单的话语回答同学的问题。";

    private final Map<String, ChatMemory> memoryMap = new ConcurrentHashMap<>();

    @Autowired
    private StreamingChatLanguageModel streamingChatLanguageModel;


    @Override
    public void stream(ChatRequestVO chatRequestVO, SseEmitter sseEmitter) {

        String sessionId = chatRequestVO.getSessionId();

        String question = chatRequestVO.getQuestion();

        // 设置上下文记忆10条，根据sessionId进行区分
        ChatMemory chatMemory = memoryMap.computeIfAbsent(sessionId, id -> MessageWindowChatMemory.withMaxMessages(10));

        // 把问题存入聊天记忆中
        chatMemory.add(UserMessage.from(question));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(SYSTEM_PROMPT));
        messages.addAll(chatMemory.messages());

        streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<>() {
            @Override
            public void onNext(String token) {
                try {
                    // 每生成一个词就推给前端
                    sseEmitter.send(sseEmitter.event().data(token, MediaType.TEXT_PLAIN));
                } catch (Exception e) {
                    sseEmitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                // 生成完毕，将回答存入到记忆中
                chatMemory.add(response.content());
                sseEmitter.complete();
            }

            @Override
            public void onError(Throwable throwable) {
                // 出现错误，将错误信息抛给前端
                sseEmitter.completeWithError(throwable);
            }
        });
    }
}
