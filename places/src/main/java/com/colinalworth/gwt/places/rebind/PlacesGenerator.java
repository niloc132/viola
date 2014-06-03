package com.colinalworth.gwt.places.rebind;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory.Route;
import com.colinalworth.gwt.places.shared.impl.AbstractPlacesImpl;
import com.colinalworth.gwt.places.shared.util.URL;
import com.colinalworth.gwt.places.vm.ParseException;
import com.colinalworth.gwt.places.vm.PlaceStringModel;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathComponent;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathConstant;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathVariable;
import com.colinalworth.gwt.places.vm.PlaceStringModel.QueryVariable;
import com.colinalworth.gwt.places.vm.PlaceStringParser;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dev.util.Name;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlacesGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();
		JClassType toGenerate = oracle.findType(typeName).isInterface();
		JClassType placeFactoryType = oracle.findType(Name.getSourceNameForClass(PlaceFactory.class));

		if (toGenerate == null) {
			logger.log(TreeLogger.ERROR, typeName + " is not an interface type");
			throw new UnableToCompleteException();
		}
		if (!toGenerate.isAssignableTo(placeFactoryType)) {
			logger.log(Type.ERROR, "This isn't a PlaceFactory");
			throw new UnableToCompleteException();
		}

		// Get the name of the new type
		String packageName = toGenerate.getPackage().getName();
		String simpleSourceName = toGenerate.getName().replace('.', '_') + "Impl";
		PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
		if (pw == null) {
			return packageName + "." + simpleSourceName;
		}

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleSourceName);
		factory.setSuperclass(Name.getSourceNameForClass(AbstractPlacesImpl.class));
		factory.addImplementedInterface(typeName);
		SourceWriter sw = factory.createSourceWriter(context, pw);

		List<MethodModel> models = new ArrayList<>();
		for(JMethod m : toGenerate.getMethods()) {
			if (m.isAnnotationPresent(Route.class)) {
				//collect data enough to build route methods
				Route route = m.getAnnotation(Route.class);
				if (route == null) {
					logger.log(Type.ERROR, "Method " + m.getName() + " does not have a @Route annotation");
					throw new UnableToCompleteException();
				}
				JClassType returnType = m.getReturnType().isInterface();
				String placeString = route.path();

				models.add(buildMethod(logger.branch(Type.TRACE, "Parsing @Route(" + placeString + ") " + returnType.getName() + " " + m.getName() + "()"), route, returnType, m.getName()));

				//implement place factory method
				sw.println(m.getReadableDeclaration(false, false, false, false, true) + " {");
				sw.indentln("return create(%1$s.class);", returnType.getParameterizedQualifiedSourceName());
				sw.println("}");
			}
		}
		Collections.sort(models);

		// implement fields from models/regex
		for (MethodModel model : models) {
			sw.println("%1$s %2$s = %1$s.compile(\"%3$s\");",
					Name.getSourceNameForClass(RegExp.class),
					model.getName(),
					escape(model.getPathRegexp()));
		}

		// declare ABF
		sw.println("public interface ABF extends %1$s {", AutoBeanFactory.class.getName());
		sw.indent();
		for (MethodModel model : models) {
			sw.indentln("%3$s<%1$s> %2$s();",
					model.getPlaceType().getParameterizedQualifiedSourceName(),
					model.getName(),
					AutoBean.class.getName());
		}
		sw.outdent();
		sw.println("}");

		// constructor
		sw.println("public %1$s() {", simpleSourceName);
		sw.indentln("super(%1$s.<ABF>create(ABF.class));", GWT.class.getName());
		sw.println("}");


		writeInnerRouteOut(logger.branch(Type.TRACE, "Writing String innerRoute(Place)"), sw, models);

		writeInnerRouteIn(logger.branch(Type.TRACE, "Writing Place innerRoute(String)"), sw, models);

		sw.commit(logger);
		return factory.getCreatedClassName();
	}

	private void writeInnerRouteOut(TreeLogger logger, SourceWriter sw, List<MethodModel> models) throws UnableToCompleteException {
		sw.println("protected String innerRoute(%1$s place) {", Name.getSourceNameForClass(Place.class));
		sw.indent();
		for (MethodModel model : models) {
			TreeLogger l = logger.branch(Type.TRACE, "Writing branch for " + model.getName());
			sw.println("if (place instanceof %1$s) {", model.getPlaceType().getQualifiedSourceName());
			//...
			sw.indent();
			sw.println("%1$s p = (%1$s) place;", model.getPlaceType().getQualifiedSourceName());
			sw.println("%1$s sb = new %1$s();", StringBuilder.class.getName());
//			sw.print("return \"\"");

			for (PathComponent pathComponent : model.getPathComponents()) {
//				sw.print(" + ");
				if (pathComponent instanceof PathConstant) {
					sw.println("sb.append(\"%1$s/\");", ((PathConstant) pathComponent).getValue());
				} else {
					assert pathComponent instanceof PathVariable;
					PathVariable var = (PathVariable) pathComponent;
					String getterExpression = getGetterMethod(l, model.getPlaceType(), var.getVarName());
					if (var.isOptional()) {
						sw.println("sb.append(urlEncodeOrDefault(p.%1$s)).append(\"/\");", getterExpression);
					} else {
						sw.println("sb.append(urlEncodeOrThrow(p.%1$s, \"%2$s\")).append(\"/\");", getterExpression, escape(model.getPlaceType().getQualifiedSourceName() + "." + getterExpression));
					}
				}
			}

			sw.println();
			sw.println("boolean seenQuery = false;");
			for (QueryVariable var : model.getQueryComponents()) {
				String getterExpression = getGetterMethod(l, model.getPlaceType(), var.getVarName());
				if (var.isOptional()) {
					sw.println("seenQuery = urlEncodePairOrSkip(sb, \"%1$s\", p.%2$s, seenQuery);", escape(var.getKey()), getterExpression);
				} else {
					sw.println("sb.append(seenQuery ? \"&\" : \"?\");");
					sw.println("sb.append(\"%1$s=\").append(urlEncodeOrThrow(p.%2$s, \"%3$s\"));", escape(UriUtils.encode(var.getKey())), getterExpression, escape(model.getPlaceType().getQualifiedSourceName() + "." + getterExpression));
					sw.println("seenQuery = true;");
				}
			}

			sw.println("return sb.toString();");
			sw.outdent();

			sw.println("}");
		}
		sw.println("return null;");
		sw.outdent();
		sw.println("}");
	}

	private void writeInnerRouteIn(TreeLogger logger, SourceWriter sw, List<MethodModel> models) {
		sw.println("protected %1$s innerRoute(String url) {", Name.getSourceNameForClass(Place.class));
		sw.indent();
		sw.println("String value;");
		for (MethodModel model : models) {
			sw.println("if (%1$s.test(url)) {", model.getName());
			sw.indent();

			sw.println("%1$s s = %2$s();", model.getPlaceType().getQualifiedSourceName(), model.getName());
			sw.println("%1$s res = %2$s.exec(url);", MatchResult.class.getName(), model.getName());
			int index = 1;
			for (PathComponent pathComponent : model.getPathComponents()) {
				if (pathComponent instanceof PathVariable) {
					PathVariable var = (PathVariable) pathComponent;
					sw.println("value = res.getGroup(%1$d);", index++);
					sw.println("if (value != null) {");
					sw.indentln("value = %1$s.decodePathSegment(value);", URL.class.getName());
					sw.println("}");
					sw.println("s.%1$s(value);", getSetterMethod(model.getPlaceType(), var.getVarName()));
				}
			}

			if (!model.getQueryComponents().isEmpty()) {
				sw.println("%1$s<String, %2$s<String>> map = buildListParamMap(url);", Map.class.getName(), List.class.getName());
				for (QueryVariable queryVariable : model.getQueryComponents()) {
					sw.println("if (map.containsKey(\"%1$s\")) {", escape(queryVariable.getKey()));
					sw.indentln("s.%1$s(map.get(\"%2$s\").get(0));", getSetterMethod(model.getPlaceType(), queryVariable.getVarName()), escape(queryVariable.getKey()));
					sw.println("}");
				}
			}

			sw.println("return s;");

			sw.outdent();
			sw.println("}");
		}
		sw.println("return null;");
		sw.outdent();
		sw.println("}");
	}

	private String getSetterMethod(JClassType type, String name) {
		//TODO verify that the class has the method
		return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private String getGetterMethod(TreeLogger logger, JClassType type, String varName) throws UnableToCompleteException {
		String cap = varName.substring(0, 1).toUpperCase() + varName.substring(1);
		JMethod m = getMethodHelper(type, "get" + cap);
		if (m != null) {
			return m.getName() + "()";
		}
		m = getMethodHelper(type, "is" + cap);
		if (m != null) {
			return m.getName() + "()";
		}
		m = getMethodHelper(type, "has" + cap);
		if (m != null) {
			return m.getName() + "()";
		}
		m = getMethodHelper(type, varName);
		if (m != null) {
			return m.getName() + "()";
		}

		logger.log(Type.ERROR, "Could not find get/is/has method for " + varName + " in " + type.getQualifiedSourceName());
		throw new UnableToCompleteException();
	}

	protected JMethod getMethodHelper(JClassType type, String methodName) {
		JMethod[] methods = type.getInheritableMethods();
		for (JMethod m : methods) {
			if (m.getName().equals(methodName)) {
				return m;
			}
		}
		return null;
	}

	private MethodModel buildMethod(TreeLogger logger, Route route, JClassType type, String name) throws UnableToCompleteException {
		MethodModel method = new MethodModel();
		method.setName(name);
		method.setPlaceType(type);
		method.setPriority(route.priority());
		try {
			method.setContents(new PlaceStringParser(new StringReader(route.path() + "\n")).url());
		} catch (ParseException e) {
			logger.log(Type.ERROR, "Unable to parse string " + route.path(), e);
			throw new UnableToCompleteException();
		}

		//TODO sanity check that url and querystring parts don't stomp on each other

		//build path regex
		StringBuilder regex = new StringBuilder("^");
		boolean first = true;
		List<PathComponent> pathComponents = method.getPathComponents();
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
				regex.append(quote(((PathConstant) pathComponent).getValue()));
			}
			first = false;
		}

		regex.append("/");
		if (!method.isRequiresTrailingSlash()) {
			regex.append("?");//final last slash is optional
		}

		boolean queryRequired = false;
		ArrayList<QueryVariable> queryVariables = new ArrayList<>(method.getQueryComponents());
		for (QueryVariable variable : queryVariables) {
			queryRequired |= !variable.isOptional();
		}

		//build query regex
		if (queryRequired) {
			regex.append("\\?.*");
		} else {
			regex.append("(?:\\?.*)?");
		}
		method.setPathRegexp(regex.toString());

		return method;
	}


	private static String quote(String string) {
		return string.replaceAll("([-\\/\\\\^$*+?.()|\\[\\]{}])", "\\\\$0");
	}

	public static class MethodModel implements Comparable<MethodModel> {
		private int priority;
		private String pathRegexp;
		private PlaceStringModel contents;
		private String name;
		private JClassType placeType;

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

		public JClassType getPlaceType() {
			return placeType;
		}

		public void setPlaceType(JClassType placeType) {
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
