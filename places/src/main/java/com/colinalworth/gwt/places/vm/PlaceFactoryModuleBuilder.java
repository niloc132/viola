package com.colinalworth.gwt.places.vm;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.places.shared.impl.AbstractPlacesImpl;
import com.colinalworth.gwt.places.shared.util.URL;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathComponent;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathConstant;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathVariable;
import com.colinalworth.gwt.places.vm.PlaceStringModel.QueryVariable;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.google.web.bindery.autobean.vm.Configuration;

import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceFactoryModuleBuilder {
	public Module build(final Class<? extends PlaceFactory> placeFactoryClass) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				Object instance = Proxy.newProxyInstance(
						getClass().getClassLoader(),
						new Class[]{placeFactoryClass},
						new PlaceFactoryInvocationHandler(placeFactoryClass));

				((AnnotatedBindingBuilder) bind(placeFactoryClass)).toInstance(instance);
				((AnnotatedBindingBuilder) bind(PlaceFactory.class)).toInstance(instance);
			}
		};
	}
	private static class PlaceFactoryInvocationHandler extends AbstractPlacesImpl implements InvocationHandler {
		private final Class<? extends PlaceFactory> placeFactoryClass;

		private final List<MethodModel> methods;

		public PlaceFactoryInvocationHandler(Class<? extends PlaceFactory> placeFactoryClass) {
			super(null);
			this.placeFactoryClass = placeFactoryClass;
			methods = new ArrayList<MethodModel>();
			for (Method m : placeFactoryClass.getDeclaredMethods()) {
				Route r = m.getAnnotation(Route.class);
				if (r == null) {
					System.err.println("Missing @Route annotation on " + m.getName());
					continue;
				}
				methods.add(buildMethod(m, r));
			}
			Collections.sort(methods);
		}

		private MethodModel buildMethod(Method m, Route route) {
			MethodModel model = new MethodModel();

			model.setName(m.getName());
			model.setPlaceType((Class<? extends Place>) m.getReturnType());
			model.setPriority(route.priority());

			try {
				model.setContents(new PlaceStringParser(new StringReader(route.path())).url());
			} catch (ParseException e) {
				throw new RuntimeException("Unable to parse string " + route.path(), e);
			}

			//TODO sanity check that url and querystring parts don't stomp on each other

			//build path regex
			StringBuilder regex = new StringBuilder("^");
			boolean first = true;
			List<PathComponent> pathComponents = model.getPathComponents();
			for (int i = 0; i < pathComponents.size(); i++) {
				PathComponent pathComponent = pathComponents.get(i);

				assert pathComponent != null;
				if (pathComponent instanceof PathVariable) {
					PathVariable variable = (PathVariable) pathComponent;
					regex.append("(?:");
					if (!first) {
						regex.append("/");
					}
					regex.append("([a-zA-Z0-9_.%");
					if (i + 1 == pathComponents.size()) {
						regex.append("/");
					}
					regex.append("]+))");
					if (variable.isOptional()) {
						regex.append("?");
					}
				} else {
					assert pathComponent instanceof PathConstant : pathComponent.getClass();
					if (!first) {
						regex.append("/");
					}
					regex.append(Pattern.quote(((PathConstant) pathComponent).getValue()));
				}
				first = false;
			}

			regex.append("/");
			if (!model.isRequiresTrailingSlash()) {
				regex.append("?");//final last slash is optional
			}

			boolean queryRequired = false;
			ArrayList<QueryVariable> queryVariables = new ArrayList<>(model.getQueryComponents());
			for (QueryVariable variable : queryVariables) {
				queryRequired |= !variable.isOptional();
			}

			//build query regex
			if (queryRequired) {
				regex.append("\\?.*");
			} else {
				regex.append("(?:\\?.*)?");
			}
			model.setPathRegexp(regex.toString());

			return model;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass() == PlaceFactory.class || method.getDeclaringClass() == Object.class) {
				try {
					return method.invoke(this, args);
				} catch (InvocationTargetException ex) {
					throw ex.getCause();
				}
			}
			//else its an autobeanfactory-ish method, forward to create
			assert args.length == 0;
			return create((Class<? extends Place>) method.getReturnType());
		}

		@Override
		public <P extends Place> P create(Class<P> clazz) {
			return AutoBeanFactorySource.createBean(clazz, new Configuration.Builder().build()).as();
		}

		@Override
		protected Place innerRoute(String url) {
			for (MethodModel m : methods) {
				if (url.matches(m.getPathRegexp())) {
					Place s = create(m.getPlaceType());
					int index = 1;
					Matcher match = Pattern.compile(m.getPathRegexp()).matcher(url);
					match.find();
					for (PathComponent pathComponent : m.getPathComponents()) {
						if (pathComponent instanceof PathVariable) {
							if (match.groupCount() < index) {
								break;//won't find later variables
							}
							String varName = ((PathVariable) pathComponent).getVarName();
							String value = match.group(index++);
							if (value != null) {
								value = URL.decodePathSegment(value);
							}
							setValue(m, s, varName, value);
						}
					}
					if (!m.getQueryComponents().isEmpty()) {
						Map<String, List<String>> map = buildListParamMap(url);
						for (QueryVariable queryVariable : m.getQueryComponents()) {
							if (map.containsKey(queryVariable.getKey())) {
								setValue(m, s, queryVariable.getVarName(), map.get(queryVariable.getKey()).get(0));
							}
						}
					}

					return s;
				}
			}
			return null;
		}

		@Override
		protected String innerRoute(Place place) {
			for (MethodModel method : methods) {
				if (method.getPlaceType().isInstance(place)) {
					StringBuilder sb = new StringBuilder();
					for (PathComponent pathComponent : method.getPathComponents()) {
						if (pathComponent instanceof PathConstant) {
							sb.append(((PathConstant) pathComponent).getValue());
						} else {
							assert pathComponent instanceof PathVariable;
							PathVariable var = (PathVariable) pathComponent;
							String value = getValue(method, place, var.getVarName());
							if (var.isOptional()) {
								sb.append(urlEncodeOrDefault(value));
							} else {
								sb.append(urlEncodeOrThrow(value, method.getPlaceType() + "." + var.getVarName()));
							}
						}
						sb.append("/");
					}

					boolean seenQuery = false;
					for (QueryVariable var : method.getQueryComponents()) {
						String value = getValue(method, place, var.getVarName());
						if (var.isOptional()) {
							seenQuery = urlEncodePairOrSkip(sb, var.getKey(), value, seenQuery);
						} else {
							sb.append(seenQuery ? "&" : "?");
							sb.append(var.getKey()).append("=").append(urlEncodeOrThrow(value, method.getPlaceType() + "." + var.getVarName()));
							seenQuery = true;
						}

					}

					return sb.toString();
				}
			}
			return null;
		}

		private void setValue(MethodModel m, Place s, String varName, String value) {
			String cap = varName.substring(0, 1).toUpperCase() + varName.substring(1);
			try {
				//TODO assuming string
				m.getPlaceType().getMethod("set" + cap, String.class).invoke(s, value);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		private String getValue(MethodModel method, Place place, String varName) {
			String cap = varName.substring(0, 1).toUpperCase() + varName.substring(1);
			try {
				//TODO assuming string, get
				return (String) method.getPlaceType().getMethod("get" + cap).invoke(place);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
			return null;
		}
	}


	public static class MethodModel implements Comparable<MethodModel> {
		private int priority;
		private String pathRegexp;
		private PlaceStringModel contents;
		private String name;
		private Class<? extends Place> placeType;

		public String getPathRegexp() {
			return pathRegexp;
		}

		public void setPathRegexp(String pathRegexp) {
			this.pathRegexp = pathRegexp;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<PathComponent> getPathComponents() {
			return contents.getPath();
		}
		public Set<QueryVariable> getQueryComponents() {
			return contents.getQuery();
		}

		public void setContents(PlaceStringModel contents) {
			this.contents = contents;
		}

		public Class<? extends Place> getPlaceType() {
			return placeType;
		}

		public void setPlaceType(Class<? extends Place> placeType) {
			this.placeType = placeType;
		}

		public boolean isRequiresTrailingSlash() {
			return contents.isRequiresTrailingSlash();
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		@Override
		public int compareTo(MethodModel o) {
			return priority - o.priority;
		}
	}
}
