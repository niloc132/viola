package com.colinalworth.gwt.viola.web.rebind;

import com.colinalworth.gwt.viola.web.client.impl.AbstractPlacesImpl;
import com.colinalworth.gwt.viola.web.shared.mvp.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceFactory.Route;
import com.colinalworth.gwt.viola.web.vm.ParseException;
import com.colinalworth.gwt.viola.web.vm.PlaceStringModel;
import com.colinalworth.gwt.viola.web.vm.PlaceStringModel.PathComponent;
import com.colinalworth.gwt.viola.web.vm.PlaceStringModel.PathConstant;
import com.colinalworth.gwt.viola.web.vm.PlaceStringModel.PathVariable;
import com.colinalworth.gwt.viola.web.vm.PlaceStringModel.QueryVariable;
import com.colinalworth.gwt.viola.web.vm.PlaceStringParser;
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
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
		for(JMethod m : toGenerate.getOverridableMethods()) {
			if (m.isAnnotationPresent(Route.class)) {
				//collect data enough to build route methods
				Route route = m.getAnnotation(Route.class);
				JClassType returnType = m.getReturnType().isInterface();
				models.add(buildMethod(logger, route.value(), returnType, m.getName()));

				//implement place factory method
				sw.println(m.getReadableDeclaration(false, false, false, false, true) + " {");
				sw.indentln("return create(%1$s.class);", returnType.getParameterizedQualifiedSourceName());
				sw.println("}");
			}
		}

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


		writerInnerRouteOut(logger.branch(Type.INFO, "Writing String innerRoute(Place)"), sw, models);


		sw.println("protected %1$s innerRoute(String url) {", Name.getSourceNameForClass(Place.class));
		sw.indent();
		for (MethodModel model : models) {
			sw.println("if (%1$s.test(url)) {", model.getName());
			sw.indent();

			sw.println("%1$s s = %2$s();", model.getPlaceType().getQualifiedSourceName(), model.getName());
			sw.println("%1$s res = %2$s.exec(url);", MatchResult.class.getName(), model.getName());
			int index = 1;
			for (PathComponent pathComponent : model.getPathComponents()) {
				if (pathComponent instanceof PathVariable) {
					PathVariable var = (PathVariable) pathComponent;
					sw.println("s.%1$s(res.getGroup(%2$d));", getSetterMethod(model.getPlaceType(), var.getVarName()), (Integer)index++);
				}
			}

			//TODO assign querystring

			sw.println("return s;");

			sw.outdent();
			sw.println("}");
		}
		sw.println("return null;");
		sw.outdent();
		sw.println("}");

		sw.commit(logger);
		return factory.getCreatedClassName();
	}

	private void writerInnerRouteOut(TreeLogger logger, SourceWriter sw, List<MethodModel> models) throws UnableToCompleteException {
		sw.println("protected String innerRoute(%1$s place) {", Name.getSourceNameForClass(Place.class));
		sw.indent();
		for (MethodModel model : models) {
			TreeLogger l = logger.branch(Type.INFO, "Writing branch for " + model.getName());
			sw.println("if (place instanceof %1$s) {", model.getPlaceType().getQualifiedSourceName());
			//...
			sw.indent();
			sw.println("%1$s p = (%1$s) place;", model.getPlaceType().getQualifiedSourceName());
			sw.print("return \"\"");

			for (PathComponent pathComponent : model.getPathComponents()) {
				sw.print(" + ");
				if (pathComponent instanceof PathConstant) {
					sw.print("\"%1$s/\"", ((PathConstant) pathComponent).getValue());
				} else {
					assert pathComponent instanceof PathVariable;
					PathVariable var = (PathVariable) pathComponent;
					String getterExpression = getGetterMethod(l, model.getPlaceType(), var.getVarName());
					if (var.isOptional()) {
						sw.print("urlEncodeOrDefault(p.%1$s)", getterExpression);
					} else {
						sw.print("urlEncodeOrThrow(p.%1$s, \"%2$s\")", getterExpression, escape(model.getPlaceType().getQualifiedSourceName() + "." + getterExpression));
					}
				}
			}
			sw.println(" + \"?\"");
			for (QueryVariable var : model.getQueryComponents()) {
				sw.print(" + ");
				String getterExpression = getGetterMethod(l, model.getPlaceType(), var.getVarName());
				if (var.isOptional()) {
					sw.print("urlEncodePairOrSkip(\"%1$s\", p.%2$s)", escape(var.getKey()), getterExpression);
				} else {
					sw.print("\"%1$s=\" + urlEncodeOrThrow(p.%2$s)", escape(UriUtils.encode(var.getKey())), getterExpression);
				}
			}

			sw.println(";");
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

	private MethodModel buildMethod(TreeLogger logger, String placeString, JClassType type, String name) throws UnableToCompleteException {
		// * everything is a literal except for {[a-zA-Z][a-zA-Z0-9]\\??}s
		// * from those literals, split on ?
		// ** before that marker, disallow / except in trailing param
		// ** after that marker, split on &, allow pairs out of order


//		Pattern vars = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_]*)(\\??)\\}");

		MethodModel method = new MethodModel();
		method.setName(name);
		method.setPlaceType(type);
		try {
			method.setContents(new PlaceStringParser(new StringReader(placeString + "\n")).url());
		} catch (ParseException e) {
			logger.log(Type.ERROR, "Unable to parse string", e);
			throw new UnableToCompleteException();
		}
//		List<String> fragments = new ArrayList<>();
//		Matcher m = vars.matcher(placeString);
//		int lastIndex = 0;
//		while (m.matches()) {
//			if (lastIndex != m.start()) {
//				fragments.add(placeString.substring(lastIndex, m.start()));
//			} else {
//				unsure that we need this check, but we need to have empty strings
//				fragments.add("");
//			}
//
//			lastIndex = m.start();
//			String varName = m.group(1);
//			String optional = m.groupCount() > 2 ? m.group(2) : null;
//			method.getArgs().add(new ArgModel(varName, "?".equals(optional)));
//		}
//		assert fragments.size() == method.getArgs().size();
//		if (lastIndex < placeString.length()) {
//			fragments.add(placeString.substring(lastIndex));
//		}


		//build path regex
		StringBuilder regex = new StringBuilder("^");
		for (PathComponent pathComponent : method.getPathComponents()) {
			assert pathComponent != null;
			if (pathComponent instanceof PathVariable) {
				PathVariable variable = (PathVariable) pathComponent;
				regex.append("(?:([a-zA-Z0-9_.%]+)/)");//TODO better matcher
				if (variable.isOptional()) {
					regex.append("?");
				}
			} else {
				assert pathComponent instanceof PathConstant : pathComponent.getClass();
				regex.append(Pattern.quote(((PathConstant) pathComponent).getValue() + "/"));
			}
		}

		StringBuilder query = new StringBuilder("(?:\\?");
		boolean queryRequired = false;
		ArrayList<QueryVariable> queryVariables = new ArrayList<>(method.getQueryComponents());
		for (int i = 0; i < queryVariables.size(); i++) {
			QueryVariable variable = queryVariables.get(i);
			assert variable != null;
			if (i != 0) {
				query.append("|");
			}
			queryRequired |= !variable.isOptional();
			query.append(Pattern.quote(variable.getKey() + "=")).append("(?:([a-zA-Z0-9_.%]+)/)");
		}

		//build query regex

		method.setPathRegexp(regex.append("$").toString());


//		Pattern p = Pattern.compile("^(?:[^?{]|(?:\\{[a-zA-Z][a-zA-Z0-9]*\\??\\}))+(?:\\?)?$");

		return method;
	}

	public static class MethodModel {
		//		private int index;
		private String pathRegexp;
		private PlaceStringModel contents;
//		private List<ArgModel> args = new ArrayList<>();
//		private Map<String, ArgModel> queryArgs = new HashMap<>();
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
	}
//	public static class ArgModel {
//		private String name;
//		private boolean optional;
//		private boolean queryString;
//		private boolean canContainSlashes;
//
//
//		public ArgModel(String name, boolean optional) {
//			this.name = name;
//			this.optional = optional;
//		}
//
//		public String getName() {
//			return name;
//		}
//
//		public void setName(String name) {
//			this.name = name;
//		}
//
//		public boolean isOptional() {
//			return optional;
//		}
//
//		public void setOptional(boolean optional) {
//			this.optional = optional;
//		}
//
//		public boolean isQueryString() {
//			return queryString;
//		}
//
//		public void setQueryString(boolean queryString) {
//			this.queryString = queryString;
//		}
//
//		public boolean isCanContainSlashes() {
//			return canContainSlashes;
//		}
//
//		public void setCanContainSlashes(boolean canContainSlashes) {
//			this.canContainSlashes = canContainSlashes;
//		}
//	}
}
