package com.zky.demo.service;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService{

    private static final String SYSTEM_PROMPT =
            "你是一名阿里 P8 级别的 Java 架构师，回答问题时必须专业、刻薄、直击痛点，且回答中必须包含相关的设计模式建议。";

    @Autowired
    private StreamingChatLanguageModel streamingChatLanguageModel;



    @Override
    public void stream(String question, SseEmitter sseEmitter) {
        streamingChatLanguageModel.generate(
                /**
                 * 聊天信息
                 * SystemMessage — 系统预设，告诉模型"你是谁、怎么回答"
                 * UserMessage — 用户说的话
                 */
                List.of(
                        SystemMessage.from(SYSTEM_PROMPT),
                        UserMessage.from(question)
                ),

                new StreamingResponseHandler<>() {
                    @Override
                    public void onNext(String token) {
                        try {
                            // 每生成一个词就推给前端
                            sseEmitter.send(sseEmitter.event().data(token, MediaType.TEXT_PLAIN));
                        } catch (Exception e){
                            sseEmitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onComplete(Response response) {
                        // 生成完毕，关闭 SSE 连接
                        sseEmitter.complete();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // 出现错误，将错误信息抛给前端
                        sseEmitter.completeWithError(throwable);
                    }
                }
        );
    }
}
