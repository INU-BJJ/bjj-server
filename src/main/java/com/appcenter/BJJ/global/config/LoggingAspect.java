package com.appcenter.BJJ.global.config;

import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // --- Pointcuts ---
    @Pointcut("execution(* com.appcenter.BJJ..*.*(..)) && !execution(* com.appcenter.BJJ.global..*(..))")
    public void all() {
    }

    @Pointcut("all() && !controller()")
    public void allExceptController() {
    }

    @Pointcut("execution(* com.appcenter.BJJ..*Controller.*(..))")
    public void controller() {
    }

    private String getTraceId() {
        return MDC.get("traceId");
    }

    // --- 요청 정보 DTO ---
    @Getter
    @AllArgsConstructor
    private static class RequestInfo {
        private String traceId;
        private String memberId;
        private String uri;
        private String httpMethod;
        private String controllerName;
        private String methodName;
        private JSONObject params;
    }

    private RequestInfo extractRequestInfo(JoinPoint joinPoint) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        HttpServletRequest request = servletAttributes.getRequest();

        String traceId = getTraceId();
        String memberId = getMemberIdFromSecurityContext();
        String uri = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        String httpMethod = request.getMethod();
        String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        JSONObject params = getParams(request);

        return new RequestInfo(traceId, memberId, uri, httpMethod, controllerName, methodName, params);
    }

    // --- 컨트롤러 Before 로그 ---
    @Before("controller()")
    public void logRequestStart(JoinPoint joinPoint) {
        RequestInfo info = extractRequestInfo(joinPoint);
        if (info == null) return;

        log.info("LOG_TYPE=HTTP_REQUEST | EVENT=START | TRACE_ID={} | MEMBER_ID={} | URI={} | HTTP_METHOD={} | HANDLER={}.{} | PARAMS={}",
                info.getTraceId(), info.getMemberId(), info.getUri(), info.getHttpMethod(),
                info.getControllerName(), info.getMethodName(), info.getParams());
    }

    // --- 컨트롤러 정상 종료 로그 ---
    @AfterReturning(pointcut = "controller()", returning = "result")
    public void logRequestEnd(JoinPoint joinPoint, Object result) {
        RequestInfo info = extractRequestInfo(joinPoint);
        if (info == null) return;

        log.info("LOG_TYPE=HTTP_REQUEST | EVENT=END | TRACE_ID={} | MEMBER_ID={} | URI={} | HANDLER={}.{} | STATUS=SUCCESS",
                info.getTraceId(), info.getMemberId(), info.getUri(),
                info.getControllerName(), info.getMethodName());
    }

    // --- 컨트롤러 예외 로그 ---
    @AfterThrowing(pointcut = "controller()", throwing = "ex")
    public void logRequestError(JoinPoint joinPoint, Throwable ex) {
        RequestInfo info = extractRequestInfo(joinPoint);
        if (info == null) return;

        String template = "LOG_TYPE=HTTP_REQUEST | EVENT=ERROR | TRACE_ID={} | MEMBER_ID={} | URI={} | HANDLER={}.{} | ERROR={} | MESSAGE={}";

        if (ex instanceof CustomException) {
            log.info(template, info.getTraceId(), info.getMemberId(), info.getUri(),
                    info.getControllerName(), info.getMethodName(),
                    ex.getClass().getSimpleName(), ex.getMessage());
        } else {
            log.error(template, info.getTraceId(), info.getMemberId(), info.getUri(),
                    info.getControllerName(), info.getMethodName(),
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    // --- 파라미터 로깅 (컨트롤러 제외) ---
    @Before("allExceptController()")
    public void logParameters(JoinPoint joinPoint) {
        String traceId = getTraceId();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String params = Arrays.stream(args)
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));

        log.debug("LOG_TYPE=METHOD | EVENT=CALL | TRACE_ID={} | SIGNATURE={}.{} | PARAMS=[{}]",
                traceId, className, methodName, params);
    }

    // --- 성능 측정 (전체) ---
    @Around("all()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String traceId = getTraceId();
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            log.debug("LOG_TYPE=METHOD | EVENT=PERFORMANCE | TRACE_ID={} | SIGNATURE={}.{} | EXECUTION_TIME={}ms",
                    traceId, className, methodName, elapsed);
        }
    }

    // --- 요청 파라미터 추출 ---
    private JSONObject getParams(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            jsonObject.put(param.replace(".", "-"), request.getParameter(param));
        }
        return jsonObject;
    }

    private String getMemberIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "-";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUsername();
        }

        return "-";
    }
}
