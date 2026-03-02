package com.hbin.mall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

@Configuration
public class GatewayBlockConfig {

    @PostConstruct
    public void initBlockHandler() {
        BlockRequestHandler handler = (exchange, t) -> {
            Map<String, String> map = Map.of(
                    "errorCode", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "errorMessage", "请求过于频繁，触发限流"
            );
            return ServerResponse
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(map));
        };
        GatewayCallbackManager.setBlockHandler(handler);
    }
}