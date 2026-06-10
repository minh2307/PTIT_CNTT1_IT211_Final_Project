package com.example.finalproject.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.web.bind.annotation.Controller *)")
    public void controllerPointcut() {}

    @Pointcut("execution(* com.example.finalproject.service..*(..))")
    public void servicePointcut() {}

    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String httpMethod = "UNKNOWN";
        String endpoint = "UNKNOWN";

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            httpMethod = request.getMethod();
            endpoint = request.getRequestURI();
        }

        String methodName = joinPoint.getSignature().toShortString();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("[REQUEST]");
        log.info("{} {}", httpMethod, endpoint);

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - start;
            LocalDateTime endTime = LocalDateTime.now();
            log.error("[RESPONSE]");
            log.error("Execution Time: {} ms", executionTime);
            throw throwable;
        }

        long executionTime = System.currentTimeMillis() - start;
        LocalDateTime endTime = LocalDateTime.now();

        log.info("[RESPONSE]");
        log.info("Execution Time: {} ms", executionTime);

        return result;
    }

    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> clazz = joinPoint.getSignature().getDeclaringType();
        String className = clazz.getSimpleName();
        
        // Resolve target class/interface name for formatting
        if (className.endsWith("Impl") && clazz.getInterfaces().length > 0) {
            className = clazz.getInterfaces()[0].getSimpleName();
        } else if (className.endsWith("Impl")) {
            className = className.substring(0, className.length() - 4);
        }

        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("{}.{}() threw exception in {} ms: {}", className, methodName, executionTime, throwable.getMessage());
            throw throwable;
        }

        long executionTime = System.currentTimeMillis() - start;
        log.info("{}.{}() executed in {} ms", className, methodName, executionTime);

        return result;
    }
}
