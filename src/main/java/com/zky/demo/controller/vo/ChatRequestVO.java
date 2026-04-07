package com.zky.demo.controller.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "聊天请求体")
public class ChatRequestVO {

    @Schema(description = "用户问题", example = "什么是单例模式？")
    private String question;

    @Schema(description = "会话ID，同一会话保持上下文", example = "user001")
    private String sessionId;
}
