// gateway模块中创建
package com.hbin.mall.gateway.exception;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.hutool.json.JSONUtil;
import com.hbin.mall.common.result.Result;
import org.springframework.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * WebFlux全局异常处理器
 */
@Component
@Slf4j
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        Throwable rootCause = getRootCause(ex);

        Result<?> result = buildErrorResponse(rootCause);

        if (result.getCode() != 500) {
            log.warn("网关认证异常: {}", result.getMsg());
        } else {
            log.error("网关内部异常", ex);
        }

        exchange.getResponse().getHeaders().set("Content-Type", "application/json;charset=UTF-8");

        String jsonResult = JSONUtil.toJsonStr(result);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(jsonResult.getBytes(StandardCharsets.UTF_8))));
    }

    private Throwable getRootCause(Throwable ex) {
        while (ex.getCause() != null && ex.getCause() != ex) {
            ex = ex.getCause();
        }
        return ex;
    }

    private Result<?> buildErrorResponse(Throwable ex) {
        if (ex instanceof NotLoginException) {
            return Result.error(401, "NOT_LOGIN");
        }

        if (ex instanceof NotPermissionException) {
            return Result.error(403, "NO_PERMISSION");
        }

        if (ex instanceof DisableServiceException) {
            return Result.error(403, "DISABLE_SERVICE");
        }

        if (ex instanceof SaTokenException) {
            String message = ex.getMessage();
            if (message != null && (message.contains("token 无效") || message.contains("未登录"))) {
                return Result.error(401, "NOT_LOGIN");
            }
            if (message != null && message.contains("权限不足")) {
                return Result.error(403, "NO_PERMISSION");
            }
            return Result.error(401, "NOT_LOGIN");
        }

        if (ex instanceof org.springframework.web.server.ResponseStatusException responseEx) {
            int status = responseEx.getStatusCode().value();
            return Result.error(status, responseEx.getReason());
        }

        return Result.error(500, "GATEWAY_INTERNAL_ERROR");
    }
}