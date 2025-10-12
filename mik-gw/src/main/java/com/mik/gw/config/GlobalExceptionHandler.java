package com.mik.gw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mik.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (ex instanceof ResponseStatusException) {
            status = ((ResponseStatusException) ex).getStatusCode().value();
        }

        Result errorResponse = Result.error(status, ex.getMessage());

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                byte[] errorBytes = objectMapper.writeValueAsBytes(errorResponse);
                return bufferFactory.wrap(errorBytes);
            } catch (Exception e) {
                return bufferFactory.wrap("".getBytes());
            }
        }));
    }
}
