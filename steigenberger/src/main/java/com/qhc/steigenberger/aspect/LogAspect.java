package com.qhc.steigenberger.aspect;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 日志统一打印切面
 */
@Component
@Aspect
public class LogAspect {

    private static Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution (* com.qhc.steigenberger.controller..*.*(..))")
    public void apiLogAop() {
    }

    @Around("apiLogAop()")
    public Object aroundApi(ProceedingJoinPoint point) throws Throwable {
    	Object[] args = point.getArgs();
    	StringBuilder argStr = new StringBuilder(512);
    	if (args != null) {
    		for (Object arg : args) {
    			argStr.append(",").append(argsToString(arg));
			}
    	}
    	String strArg = argStr.length() > 0 ? argStr.substring(1) : "";
        logger.info("统一日志 ===== {}.{}() start =====,参数:{}", point.getSignature().getDeclaringTypeName(), point.getSignature().getName(), strArg);
        DateTime startTime = new DateTime();
        DateTime endTime = null;
        Interval interval = null;
        Object response = null;
        try {
            //执行该方法
            response = point.proceed();
        } catch (Exception e) {
            logger.error("统一日志 ===== ", e);
            endTime = new DateTime();
            interval = new Interval(startTime, endTime);
            //logger.info("日志统一打印 ===== {}.{}() end =====,响应时间:{}毫秒,响应内容:{}", point.getSignature().getDeclaringTypeName(), point.getSignature().getName(), interval.toDurationMillis());
            throw e;
        }
        endTime = new DateTime();
        interval = new Interval(startTime, endTime);
        logger.info("统一日志 ===== {}.{}() end =====,响应时间:{}毫秒,响应内容:{}", point.getSignature().getDeclaringTypeName(), point.getSignature().getName(), interval.toDurationMillis(), argsToString(response));
        return response;
    }

    private String argsToString(Object object) {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    	if (object instanceof ServletRequest || object instanceof ServletResponse || object instanceof HttpSession) {
    		return String.valueOf(object);
    	}
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("统一日志 ===== 转换json失败", e);
        }
        return String.valueOf(object);
    }

}
