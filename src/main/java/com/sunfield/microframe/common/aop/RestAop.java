package com.sunfield.microframe.common.aop;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Component
@Aspect
public class RestAop {

	private static Log log = LogFactory.getLog(RestAop.class);
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	@Around(value = "execution(public * com.sunfield.microframe.rest..*.*(..))")
	public Object beforeRest(ProceedingJoinPoint joinPoint){
		try {
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
	        Method method = signature.getMethod();
	        Class<?> targetClass = method.getDeclaringClass();
	
	        StringBuffer classAndMethod = new StringBuffer();
	
	        String target = targetClass.getName() + "#" + method.getName();
	        String params = JSONObject.toJSONStringWithDateFormat(joinPoint.getArgs(), DATE_FORMAT, SerializerFeature.WriteMapNullValue);
	
	        log.info(classAndMethod.toString() + "==> START REST: " + target + " PARAMS:" + params);
	
	        long start = System.currentTimeMillis();
	        Object result = joinPoint.proceed();
	        long timeConsuming = System.currentTimeMillis() - start;
	
	        log.info(classAndMethod.toString() + "<== END REST: " + target + 
	        		" RETURN:" + JSONObject.toJSONStringWithDateFormat(result, DATE_FORMAT, SerializerFeature.WriteMapNullValue) +" COST TIME:" + timeConsuming + "ms");
	        return result;
		} catch(Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
		return null;
	}
}
