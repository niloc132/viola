package com.colinalworth.gwt.viola.web.server.rpq.impl;

import com.colinalworth.rpq.client.RequestQueue;
import com.colinalworth.rpq.client.RequestQueue.Service;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.binder.AnnotatedBindingBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Server wiring to let the server use RPQ services synchronously.
 */
public class RpqServerModuleBuilder {

	public Module build(final Class<? extends RequestQueue> queueClass) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				for (Method m : queueClass.getDeclaredMethods()) {

					Service s = m.getAnnotation(Service.class);
					if (s == null) {
						continue;
					}

					ServiceInvocationHandler handler = new ServiceInvocationHandler(s.value());
					requestInjection(handler);
					Object instance = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{m.getReturnType()}, handler);
					((AnnotatedBindingBuilder) bind(m.getReturnType())).toInstance(instance);
				}
				RequestQueueInvocationHandler handler = new RequestQueueInvocationHandler();
				requestInjection(handler);
				Object instance = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {queueClass}, handler);
				((AnnotatedBindingBuilder) bind(queueClass)).toInstance(instance);
			}
		};
	}
	private static class ServiceInvocationHandler implements InvocationHandler {
		private final Class<?> type;

		@Inject
		Injector injector;

		public ServiceInvocationHandler(Class<?> type) {
			this.type = type;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?>[] params = method.getParameterTypes();
			AsyncCallback<Object> callback = null;
			if (AsyncCallback.class.isAssignableFrom(params[params.length - 1])) {
				Class<?>[] truncatedParams = new Class[params.length - 1];
				System.arraycopy(params, 0, truncatedParams, 0, truncatedParams.length);
				params = truncatedParams;

				Object[] truncatedArgs = new Object[args.length - 1];
				System.arraycopy(args, 0, truncatedArgs, 0, truncatedArgs.length);
				callback = (AsyncCallback<Object>) args[args.length - 1];
				args = truncatedArgs;
			}
			Object instance = injector.getInstance(type);
			try {
				Object retValue = type.getMethod(method.getName(), params).invoke(instance, args);
				if (callback != null) {
					callback.onSuccess(retValue);
				}
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				if (callback != null) {
					callback.onFailure(e.getCause());
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (callback != null) {
					callback.onFailure(new RuntimeException(e.getMessage()));
				}
			}

			return null;
		}
	}
	private static class RequestQueueInvocationHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return null;
		}
	}
}
