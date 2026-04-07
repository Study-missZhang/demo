package com.zky.demo.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.ai4j.openai4j.Json;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageJsonCodec;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 会话存储
 */
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    // Redis 会话前缀
    private static final String Key_PREFIX = "chat:memory";

    // Redis 会话过期时间
    private static final long EXPIRE_MINUTES = 30;

    // Redis 操作类
    private final StringRedisTemplate redisTemplate;



    public RedisChatMemoryStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redisTemplate.opsForValue().get(Key_PREFIX + memoryId);

        // Redis 中没有数据，返回空列表
        if(json == null){
            return new ArrayList<>();
        }

        try {
            // 反序列化
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 把最新的消息，写入到Redis中
     * @param memoryId
     * @param messages
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        try {
            String json = ChatMessageSerializer.messagesToJson(messages);
            redisTemplate.opsForValue().set(
                    Key_PREFIX + memoryId,
                    json,
                    EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e){
            throw new RuntimeException("序列化聊天记录失败：", e);
        }
    }

    /**
     * 删除Redis中的数据
     * @param memoryId
     */
    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(Key_PREFIX + memoryId);
    }
}
