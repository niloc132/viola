/**
 * Sencha GXT 3.1.1 - Sencha for GWT
 * Copyright(c) 2007-2014, Sencha, Inc.
 * licensing@sencha.com
 *
 * http://www.sencha.com/products/gxt/license/
 */
package com.sencha.gxt.core.rebind;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.util.Name;
import com.google.gwt.dev.util.Util;
import com.google.gwt.editor.rebind.model.ModelUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.XTemplates.XTemplate;
import com.sencha.gxt.core.rebind.ConditionParser.Token;
import com.sencha.gxt.core.rebind.XTemplateParser.ContainerTemplateChunk;
import com.sencha.gxt.core.rebind.XTemplateParser.ContentChunk;
import com.sencha.gxt.core.rebind.XTemplateParser.ContentChunk.ContentType;
import com.sencha.gxt.core.rebind.XTemplateParser.ControlChunk;
import com.sencha.gxt.core.rebind.XTemplateParser.TemplateChunk;
import com.sencha.gxt.core.rebind.XTemplateParser.TemplateModel;

/**
 * This file is GPL v3 licensed by Sencha, and I've added one minor change here to make it compatible
 * with latest GWT. The rest of the project does not link against it directly, but uses it to compile,
 * so should not be virally relicensed. by it. In the event that I'm mistaken about this, one of the
 * two exemptions added to GPLv3 almost certainly covers this issue, allowing this file to instead be
 * Apache v2 licensed.
 */
public class XTemplatesGenerator extends Generator {
  private JClassType xTemplatesInterface;
  private JClassType listInterface;

  private TreeLogger logger;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    // make sure it is an interface
    TypeOracle oracle = context.getTypeOracle();

    this.logger = logger;

    this.xTemplatesInterface = oracle.findType(Name.getSourceNameForClass(XTemplates.class));
    this.listInterface = oracle.findType(Name.getSourceNameForClass(List.class));
    JClassType toGenerate = oracle.findType(typeName).isInterface();
    if (toGenerate == null) {
      logger.log(TreeLogger.ERROR, typeName + " is not an interface type");
      throw new UnableToCompleteException();
    }
    if (!toGenerate.isAssignableTo(xTemplatesInterface)) {
      logger.log(Type.ERROR, "This isn't a XTemplates subtype...");
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
    factory.addImplementedInterface(typeName);
    // imports
    factory.addImport(Name.getSourceNameForClass(GWT.class));
    factory.addImport(Name.getSourceNameForClass(SafeHtml.class));
    factory.addImport(Name.getSourceNameForClass(SafeHtmlBuilder.class));

    // Loop through the formatters declared for this type and supertypes
    FormatCollector formatters = new FormatCollector(context, logger, toGenerate);
    MethodCollector invokables = new MethodCollector(context, logger, toGenerate);

    SourceWriter sw = factory.createSourceWriter(context, pw);

    for (JMethod method : toGenerate.getOverridableMethods()) {
      TreeLogger l = logger.branch(Type.DEBUG, "Creating XTemplate method " + method.getName());
      final String template;
      XTemplate marker = method.getAnnotation(XTemplate.class);
      if (marker == null) {
        l.log(Type.ERROR, "Unable to create template for method " + method.getReadableDeclaration()
                + ", this may cause other failures.");
        continue;
      } else {
        if (marker.source().length() != 0) {
          if (marker.value().length() != 0) {
            l.log(Type.WARN, "Found both source file and inline template, using source file");
          }

          InputStream stream = getTemplateResource(context, method.getEnclosingType(), l, marker.source());
          if (stream == null) {
            l.log(Type.ERROR, "No data could be loaded - no data at path " + marker.source());
            throw new UnableToCompleteException();
          }
          template = Util.readStreamAsString(stream);
        } else if (marker.value().length() != 0) {
          template = marker.value();
        } else {
          l.log(Type.ERROR,
                  "XTemplate annotation found with no contents, cannot generate method " + method.getName()
                          + ", this may cause other failures.");
          continue;
        }
      }

      XTemplateParser p = new XTemplateParser(l.branch(Type.DEBUG,
              "Parsing provided template for " + method.getReadableDeclaration()));
      TemplateModel m = p.parse(template);
      SafeHtmlTemplatesCreator safeHtml = new SafeHtmlTemplatesCreator(context, l.branch(Type.DEBUG,
              "Building SafeHtmlTemplates"), method);

      sw.println(method.getReadableDeclaration(false, true, true, false, true) + "{");
      sw.indent();

      Map<String, JType> params = new HashMap<String, JType>();
      for (JParameter param : method.getParameters()) {
        params.put(param.getName(), param.getType());
      }
      Context scopeContext = new Context(context, l, params, formatters);
      // if there is only one parameter, wrap the scope up so that properties
      // can be accessed directly
      if (method.getParameters().length == 1) {
        JParameter param = method.getParameters()[0];
        scopeContext = new Context(scopeContext, param.getName(), param.getType());

      }

      String outerSHVar = scopeContext.declareLocalVariable("outer");
      sw.println("SafeHtml %1$s;", outerSHVar);

      buildSafeHtmlTemplates(outerSHVar, sw, m, safeHtml, scopeContext, invokables);

      sw.println("return %1$s;", outerSHVar);

      sw.outdent();
      sw.println("}");

      safeHtml.create();
    }

    // Save the file and return its type name
    sw.commit(logger);
    return factory.getCreatedClassName();
  }

  protected InputStream getTemplateResource(GeneratorContext context, JClassType toGenerate, TreeLogger l,
                                            String markerPath) throws UnableToCompleteException {
    // look for a local file first
    // TODO remove this assumption
    String path = slashify(toGenerate.getPackage().getName()) + "/" + markerPath;

    //BEGIN COLIN'S BUGFIX
    Resource res = context.getResourcesOracle().getResource(path);
    //END COLIN'S BUGFIX

    // if not a local path, try an absolute one
    if (res == null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(markerPath);
      if (url == null) {
        return null;
      }
      try {
        return url.openStream();
      } catch (IOException e) {
        logger.log(Type.ERROR, "IO Exception occured", e);
        throw new UnableToCompleteException();
      }
    }
    try {
      return res.openContents();
    } catch (Exception e) {
      logger.log(Type.ERROR, "Exception occured reading " + path, e);
      throw new UnableToCompleteException();
    }
  }

  private static String slashify(String s) {
    return s.replace(".", "/");
  }

  /**
   * Handles a given template chunk container by creating a method in the
   * safeHtmlTemplates impl
   *
   * @param sw the current sourcewriter
   * @param wrapper the chunk container to act recursively on
   * @param safeHtml creator to add new SafeHtml calls to
   * @param scopeContext current scope to make method calls to
   * @param invokables
   * @throws UnableToCompleteException
   */
  private void buildSafeHtmlTemplates(String safeHtmlVar, SourceWriter sw, ContainerTemplateChunk wrapper,
                                      SafeHtmlTemplatesCreator safeHtml, Context scopeContext, MethodCollector invokables)
          throws UnableToCompleteException {

    // debugging section to see what is about to be printed
    sw.beginJavaDocComment();
    sw.print(wrapper.toString());
    sw.endJavaDocComment();

    // make a new interface method for this content
    StringBuilder sb = new StringBuilder();
    List<String> paramTypes = new ArrayList<String>();
    List<String> params = new ArrayList<String>();

    // write out children to local vars or to the template
    int argCount = 0;
    for (TemplateChunk chunk : wrapper.children) {
      if (chunk instanceof ContentChunk) {
        ContentChunk contentChunk = (ContentChunk) chunk;
        // build up the template
        if (contentChunk.type == ContentType.LITERAL) {
          sb.append(contentChunk.content);
        } else if (contentChunk.type == ContentType.CODE) {
          sb.append("{").append(argCount++).append("}");
          paramTypes.add("java.lang.String");
          StringBuffer expr = new StringBuffer("\"\" + (");

          // parse out the quoted string literals first
          Matcher str = Pattern.compile("\"[^\"]+\"").matcher(contentChunk.content);
          TreeLogger code = logger.branch(Type.DEBUG, "Parsing code segment: \"" + contentChunk.content + "\"");
          int lastMatchEnd = 0;
          while (str.find()) {
            int begin = str.start(), end = str.end();
            String escapedString = str.group();
            String unmatched = contentChunk.content.substring(lastMatchEnd, begin);

            appendCodeBlockOperatorOrIdentifier(scopeContext, expr, code, unmatched);

            expr.append(escapedString);
            lastMatchEnd = end;
          }

          //finish rest of non-string-lit expression
          appendCodeBlockOperatorOrIdentifier(scopeContext, expr, code, contentChunk.content.substring(lastMatchEnd));

          params.add(expr.append(")").toString());
          code.log(Type.DEBUG, "Final compiled expression: " + expr);
        } else if (contentChunk.type == ContentType.REFERENCE) {
          sb.append("{").append(argCount++).append("}");

          JType argType = scopeContext.getType(contentChunk.content);
          if (argType == null) {
            logger.log(Type.ERROR, "Reference could not be found: '" + contentChunk.content + "'. Please fix the expression in your template.");
            throw new UnableToCompleteException();
          }
          paramTypes.add(argType.getParameterizedQualifiedSourceName());
          params.add(scopeContext.deref(contentChunk.content));

        } else {
          assert false : "Content type not supported + " + contentChunk.type;
        }

      } else if (chunk instanceof ControlChunk) {
        ControlChunk controlChunk = (ControlChunk) chunk;
        // build logic, get scoped name
        boolean hasIf = controlChunk.controls.containsKey("if");
        boolean hasFor = controlChunk.controls.containsKey("for");

        if (!hasIf && !hasFor) {
          logger.log(Type.ERROR, "<tpl> tag did not define a 'for' or 'if' attribute!");
          throw new UnableToCompleteException();
        }

        // declare a sub-template, and stash content in there, interleaving it
        // into the current template
        String subTemplate = scopeContext.declareLocalVariable("subTemplate");
        String templateInBlock = scopeContext.declareLocalVariable("innerTemplate");
        sb.append("{").append(argCount++).append("}");
        paramTypes.add("com.google.gwt.safehtml.shared.SafeHtml");
        params.add(subTemplate);
        sw.println("SafeHtml %1$s;", subTemplate);
        sw.println("SafeHtmlBuilder %1$s_builder = new SafeHtmlBuilder();", subTemplate);

        // find the context that should be passed to the child template
        final Context childScope;

        // if we have both for and if, if needs to wrap the for
        if (hasIf) {
          ConditionParser p = new ConditionParser(logger);
          List<Token> tokens = p.parse(controlChunk.controls.get("if"));
          StringBuilder condition = new StringBuilder();
          for (Token t : tokens) {
            switch (t.type) {
              case ExpressionLiteral:
                condition.append(t.contents);
                break;
              case MethodInvocation:
                Matcher invoke = Pattern.compile("([a-zA-Z0-9\\._]+)\\:([a-zA-Z0-9_]+)\\(([^\\)]*)\\)").matcher(
                        t.contents);
                invoke.matches();
                String deref = scopeContext.deref(invoke.group(1));
                String methodName = invoke.group(2);
                String args = "";
                for (String a : invoke.group(3).split(",")) {
                  String possible = scopeContext.deref(a);
                  args += possible == null ? a : possible;
                }

                condition.append(invokables.getMethodInvocation(methodName, deref, args));
                break;
              case Reference:
                condition.append("(").append(scopeContext.deref(t.contents)).append(")");
                break;
              default:
                logger.log(Type.ERROR, "Unexpected token type: " + t.type);
                throw new UnableToCompleteException();
            }
          }
          sw.println("if (%1$s) {", condition.toString());
          sw.indent();
        }
        // if there is a for, print it out, and change scope
        if (hasFor) {
          String loopRef = controlChunk.controls.get("for");

          JType collectionType = scopeContext.getType(loopRef);
          if (collectionType == null) {
            logger.log(Type.ERROR, "Reference in 'for' attribute could not be found: '" + loopRef + "'. Please fix the expression in your template.");
            throw new UnableToCompleteException();
          }
          final JType localType;// type accessed within the loop
          final String localAccessor;// expr to access looped instance, where
          // %1$s is the loop obj, and %2$s is the
          // int index
          if (collectionType.isArray() != null) {
            localType = collectionType.isArray().getComponentType();
            localAccessor = "%1$s[%2$s]";
          } else {// List subtype
            localType = ModelUtils.findParameterizationOf(listInterface, collectionType.isClassOrInterface())[0];
            localAccessor = "%1$s.get(%2$s)";
          }

          String loopVar = scopeContext.declareLocalVariable("i");
          // make sure the collection isnt null
          sw.println("if (%1$s != null) {", scopeContext.deref(loopRef));
          sw.indent();
          sw.println("for (int %1$s = 0; %1$s < %2$s; %1$s++) {", loopVar, scopeContext.derefCount(loopRef));
          String itemExpr = String.format(localAccessor, scopeContext.deref(loopRef), loopVar);
          childScope = new Context(scopeContext, itemExpr, localType);
          childScope.setCountVar(loopVar);
          sw.indent();
        } else {
          // if no for, use the same scope as the outer content
          childScope = scopeContext;
        }
        // generate a subtemplate, insert that
        sw.println("SafeHtml %1$s;", templateInBlock);
        buildSafeHtmlTemplates(templateInBlock, sw, controlChunk, safeHtml, childScope, invokables);
        sw.println("%1$s_builder.append(%2$s);", subTemplate, templateInBlock);

        // close up the blocks
        if (hasFor) {
          sw.outdent();
          sw.println("}");
          sw.outdent();
          sw.println("}");
        }
        if (hasIf) {
          sw.outdent();
          sw.println("}");
        }

        sw.println("%1$s = %1$s_builder.toSafeHtml();", subTemplate);

      } else {
        assert false : "Unsupported chunk type: " + chunk.getClass();
      }
    }

    String methodName = safeHtml.addTemplate(sb.toString(), paramTypes);
    sw.beginJavaDocComment();
    sw.println("safehtml content:");
    sw.indent();
    sw.println(sb.toString());
    sw.outdent();
    sw.println("params:");
    sw.indent();
    sw.print(args(params));
    sw.outdent();
    sw.endJavaDocComment();
    sw.println("%4$s = %1$s.%2$s(%3$s);", safeHtml.getInstanceExpression(), methodName, args(params),
            safeHtmlVar);
  }

  /**
   * Walks the code block string given and replaces all possible variables  (identified by 
   * {@code [a-zA-Z_]+[a-zA-Z0-9_]*(:?\.[a-zA-Z_]+[a-zA-Z0-9_]*)*}) with the deref'd java expression.
   * Any other content (operators, and possibly numeric literals) are appended as is.
   *
   * This potentially will have an issue with {@code null}, {@code true}, {@code false} etc values.
   * However, no earlier version correctly handled those cases, so not going to worry about it for
   * now. The real fix is to stop using just regular expressions and switch to something a little
   * more powerful.
   *
   * @param context the current scope context
   * @param expr the expression being built up, that java content should be appended to
   * @param logger
   * @param nonStringLit the current expression from the xtemplate
   * @throws UnableToCompleteException if something looking like a variable appears that can't be deref'd
   */
  private static void appendCodeBlockOperatorOrIdentifier(Context context, StringBuffer expr, TreeLogger logger, String nonStringLit)
          throws UnableToCompleteException {
    Matcher m = Pattern.compile("(:?[a-zA-Z_]+[a-zA-Z0-9_]*(:?\\.[a-zA-Z_]+[a-zA-Z0-9_]*)*|#)").matcher(nonStringLit);
    while (m.find()) {
      String ref = m.group();
      String deref = context.deref(ref);
      if (deref == null) {
        logger.log(Type.ERROR, "Reference could not be found: '" + ref + "'.");
        throw new UnableToCompleteException();
      }
      logger.log(Type.DEBUG, "Replaced " + ref + " with " + deref);
      m.appendReplacement(expr, deref);
    }
    m.appendTail(expr);
  }

  /**
   * Builds an arg list ready to be passed into a method invocation. Effectively
   * is params.join(', ')
   *
   * @param params
   */
  private String args(List<String> params) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(params.get(i));
    }
    return sb.toString();
  }
}
