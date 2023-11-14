package org.ogreg;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged
@Priority(1000)
@Interceptor
public class LoggedInterceptor {
	private static final Logger log = LoggerFactory.getLogger(LoggedInterceptor.class);

	@AroundConstruct
	public Object logConstruction(InvocationContext ctx) throws Exception {
		long before = System.currentTimeMillis();
		Object result = ctx.proceed();
		log.info("LoggedInterceptor: {}, duration={} ms", ctx.getConstructor(), System.currentTimeMillis() - before);
		return result;
	}

	@AroundInvoke
	public Object logInvocation(InvocationContext ctx) throws Exception {
		long before = System.currentTimeMillis();
		Object result = ctx.proceed();
		log.info("LoggedInterceptor: {}, duration={} ms", ctx.getMethod(), System.currentTimeMillis() - before);
		return result;
	}
}
