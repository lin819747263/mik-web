package com.mik.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerEncoder implements Encoder.Text<WsMessage>{

    //todo 提供全局单例
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String encode(WsMessage wsMessage) throws EncodeException {
        try {
            return objectMapper.writeValueAsString(wsMessage);
        } catch (JsonProcessingException e) {
            log.error("序列化错误", e);
        }
        return "";
    }
}
