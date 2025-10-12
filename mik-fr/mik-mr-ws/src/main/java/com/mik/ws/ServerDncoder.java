package com.mik.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ServerDncoder implements Decoder.Text<WsMessage>{

    //todo 提供全局单例
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WsMessage decode(String s) throws DecodeException {
        try {
            return objectMapper.readValue(s, WsMessage.class);
        } catch (IOException e) {
            throw new DecodeException(s, "无法解码消息", e);
        }
    }

    /**
     * 判断是否可以解码
     * @param s
     * @return
     */
    @Override
    public boolean willDecode(String s) {
        return false;
    }
}
