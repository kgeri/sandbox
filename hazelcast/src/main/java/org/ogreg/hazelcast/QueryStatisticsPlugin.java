package org.ogreg.hazelcast;

import com.hazelcast.map.impl.query.Query;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public abstract class QueryStatisticsPlugin {
	private static final Logger log = LoggerFactory.getLogger(QueryStatisticsPlugin.class);

	static Query LastQuery;

	public static void install() {
		ByteBuddyAgent.install();

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		TypePool typePool = TypePool.Default.of(classLoader);
		ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(classLoader);
		ClassReloadingStrategy classReloadingStrategy = ClassReloadingStrategy.fromInstalledAgent();

		TypeDescription queryOperationType = typePool.describe("com.hazelcast.map.impl.query.QueryOperation").resolve();
		new ByteBuddy()
				.rebase(queryOperationType, classFileLocator)
				.method(ElementMatchers.hasMethodName("call"))
				.intercept(MethodDelegation.to(QueryOperationInterceptor.class))
				.make()
				.load(classLoader, classReloadingStrategy);
		log.info("Installed successfully");
	}

	public static class QueryOperationInterceptor {
		private static final Logger log = LoggerFactory.getLogger(QueryOperationInterceptor.class);

		@RuntimeType
		public static Object call(@SuperCall Callable<Object> superCall, @FieldValue("query") Object query) {
			try {
				log.info("QueryEngineImpl.execute ENTER");
				return superCall.call();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				LastQuery = (Query) query;
				log.info("QueryEngineImpl.execute EXIT");
			}
		}
	}
}
