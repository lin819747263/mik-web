package com.mik.ws;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WsMessage {
    private String msgId;
    private String subject;
    private String msgType;
    private Object data;
}
