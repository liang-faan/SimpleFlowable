package com.gitee.approval.aspect;

import java.util.Arrays;
import java.util.HashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class WebLogAspect {

	@Pointcut("execution(* com.weishao.approval.controller..*.*(..))")
	public void webLog() {
	}

	@Around("webLog()")
	public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		ObjectMapper objectMapper = new ObjectMapper();
		String className = proceedingJoinPoint.getSignature().getDeclaringTypeName();
		boolean flag = className.equals("com.weishao.approval.controller.ExceptionController");
		if (!flag) {
			HashMap<String, String> msg = new HashMap<String, String>();
			msg.put("REQ_URL :", request.getRequestURL().toString());
			msg.put("METHOD  ", request.getMethod());
			msg.put("REMOTE_IP ", request.getRemoteAddr());
			msg.put("CLASS_METHOD ", className + "." + proceedingJoinPoint.getSignature().getName());
			msg.put("BODY_CONTENT : ", Arrays.toString(proceedingJoinPoint.getArgs()));
			String json = objectMapper.writeValueAsString(msg);
			log.info("[Request] {}", json);
		}

		long startTime = System.currentTimeMillis();
		Object result = proceedingJoinPoint.proceed();

		if (!flag) {
			String json = objectMapper.writeValueAsString(result);
			log.info("[Response] : {} , Elipse {} ms", json, System.currentTimeMillis() - startTime);
		}

		return result;
	}

}
