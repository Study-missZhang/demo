package com.zky.demo.config;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel(DeepSeekProperties properties){
        return OpenAiStreamingChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
