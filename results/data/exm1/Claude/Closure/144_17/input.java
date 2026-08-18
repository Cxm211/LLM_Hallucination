// buggy code
  FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) :
        typeRegistry.getNativeType(UNKNOWN_TYPE);
    if (templateTypeName != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType()) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }

  FunctionType buildAndRegister() {
    if (returnType == null) {
      returnType = typeRegistry.getNativeType(UNKNOWN_TYPE);
    }

    if (parametersNode == null) {
      throw new IllegalStateException(
          "All Function types must have params and a return type");
    }

    FunctionType fnType;
    if (isConstructor) {
      fnType = getOrCreateConstructor();
    } else if (isInterface) {
      fnType = typeRegistry.createInterfaceType(fnName, sourceNode);
      if (scope.isGlobal() && !fnName.isEmpty()) {
        typeRegistry.declareType(fnName, fnType.getInstanceType());
      }
      maybeSetBaseType(fnType);
    } else {
      fnType = new FunctionBuilder(typeRegistry)
          .withName(fnName)
          .withSourceNode(sourceNode)
          .withParamsNode(parametersNode)
          .withReturnType(returnType)
          .withTypeOfThis(thisType)
          .withTemplateName(templateTypeName)
          .build();
      maybeSetBaseType(fnType);
    }

    if (implementedInterfaces != null) {
      fnType.setImplementedInterfaces(implementedInterfaces);
    }

    typeRegistry.clearTemplateTypeName();

    return fnType;
  }

    private FunctionType getFunctionType(String name,
        Node rValue, JSDocInfo info, @Nullable Node lvalueNode) {
      FunctionType functionType = null;

      // Handle function aliases.
      if (rValue != null && rValue.isQualifiedName()) {
        Var var = scope.getVar(rValue.getQualifiedName());
        if (var != null && var.getType() instanceof FunctionType) {
          functionType = (FunctionType) var.getType();
          if (functionType != null && functionType.isConstructor()) {
            typeRegistry.declareType(name, functionType.getInstanceType());
          }
        }
        return functionType;
      }

      Node owner = null;
      if (lvalueNode != null) {
        owner = getPrototypePropertyOwner(lvalueNode);
      }

      Node errorRoot = rValue == null ? lvalueNode : rValue;
      boolean isFnLiteral =
          rValue != null && rValue.getType() == Token.FUNCTION;
      Node fnRoot = isFnLiteral ? rValue : null;
      Node parametersNode = isFnLiteral ?
          rValue.getFirstChild().getNext() : null;

      if (functionType == null && info != null && info.hasType()) {
        JSType type = info.getType().evaluate(scope, typeRegistry);

        // Known to be not null since we have the FUNCTION token there.
        type = type.restrictByNotNullOrUndefined();
        if (type.isFunctionType()) {
          functionType = (FunctionType) type;
          functionType.setJSDocInfo(info);
        }
      }

      if (functionType == null) {
        if (info == null ||
            !FunctionTypeBuilder.isFunctionTypeDeclaration(info)) {
          // We don't really have any type information in the annotation.
          // Before we give up on this function, look at the object we're
          // assigning it to. For example, if the function looks like this:
          // SubFoo.prototype.bar = function() { ... };
          // We can use type information on Foo.prototype.bar and apply it
          // to this function.
          if (lvalueNode != null && lvalueNode.getType() == Token.GETPROP &&
              lvalueNode.isQualifiedName()) {
            Var var = scope.getVar(
                lvalueNode.getFirstChild().getQualifiedName());
            if (var != null) {
              ObjectType ownerType = ObjectType.cast(var.getType());
              FunctionType propType = null;
              if (ownerType != null) {
                String propName = lvalueNode.getLastChild().getString();
                propType = findOverriddenFunction(ownerType, propName);
              }

              if (propType != null) {
                functionType =
                    new FunctionTypeBuilder(
                        name, compiler, errorRoot, sourceName, scope)
                    .setSourceNode(fnRoot)
                    .inferFromOverriddenFunction(propType, parametersNode)
                    .inferThisType(info, owner)
                    .buildAndRegister();
              }
            }
          }
        }
      } // end if (functionType == null)

      if (functionType == null) {
        functionType =
            new FunctionTypeBuilder(name, compiler, errorRoot, sourceName,
                scope)
            .setSourceNode(fnRoot)
            .inferTemplateTypeName(info)
            .inferReturnType(info)
            .inferInheritance(info)
            .inferThisType(info, owner)
            .inferParameterTypes(parametersNode, info)
            .buildAndRegister();
      }

      // assigning the function type to the function node
      if (rValue != null) {
        setDeferredType(rValue, functionType);
      }

      // all done
      return functionType;
    }

  public FunctionBuilder withReturnType(JSType returnType) {
    this.returnType = returnType;
    return this;
  }

  FunctionType cloneWithNewReturnType(JSType newReturnType, boolean inferred) {
    return new FunctionType(
        registry, null, null,
        new ArrowType(
            registry, call.parameters, newReturnType, inferred),
        typeOfThis, null, false, false);
  }

// relevant test
// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError2
  public void testParseParamError2() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError3
  public void testParseParamError3() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError4
  public void testParseParamError4() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError5
  public void testParseParamError5() throws Exception {
    parse("@param {number} x \n * @param {string} x */",
        "duplicate variable name \"x\"");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends1
  public void testParseExtends1() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
                     parse("@extends String*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends2
  public void testParseExtends2() throws Exception {
    JSDocInfo info = parse("@extends com.google.Foo.Bar.Hello.World*/");
    assertTypeEquals(
        registry.createNamedType(
            "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsGenerics
  public void testParseExtendsGenerics() throws Exception {
    JSDocInfo info =
        parse("@extends com.google.Foo.Bar.Hello.World.<Boolean,number>*/");
    assertTypeEquals(
        registry.createNamedType(
            "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsGenerics
  public void testParseImplementsGenerics() throws Exception {
    
    List<JSTypeExpression> interfaces =
        parse("@implements {SomeInterface.<*>} */")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface", null, -1, -1),
        interfaces.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends4
  public void testParseExtends4() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends5
  public void testParseExtends5() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends {String*/", "expected closing }").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends6
  public void testParseExtends6() throws Exception {
    
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends \n * {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsInvalidName
  public void testParseExtendsInvalidName() throws Exception {
    
    
    
    
    assertTypeEquals(
        registry.createNamedType("some_++#%$%_UglyString", null, -1, -1),
        parse("@extends {some_++#%$%_UglyString} */").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable1
  public void testParseExtendsNullable1() throws Exception {
    parse("@extends {Base?} */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable2
  public void testParseExtendsNullable2() throws Exception {
    parse("@extends Base? */", "expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum1
  public void testParseEnum1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@enum*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum2
  public void testParseEnum2() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@enum {string}*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum3
  public void testParseEnum3() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@enum string*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc1
  public void testParseDesc1() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello world!*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc2
  public void testParseDesc2() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello world!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc3
  public void testParseDesc3() throws Exception {
    assertEquals("", parse("@desc*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc4
  public void testParseDesc4() throws Exception {
    assertEquals("", parse("@desc\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc5
  public void testParseDesc5() throws Exception {
    assertEquals("hello world!",
                 parse("@desc hello\nworld!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc6
  public void testParseDesc6() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello\n* world!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc7
  public void testParseDesc7() throws Exception {
    assertEquals("a b c", parse("@desc a\n\nb\nc*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc8
  public void testParseDesc8() throws Exception {
    assertEquals("a b c d",
        parse("@desc a\n      *b\n\n  *c\n\nd*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc9
  public void testParseDesc9() throws Exception {
    String comment = "@desc\n.\n,\n{\n)\n}\n|\n.<\n>\n<\n?\n~\n+\n-\n;\n:\n*/";

    assertEquals(". , { ) } | .< > < ? ~ + - ; :",
        parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc10
  public void testParseDesc10() throws Exception {
    String comment = "@desc\n?\n?\n?\n?*/";

    assertEquals("? ? ? ?", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc11
  public void testParseDesc11() throws Exception {
    String comment = "@desc :[]*/";

    assertEquals(":[]", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc12
  public void testParseDesc12() throws Exception {
    String comment = "@desc\n:\n[\n]\n...*/";

    assertEquals(": [ ] ...", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParsePreserve
  public void testParsePreserve() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@preserve Foo\nBar\n\nBaz*/";
    parse(comment);
    assertEquals(" Foo\n Bar\n\n Baz", node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicense
  public void testParseLicense() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo\nBar\n\nBaz*/";
    parse(comment);
    assertEquals(" Foo\n Bar\n\n Baz", node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicenseWithAnnotation
  public void testParseLicenseWithAnnotation() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo \n * @author Charlie Brown */";
    parse(comment);
    assertEquals(" Foo \n @author Charlie Brown ",
        node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine1
  public void testParseDefine1() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@define {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine2
  public void testParseDefine2() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@define {string*/", "expected closing }").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine3
  public void testParseDefine3() throws Exception {
    JSDocInfo info = parse("@define {boolean}*/");
    assertTrue(info.isConstant());
    assertTrue(info.isDefine());
    assertTypeEquals(BOOLEAN_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine4
  public void testParseDefine4() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@define {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine5
  public void testParseDefine5() throws Exception {
    assertTypeEquals(createUnionType(NUMBER_TYPE, BOOLEAN_TYPE),
        parse("@define {number|boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors1
  public void testParseDefineErrors1() throws Exception {
    parse("@enum {string}\n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors2
  public void testParseDefineErrors2() throws Exception {
    parse("@define {string}\n @enum {string} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors3
  public void testParseDefineErrors3() throws Exception {
    parse("@const\n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors4
  public void testParseDefineErrors4() throws Exception {
    parse("@type string \n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors5
  public void testParseDefineErrors5() throws Exception {
    parse("@return {string}\n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors7
  public void testParseDefineErrors7() throws Exception {
    parse("@define {string}\n @const */", "conflicting @const tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors8
  public void testParseDefineErrors8() throws Exception {
    parse("@define {string}\n @type string */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCheck1
  public void testParseNoCheck1() throws Exception {
    assertTrue(parse("@notypecheck*/").isNoTypeCheck());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCheck2
  public void testParseNoCheck2() throws Exception {
    parse("@notypecheck\n@notypecheck*/", "extra @notypecheck tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOverride1
  public void testParseOverride1() throws Exception {
    assertTrue(parse("@override*/").isOverride());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOverride2
  public void testParseOverride2() throws Exception {
    parse("@override\n@override*/", "extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc1
  public void testParseInheritDoc1() throws Exception {
    assertTrue(parse("@inheritDoc*/").isOverride());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc2
  public void testParseInheritDoc2() throws Exception {
    parse("@override\n@inheritDoc*/", "extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc3
  public void testParseInheritDoc3() throws Exception {
    parse("@inheritDoc\n@inheritDoc*/", "extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoAlias1
  public void testParseNoAlias1() throws Exception {
    assertTrue(parse("@noalias*/").isNoAlias());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoAlias2
  public void testParseNoAlias2() throws Exception {
    parse("@noalias\n@noalias*/", "extra @noalias tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDeprecated1
  public void testParseDeprecated1() throws Exception {
    assertTrue(parse("@deprecated*/").isDeprecated());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDeprecated2
  public void testParseDeprecated2() throws Exception {
    parse("@deprecated\n@deprecated*/", "extra @deprecated tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExport1
  public void testParseExport1() throws Exception {
    assertTrue(parse("@export*/").isExport());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExport2
  public void testParseExport2() throws Exception {
    parse("@export\n@export*/", "extra @export tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExterns1
  public void testParseExterns1() throws Exception {
    assertTrue(parseFileOverview("@externs*/").isExterns());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExterns2
  public void testParseExterns2() throws Exception {
    parseFileOverview("@externs\n@externs*/", "extra @externs tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExterns3
  public void testParseExterns3() throws Exception {
    assertNull(parse("@externs*/"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJavaDispatch1
  public void testParseJavaDispatch1() throws Exception {
    assertTrue(parse("@javadispatch*/").isJavaDispatch());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJavaDispatch2
  public void testParseJavaDispatch2() throws Exception {
    parse("@javadispatch\n@javadispatch*/",
        "extra @javadispatch tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJavaDispatch3
  public void testParseJavaDispatch3() throws Exception {
    assertNull(parseFileOverview("@javadispatch*/"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCompile1
  public void testParseNoCompile1() throws Exception {
    assertTrue(parseFileOverview("@nocompile*/").isNoCompile());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCompile2
  public void testParseNoCompile2() throws Exception {
    parseFileOverview("@nocompile\n@nocompile*/", "extra @nocompile tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBugAnnotation
  public void testBugAnnotation() throws Exception {
    parse("@bug */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDescriptionAnnotation
  public void testDescriptionAnnotation() throws Exception {
    parse("@description */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression1
  public void testRegression1() throws Exception {
    String comment =
        " * @param {number} index the index of blah\n" +
        " * @return {boolean} whatever\n" +
        " * @private\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
    assertTypeEquals(BOOLEAN_TYPE, info.getReturnType());
    assertEquals(Visibility.PRIVATE, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression2
  public void testRegression2() throws Exception {
    String comment =
        " * @return {boolean} whatever\n" +
        " * but important\n" +
        " *\n" +
        " * @param {number} index the index of blah\n" +
        " * some more comments here\n" +
        " * @param name the name of the guy\n" +
        " *\n" +
        " * @protected\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(2, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
    assertEquals(null, info.getParameterType("name"));
    assertTypeEquals(BOOLEAN_TYPE, info.getReturnType());
    assertEquals(Visibility.PROTECTED, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression3
  public void testRegression3() throws Exception {
    String comment =
        " * @param mediaTag this specified whether the @media tag is ....\n" +
        " *\n" +
        "\n" +
        "@public\n" +
        " *\n" +
        "\n" +
        " **********\n" +
        " * @final\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("mediaTag"));
    assertEquals(Visibility.PUBLIC, info.getVisibility());
    assertTrue(info.isConstant());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression4
  public void testRegression4() throws Exception {
    String comment =
        " * @const\n" +
        " * @hidden\n" +
        " * @preserveTry\n" +
        " * @constructor\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertTrue(info.isConstant());
    assertFalse(info.isDefine());
    assertTrue(info.isConstructor());
    assertTrue(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression5
  public void testRegression5() throws Exception {
    String comment = "@const\n@enum {string}\n@public*/";

    JSDocInfo info = parse(comment);
    assertTrue(info.isConstant());
    assertFalse(info.isDefine());
    assertTypeEquals(STRING_TYPE, info.getEnumParameterType());
    assertEquals(Visibility.PUBLIC, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression6
  public void testRegression6() throws Exception {
    String comment = "@hidden\n@enum\n@public*/";

    JSDocInfo info = parse(comment);
    assertTrue(info.isHidden());
    assertTypeEquals(NUMBER_TYPE, info.getEnumParameterType());
    assertEquals(Visibility.PUBLIC, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression7
  public void testRegression7() throws Exception {
    String comment =
        " * @desc description here\n" +
        " * @param {boolean} flag and some more description\n" +
        " *     nicely formatted\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(BOOLEAN_TYPE, info.getParameterType("flag"));
    assertEquals("description here", info.getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression8
  public void testRegression8() throws Exception {
    String comment =
        " * @name random tag here\n" +
        " * @desc description here\n" +
        " *\n" +
        " * @param {boolean} flag and some more description\n" +
        " *     nicely formatted\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(BOOLEAN_TYPE, info.getParameterType("flag"));
    assertEquals("description here", info.getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression9
  public void testRegression9() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @param {string} p0 blah blah blah\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(1, jsdoc.getParameterCount());
    assertTypeEquals(STRING_TYPE, jsdoc.getParameterType("p0"));
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression10
  public void testRegression10() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @param {!String} p0 blah blah blah\n" +
        " * @param {boolean} p1 fobar\n" +
        " * @return {!Date} jksjkash dshad\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(2, jsdoc.getParameterCount());
    assertTypeEquals(STRING_OBJECT_TYPE, jsdoc.getParameterType("p0"));
    assertTypeEquals(BOOLEAN_TYPE, jsdoc.getParameterType("p1"));
    assertTypeEquals(DATE_TYPE, jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression11
  public void testRegression11() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @constructor\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression12
  public void testRegression12() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @extends FooBar\n" +
        " */");

    assertTypeEquals(registry.createNamedType("FooBar", null, 0, 0),
        jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression13
  public void testRegression13() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @type {!RegExp}\n" +
        " * @protected\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertTypeEquals(REGEXP_TYPE, jsdoc.getType());
    assertEquals(Visibility.PROTECTED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression14
  public void testRegression14() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @const\n" +
        " * @private\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertTrue(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.PRIVATE, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression15
  public void testRegression15() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @desc Hello,\n" +
        " * World!\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertEquals("Hello, World!", jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
    assertFalse(jsdoc.isExport());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression16
  public void testRegression16() throws Exception {
    JSDocInfo jsdoc = parse(
        " Email is plp@foo.bar\n" +
        " @type {string}\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression17
  public void testRegression17() throws Exception {
    
    assertNull(parse("@private*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression1
  public void testFullRegression1() throws Exception {
    parseFull("function bar(foo){}",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression2
  public void testFullRegression2() throws Exception {
    parseFull("function bar(foo){}",
        "expected closing }",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression3
  public void testFullRegression3() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907488
  public void testBug907488() throws Exception {
    parse("@type {number,null} */",
        "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907494
  public void testBug907494() throws Exception {
    parse("@return {Object,undefined} */",
        "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug909468
  public void testBug909468() throws Exception {
    parse("@extends {(x)}*/",
        "expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterface
  public void testParseInterface() throws Exception {
    assertTrue(parse("@interface*/").isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplicitCast1
  public void testParseImplicitCast1() throws Exception {
    assertTrue(parse("@type {string} \n * @implicitCast*/").isImplicitCast());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplicitCast2
  public void testParseImplicitCast2() throws Exception {
    assertFalse(parse("@type {string}*/").isImplicitCast());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDuplicateImplicitCast
  public void testParseDuplicateImplicitCast() throws Exception {
    parse("@type {string} \n * @implicitCast \n * @implicitCast*/",
          "extra @implicitCast tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterfaceDoubled
  public void testParseInterfaceDoubled() throws Exception {
    parse(
        "* @interface\n" +
        "* @interface\n" +
        "*/",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplements
  public void testParseImplements() throws Exception {
    List<JSTypeExpression> interfaces = parse("@implements {SomeInterface}*/")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface", null, -1, -1),
        interfaces.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsTwo
  public void testParseImplementsTwo() throws Exception {
    List<JSTypeExpression> interfaces =
        parse(
            "* @implements {SomeInterface1}\n" +
            "* @implements {SomeInterface2}\n" +
            "*/")
        .getImplementedInterfaces();
    assertEquals(2, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface1", null, -1, -1),
        interfaces.get(0));
    assertTypeEquals(registry.createNamedType("SomeInterface2", null, -1, -1),
        interfaces.get(1));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsSameTwice
  public void testParseImplementsSameTwice() throws Exception {
    parse(
        "* @implements {Smth}\n" +
        "* @implements {Smth}\n" +
        "*/",
        "duplicate @implements tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNoName
  public void testParseImplementsNoName() throws Exception {
    parse("* @implements {} */",
        "expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsMissingRC
  public void testParseImplementsMissingRC() throws Exception {
    parse("* @implements {Smth */",
        "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable1
  public void testParseImplementsNullable1() throws Exception {
    parse("@implements {Base?} */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable2
  public void testParseImplementsNullable2() throws Exception {
    parse("@implements Base? */", "expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
     JSDocInfo jsdoc = parse(
         " * @interface \n" +
         " * @extends {Extended} */");
    assertTrue(jsdoc.isInterface());
    assertTypeEquals(registry.createNamedType("Extended", null, -1, -1),
        jsdoc.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadExtendsWithNullable
  public void testBadExtendsWithNullable() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n * @extends {Object?} */",
        "expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(OBJECT_TYPE, jsdoc.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadImplementsWithNullable
  public void testBadImplementsWithNullable() throws Exception {
  JSDocInfo jsdoc = parse("@implements {Disposable?}\n * @constructor */",
      "expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(registry.createNamedType("Disposable", null, -1, -1),
        jsdoc.getImplementedInterfaces().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor1
  public void testBadTypeDefInterfaceAndConstructor1() throws Exception {
    JSDocInfo jsdoc = parse("@interface\n@constructor*/",
        "cannot be both an interface and a constructor");
    assertTrue(jsdoc.isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor2
  public void testBadTypeDefInterfaceAndConstructor2() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n@interface*/",
        "cannot be both an interface and a constructor");
    assertTrue(jsdoc.isConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationParameter
  public void testDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMultilineDocumentationParameter
  public void testMultilineDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description"
                + "\n* on multiple \n* lines.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description on multiple lines.",
                 jsdoc.getDescriptionForParameter("number42"));

  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter
  public void testDocumentationMultipleParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description.*/"
                , true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));

    assertTrue(jsdoc.hasDescriptionForParameter("number87"));
    assertEquals("This is another description.",
                 jsdoc.getDescriptionForParameter("number87"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter2
  public void testDocumentationMultipleParameter2() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {number} delta = 0 results in a redraw\n" +
                "  != 0 ..... */", true);
    assertTrue(jsdoc.hasDescriptionForParameter("delta"));
    assertEquals("= 0 results in a redraw != 0 .....",
                 jsdoc.getDescriptionForParameter("delta"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthors
  public void testAuthors() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description."
                + "\n* @author a@google.com (A Person)"
                + "\n* @author b@google.com (B Person)"
                + "\n* @author c@google.com (C Person)*/"
                , true);

    Collection<String> authors = jsdoc.getAuthors();

    assertTrue(authors != null);
    assertTrue(authors.size() == 3);

    assertContains(authors, "a@google.com (A Person)");
    assertContains(authors, "b@google.com (B Person)");
    assertContains(authors, "c@google.com (C Person)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress1
  public void testSuppress1() throws Exception {
    JSDocInfo info = parse("@suppress {x} */");
    assertEquals(Sets.newHashSet("x"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress2
  public void testSuppress2() throws Exception {
    JSDocInfo info = parse("@suppress {x|y|x|z} */");
    assertEquals(Sets.newHashSet("x", "y", "z"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress1
  public void testBadSuppress1() throws Exception {
    parse("@suppress {} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress2
  public void testBadSuppress2() throws Exception {
    parse("@suppress {x|} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress3
  public void testBadSuppress3() throws Exception {
    parse("@suppress {|x} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress4
  public void testBadSuppress4() throws Exception {
    parse("@suppress {x|y */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress5
  public void testBadSuppress5() throws Exception {
    parse("@suppress {x,y} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress6
  public void testBadSuppress6() throws Exception {
    parse("@suppress {x} \n * @suppress {y} */", "duplicate @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress7
  public void testBadSuppress7() throws Exception {
    parse("@suppress {impossible} */",
          "unknown @suppress parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewSingleLine
  public void testFileOverviewSingleLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Hi mom! */");
    assertEquals("Hi mom!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewMultiLine
  public void testFileOverviewMultiLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Pie is \n * good! */");
    assertEquals("Pie is\n good!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewDuplicate
  public void testFileOverviewDuplicate() throws Exception {
    JSDocInfo jsdoc = parseFileOverview(
        "@fileoverview Pie \n * @fileoverview Cake */",
        "extra @fileoverview tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testReferences
  public void testReferences() throws Exception {
    JSDocInfo jsdoc
        = parse("@see A cool place!"
                + "\n* @see The world."
                + "\n* @see SomeClass#SomeMember"
                + "\n* @see A boring test case*/"
                , true);

    Collection<String> references = jsdoc.getReferences();

    assertTrue(references != null);
    assertTrue(references.size() == 4);

    assertContains(references, "A cool place!");
    assertContains(references, "The world.");
    assertContains(references, "SomeClass#SomeMember");
    assertContains(references, "A boring test case");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTags
  public void testSingleTags() throws Exception {
    JSDocInfo jsdoc
        = parse("@version Some old version"
                + "\n* @deprecated In favor of the new one!"
                + "\n* @return {SomeType} The most important object :-)*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTagsReordered
  public void testSingleTagsReordered() throws Exception {
    JSDocInfo jsdoc
        = parse("@deprecated In favor of the new one!"
                + "\n * @return {SomeType} The most important object :-)"
                + "\n * @version Some old version*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionDuplication
  public void testVersionDuplication() throws Exception {
    parse("* @version Some old version"
          + "\n* @version Another version*/", true,
          "conflicting @version tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionMissing
  public void testVersionMissing() throws Exception {
    parse("* @version */", true,
          "@version tag missing version information");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthorMissing
  public void testAuthorMissing() throws Exception {
    parse("* @author */", true,
          "@author tag missing author");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSeeMissing
  public void testSeeMissing() throws Exception {
    parse("* @see */", true,
          "@see tag missing description");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSourceName
  public void testSourceName() throws Exception {
    JSDocInfo jsdoc = parse("@deprecated */", true);
    assertEquals("testcode", jsdoc.getSourceName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment
  public void testParseBlockComment() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment2
  public void testParseBlockComment2() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is *** multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is *** multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment3
  public void testParseBlockComment3() throws Exception {
    JSDocInfo jsdoc = parse("\n "
                            + "* hello world \n"
                            + "* @author abc@google.com */", true);

    assertEquals("hello world", jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers1
  public void testParseWithMarkers1() throws Exception {
    JSDocInfo jsdoc = parse("@author abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 0, 0),
        "abc@google.com", 7, 0, 21);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers2
  public void testParseWithMarkers2() throws Exception {
    JSDocInfo jsdoc = parse("@param {Foo} somename abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "abc@google.com", 21, 0, 37);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers3
  public void testParseWithMarkers3() throws Exception {
    JSDocInfo jsdoc =
        parse("@return {Foo} some long \n * multiline" +
              " \n * description */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "return", 0, 0),
        "some long multiline description", 13, 2, 15);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers4
  public void testParseWithMarkers4() throws Exception {
    JSDocInfo jsdoc =
        parse("@author foobar \n * @param {Foo} somename abc@google.com */",
              true);

    assertAnnotationMarker(jsdoc, "author", 0, 0);
    assertAnnotationMarker(jsdoc, "param", 1, 3);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames1
  public void testParseWithMarkerNames1() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription */", true);

    assertNameInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "name", 18);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames2
  public void testParseWithMarkerNames2() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription \n" +
                            "* @param {AnotherType} anothername des */", true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 0, 0, 0),
            "name", 18),
        "SomeType", 7, true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 1, 2, 1),
            "anothername", 23),
        "AnotherType", 9, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithoutMarkerName
  public void testParseWithoutMarkerName() throws Exception {
    JSDocInfo jsdoc = parse("@author helloworld*/", true);
    assertNull(assertAnnotationMarker(jsdoc, "author", 0, 0).name);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType
  public void testParseWithMarkerType() throws Exception {
    JSDocInfo jsdoc = parse("@extends {FooBar}*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 9, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType2
  public void testParseWithMarkerType2() throws Exception {
    JSDocInfo jsdoc = parse("@extends FooBar*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 9, false);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict1
  public void testTypeTagConflict1() throws Exception {
    parse("@constructor \n * @constructor */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict2
  public void testTypeTagConflict2() throws Exception {
    parse("@interface \n * @interface */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict3
  public void testTypeTagConflict3() throws Exception {
    parse("@constructor \n * @interface */",
        "cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict4
  public void testTypeTagConflict4() throws Exception {
    parse("@interface \n * @constructor */",
        "cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict5
  public void testTypeTagConflict5() throws Exception {
    parse("@interface \n * @type {string} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict6
  public void testTypeTagConflict6() throws Exception {
    parse("@typedef {string} \n * @type {string} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict7
  public void testTypeTagConflict7() throws Exception {
    parse("@typedef {string} \n * @constructor */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict8
  public void testTypeTagConflict8() throws Exception {
    parse("@typedef {string} \n * @return {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict9
  public void testTypeTagConflict9() throws Exception {
    parse("@enum {string} \n * @return {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict10
  public void testTypeTagConflict10() throws Exception {
    parse("@this {Object} \n * @enum {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict11
  public void testTypeTagConflict11() throws Exception {
    parse("@param {Object} x \n * @type {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict12
  public void testTypeTagConflict12() throws Exception {
    parse("@typedef {boolean} \n * @param {Object} x */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict13
  public void testTypeTagConflict13() throws Exception {
    parse("@typedef {boolean} \n * @extends {Object} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateTypeNameMisisng
  public void testParserWithTemplateTypeNameMisisng() {
    parse("@template */", "@template tag missing type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateTypeNameMissing
  public void testParserWithTemplateTypeNameMissing() {
    parse("@template T\n@template V */", "@template tag at most once");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedNewAnnotations
  public void testWhitelistedNewAnnotations() {
    parse("@foobar */",
        "illegal use of unknown JSDoc tag \"foobar\"; ignoring it");
    extraAnnotations.add("foobar");
    parse("@foobar */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedConflictingAnnotation
  public void testWhitelistedConflictingAnnotation() {
    extraAnnotations.add("param");
    JSDocInfo info = parse("@param {number} index */");
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNonIdentifierAnnotation
  public void testNonIdentifierAnnotation() {
    
    
    extraAnnotations.add("123");
    parse("@123 */", "illegal use of unknown JSDoc tag \"\"; ignoring it");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax1
  public void testUnsupportedJsDocSyntax1() {
    JSDocInfo info =
        parse("@param {string} [accessLevel=\"author\"] The user level */",
            true);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("accessLevel"));
    assertEquals("The user level",
        info.getDescriptionForParameter("accessLevel"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax2
  public void testUnsupportedJsDocSyntax2() {
    JSDocInfo info =
        parse("@param userInfo The user info. \n" +
              " * @param userInfo.name The name of the user */", true);
    assertEquals(1, info.getParameterCount());
    assertEquals("The user info.",
        info.getDescriptionForParameter("userInfo"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedAnnotations
  public void testWhitelistedAnnotations() {
    parse(
      "* @addon \n" +
      "* @augments \n" +
      "* @base \n" +
      "* @borrows \n" +
      "* @bug \n" +
      "* @class \n" +
      "* @config \n" +
      "* @constructs \n" +
      "* @default \n" +
      "* @description \n" +
      "* @event \n" +
      "* @example \n" +
      "* @exception \n" +
      "* @exec \n" +
      "* @externs \n" +
      "* @field \n" +
      "* @function \n" +
      "* @id \n" +
      "* @ignore \n" +
      "* @inner \n" +
      "* @lends \n" +
      "* @link \n" +
      "* @member \n" +
      "* @memberOf \n" +
      "* @modName \n" +
      "* @mods \n" +
      "* @name \n" +
      "* @namespace \n" +
      "* @nocompile \n" +
      "* @property \n" +
      "* @requires \n" +
      "* @since \n" +
      "* @static \n" +
      "* @supported */");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign1
  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(2, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getLineno());
    assertEquals(12, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(4, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp1
  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(5, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp2
  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(3, name.getLineno());
    assertEquals(0, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem1
  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem2
  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem3
  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(2, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoForComparison
  public void testLinenoCharnoForComparison() throws Exception {
    Node lt =
      parse("for (; i < j;){}").getFirstChild().getFirstChild().getNext();

    assertEquals(Token.LT, lt.getType());
    assertEquals(1, lt.getLineno());
    assertEquals(9, lt.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoHook
  public void testLinenoCharnoHook() throws Exception {
    Node n = parse("\n a ? 9 : 0").getFirstChild().getFirstChild();

    assertEquals(Token.HOOK, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(1, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoArrayLiteral
  public void testLinenoCharnoArrayLiteral() throws Exception {
    Node n = parse("\n  [8, 9]").getFirstChild().getFirstChild();

    assertEquals(Token.ARRAYLIT, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(2, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(3, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(6, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoObjectLiteral
  public void testLinenoCharnoObjectLiteral() throws Exception {
    Node n = parse("\n\n var a = {a:0\n,b :1};")
        .getFirstChild().getFirstChild().getFirstChild();

    assertEquals(Token.OBJECTLIT, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(9, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.STRING, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(10, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(12, n.getCharno());

    n = n.getNext();

    assertEquals(Token.STRING, n.getType());
    assertEquals(4, n.getLineno());
    assertEquals(1, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(4, n.getLineno());
    assertEquals(4, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAdd
  public void testLinenoCharnoAdd() throws Exception {
    testLinenoCharnoBinop("+");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoSub
  public void testLinenoCharnoSub() throws Exception {
    testLinenoCharnoBinop("-");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMul
  public void testLinenoCharnoMul() throws Exception {
    testLinenoCharnoBinop("*");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoDiv
  public void testLinenoCharnoDiv() throws Exception {
    testLinenoCharnoBinop("/");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMod
  public void testLinenoCharnoMod() throws Exception {
    testLinenoCharnoBinop("%");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoShift
  public void testLinenoCharnoShift() throws Exception {
    testLinenoCharnoBinop("<<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryAnd
  public void testLinenoCharnoBinaryAnd() throws Exception {
    testLinenoCharnoBinop("&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAnd
  public void testLinenoCharnoAnd() throws Exception {
    testLinenoCharnoBinop("&&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryOr
  public void testLinenoCharnoBinaryOr() throws Exception {
    testLinenoCharnoBinop("|");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoOr
  public void testLinenoCharnoOr() throws Exception {
    testLinenoCharnoBinop("||");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLt
  public void testLinenoCharnoLt() throws Exception {
    testLinenoCharnoBinop("<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLe
  public void testLinenoCharnoLe() throws Exception {
    testLinenoCharnoBinop("<=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGt
  public void testLinenoCharnoGt() throws Exception {
    testLinenoCharnoBinop(">");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGe
  public void testLinenoCharnoGe() throws Exception {
    testLinenoCharnoBinop(">=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment1
  public void testJSDocAttachment1() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment2
  public void testJSDocAttachment2() {
    Node varNode = parse("var a,b;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode1 = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode1.getType());
    assertNull(nameNode1.getJSDocInfo());

    
    Node nameNode2 = nameNode1.getNext();
    assertEquals(Token.NAME, nameNode2.getType());
    assertNull(nameNode2.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment3
  public void testJSDocAttachment3() {
    Node assignNode = parse(
        "goog.FOO = 5;").getFirstChild().getFirstChild();

    
    assertEquals(Token.ASSIGN, assignNode.getType());
    JSDocInfo info = assignNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment4
  public void testJSDocAttachment4() {
    Node varNode = parse(
        "var a, b = 5;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNull(a.getJSDocInfo());

    
    Node b = a.getNext();
    JSDocInfo info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment5
  public void testJSDocAttachment5() {
    Node varNode = parse(
        "var a, b = 5;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNotNull(a.getJSDocInfo());
    JSDocInfo info = a.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node b = a.getNext();
    info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment6
  public void testJSDocAttachment6() throws Exception {
    Node functionNode = parse(
        "var a = 5;" +
        "function f(index){}")
        .getFirstChild().getNext();

    assertEquals(Token.FUNCTION, functionNode.getType());
    JSDocInfo info = functionNode.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.hasParameter("index"));
    assertTrue(info.hasReturnType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment7
  public void testJSDocAttachment7() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment8
  public void testJSDocAttachment8() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment9
  public void testJSDocAttachment9() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment10
  public void testJSDocAttachment10() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment11
  public void testJSDocAttachment11() {
    Node varNode =
       parse("var a;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);

    assertTypeEquals(createRecordTypeBuilder().
                     addProperty("x", NUMBER_TYPE).
                     addProperty("y", STRING_TYPE).
                     addProperty("z", UNKNOWN_TYPE).
                     build(),
                     info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment12
  public void testJSDocAttachment12() {
    Node varNode =
       parse("var a = { b: c};")
        .getFirstChild();
    Node objectLitNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLitNode.getType());
    assertNotNull(objectLitNode.getFirstChild().getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing1
  public void testIncorrectJSDocDoesNotAlterJSParsing1() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing2
  public void testIncorrectJSDocDoesNotAlterJSParsing2() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing3
  public void testIncorrectJSDocDoesNotAlterJSParsing3() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing4
  public void testIncorrectJSDocDoesNotAlterJSParsing4() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing5
  public void testIncorrectJSDocDoesNotAlterJSParsing5() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing6
  public void testIncorrectJSDocDoesNotAlterJSParsing6() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "expected closing }",
              "expecting a variable name in a @param tag"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing7
  public void testIncorrectJSDocDoesNotAlterJSParsing7() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@see tag missing description"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing8
  public void testIncorrectJSDocDoesNotAlterJSParsing8() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@author tag missing author"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing9
  public void testIncorrectJSDocDoesNotAlterJSParsing9() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
              "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "illegal use of unknown JSDoc tag \"someillegaltag\";"
              + " ignoring it"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnescapedSlashInRegexpCharClass
  public void testUnescapedSlashInRegexpCharClass() throws Exception {
    
    parse("var foo = /[/]/;");
    parse("var foo = /[hi there/]/;");
    parse("var foo = /[/yo dude]/;");
    parse("var foo = /\\/[@#$/watashi/wa/suteevu/desu]/;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testParse
  public void testParse() {
    Node a = Node.newString(Token.NAME, "a");
    a.addChildToFront(Node.newString(Token.NAME, "b"));
    List<ParserResult> testCases = ImmutableList.of(
        new ParserResult(
            "3;",
            createScript(new Node(Token.EXPR_RESULT, Node.newNumber(3.0)))),
        new ParserResult(
            "var a = b;",
             createScript(new Node(Token.VAR, a))),
        new ParserResult(
            "\"hell\\\no\\ world\\\n\\\n!\"",
             createScript(new Node(Token.EXPR_RESULT,
             Node.newString(Token.STRING, "hello world!")))));

    for (ParserResult testCase : testCases) {
      assertNodeEquality(testCase.node, parse(testCase.code));
    }
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning1
  public void testTrailingCommaWarning1() {
    parse("var a = ['foo', 'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning2
  public void testTrailingCommaWarning2() {
    parse("var a = ['foo',,'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning3
  public void testTrailingCommaWarning3() {
    parse("var a = ['foo', 'bar',];", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning4
  public void testTrailingCommaWarning4() {
    parse("var a = [,];", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning5
  public void testTrailingCommaWarning5() {
    parse("var a = {'foo': 'bar'};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning6
  public void testTrailingCommaWarning6() {
    parse("var a = {'foo': 'bar',};", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning7
  public void testTrailingCommaWarning7() {
    parseError("var a = {,};", BAD_PROPERTY_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testConstForbidden
  public void testConstForbidden() {
    parseError("const x = 3;", "Unsupported syntax: CONST");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden
  public void testDestructuringAssignForbidden() {
    parseError("var [x, y] = foo();", "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden2
  public void testDestructuringAssignForbidden2() {
    parseError("var {x, y} = foo();", "missing : after property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden3
  public void testDestructuringAssignForbidden3() {
    parseError("var {x: x, y: y} = foo();",
        "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden4
  public void testDestructuringAssignForbidden4() {
    parseError("[x, y] = foo();", "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLetForbidden
  public void testLetForbidden() {
    parseError("function f() { let (x = 3) { alert(x); }; }",
        "missing ; before statement", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testYieldForbidden
  public void testYieldForbidden() {
    parseError("function f() { yield 3; }", "missing ; before statement");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testBracelessFunctionForbidden
  public void testBracelessFunctionForbidden() {
    parseError("var sq = function(x) x * x;",
        "missing { before function body");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGeneratorsForbidden
  public void testGeneratorsForbidden() {
    parseError("var i = (x for (x in obj));",
        "missing ) in parenthetical");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden1
  public void testGettersForbidden1() {
    parseError("var x = {get foo() { return 3; }};",
        "getters are not supported in Internet Explorer");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden2
  public void testGettersForbidden2() {
    parseError("var x = {get foo bar() { return 3; }};",
        "invalid property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden3
  public void testGettersForbidden3() {
    parseError("var x = {a getter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden4
  public void testGettersForbidden4() {
    parseError("var x = {\"a\" getter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden5
  public void testGettersForbidden5() {
    parseError("var x = {a: 2, get foo() { return 3; }};",
        "getters are not supported in Internet Explorer");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSettersForbidden
  public void testSettersForbidden() {
    parseError("var x = {set foo() { return 3; }};",
        "setters are not supported in Internet Explorer");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSettersForbidden2
  public void testSettersForbidden2() {
    parseError("var x = {a setter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc1
  public void testFileOverviewJSDoc1() {
    Node n = parse(" function Foo() {}");
    assertEquals(Token.FUNCTION, n.getFirstChild().getType());
    assertTrue(n.getJSDocInfo() != null);
    assertNull(n.getFirstChild().getJSDocInfo());
    assertEquals("Hi mom!",
        n.getJSDocInfo().getFileOverview());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDocDoesNotHoseParsing
  public void testFileOverviewJSDocDoesNotHoseParsing() {
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc2
  public void testFileOverviewJSDoc2() {
    Node n = parse(" " +
        " function Foo() {}");
    assertTrue(n.getJSDocInfo() != null);
    assertEquals("Hi mom!", n.getJSDocInfo().getFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo() != null);
    assertFalse(n.getFirstChild().getJSDocInfo().hasFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo().isConstructor());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testObjectLiteralDoc1
  public void testObjectLiteralDoc1() {
    Node n = parse("var x = { 1: 2};");

    Node objectLit = n.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLit.getType());

    Node number = objectLit.getFirstChild();
    assertEquals(Token.NUMBER, number.getType());
    assertNotNull(number.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDuplicatedParam
  public void testDuplicatedParam() {
    parse("function foo(x, x) {}", "Duplicate parameter name \"x\".");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLamestWarningEver
  public void testLamestWarningEver() {
    
    parse("var x =  (y);");
    parse("var x =  (y);");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnfinishedComment
  public void testUnfinishedComment() {
    parseError(" var x;");
    Node var = n.getFirstChild();
    assertNotNull(var.getJSDocInfo());
    assertEquals("This is a variable.",
        var.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.rhino.JSDocInfoTest::testVisibilityOrdinal
  public void testVisibilityOrdinal() {
    assertEquals(0, PRIVATE.ordinal());
    assertEquals(1, PROTECTED.ordinal());
    assertEquals(2, PUBLIC.ordinal());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetType
  public void testSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertTypeEquals(STRING_TYPE, resolve(info.getType()));
    assertNull(info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypeAndVisibility
  public void testSetTypeAndVisibility() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));
    info.setVisibility(PROTECTED);

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertTypeEquals(STRING_TYPE, resolve(info.getType()));
    assertEquals(PROTECTED, info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnType
  public void testSetReturnType() {
    JSDocInfo info = new JSDocInfo();
    info.setReturnType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnTypeAndBaseType
  public void testSetReturnTypeAndBaseType() {
    JSDocInfo info = new JSDocInfo();
    info.setBaseType(
        new JSTypeExpression(
            new Node(Token.BANG, Node.newString("Number")), ""));
    info.setReturnType(fromString("string"));

    assertTypeEquals(NUMBER_OBJECT_TYPE,
        resolve(info.getBaseType()));
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetEnumParameterType
  public void testSetEnumParameterType() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertTypeEquals(STRING_TYPE,
        resolve(info.getEnumParameterType()));
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType
  public void testMultipleSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("number"));

    try {
      info.setReturnType(fromString("boolean"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertTypeEquals(NUMBER_TYPE, resolve(info.getType()));
    assertNull(info.getReturnType());
    assertNull(info.getEnumParameterType());
    assertNull(info.getTypedefType());
    assertTrue(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType2
  public void testMultipleSetType2() {
    JSDocInfo info = new JSDocInfo();

    info.setReturnType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getReturnType()));
    assertNull(info.getEnumParameterType());
    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertFalse(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType3
  public void testMultipleSetType3() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setReturnType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertNull(info.getReturnType());
    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getEnumParameterType()));
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypedefType
  public void testSetTypedefType() {
    JSDocInfo info = new JSDocInfo();
    info.setTypedefType(fromString("boolean"));

    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getTypedefType()));
    assertTrue(info.hasTypedefType());
    assertFalse(info.hasType());
    assertFalse(info.hasEnumParameterType());
    assertFalse(info.hasReturnType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetConstant
  public void testSetConstant() {
    JSDocInfo info = new JSDocInfo();
    info.setConstant(true);

    assertFalse(info.hasType());
    assertTrue(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetConstructor
  public void testSetConstructor() {
    JSDocInfo info = new JSDocInfo();
    info.setConstructor(true);

    assertFalse(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetDefine
  public void testSetDefine() {
    JSDocInfo info = new JSDocInfo();
    info.setDefine(true);

    assertTrue(info.isConstant());
    assertFalse(info.isConstructor());
    assertTrue(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetHidden
  public void testSetHidden() {
    JSDocInfo info = new JSDocInfo();
    info.setHidden(true);

    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertTrue(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetShouldPreserveTry
  public void testSetShouldPreserveTry() {
    JSDocInfo info = new JSDocInfo();
    info.setShouldPreserveTry(true);

    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetNoTypeCheck
  public void testSetNoTypeCheck() {
    JSDocInfo info = new JSDocInfo();
    info.setNoCheck(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isNoAlias());
    assertFalse(info.isOverride());
    assertTrue(info.isNoTypeCheck());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetOverride
  public void testSetOverride() {
    JSDocInfo info = new JSDocInfo();
    info.setOverride(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isNoAlias());
    assertTrue(info.isOverride());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetExport
  public void testSetExport() {
    JSDocInfo info = new JSDocInfo();
    info.setExport(true);

    assertTrue(info.isExport());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetNoAlias
  public void testSetNoAlias() {
    JSDocInfo info = new JSDocInfo();
    info.setNoAlias(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isOverride());
    assertTrue(info.isNoAlias());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetDeprecated
  public void testSetDeprecated() {
    JSDocInfo info = new JSDocInfo();
    info.setDeprecated(true);

    assertFalse(info.isNoAlias());
    assertFalse(info.isOverride());
    assertTrue(info.isDeprecated());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetFlags1
  public void testMultipleSetFlags1() {
    JSDocInfo info = new JSDocInfo();
    info.setConstant(true);
    info.setConstructor(true);
    info.setHidden(true);
    info.setShouldPreserveTry(true);

    assertFalse(info.hasType());
    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertTrue(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setHidden(false);

    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setConstant(false);
    info.setConstructor(false);

    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setConstructor(true);

    assertFalse(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetFileOverviewWithDocumentationOff
  public void testSetFileOverviewWithDocumentationOff() {
    JSDocInfo info = new JSDocInfo();
    info.documentFileOverview("hi bob");
    assertNull(info.getFileOverview());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetFileOverviewWithDocumentationOn
  public void testSetFileOverviewWithDocumentationOn() {
    JSDocInfo info = new JSDocInfo(true);
    info.documentFileOverview("hi bob");
    assertEquals("hi bob", info.getFileOverview());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetSuppressions
  public void testSetSuppressions() {
    JSDocInfo info = new JSDocInfo(true);
    info.setSuppressions(Sets.newHashSet("sam", "bob"));
    assertEquals(Sets.newHashSet("bob", "sam"), info.getSuppressions());
  }

// com.google.javascript.rhino.NodeTest::testMergeExtractNormal
  public void testMergeExtractNormal() throws Exception {
    testMergeExtract(5, 6);
    testMergeExtract(456, 3423);
    testMergeExtract(0, 0);
  }

// com.google.javascript.rhino.NodeTest::testMergeExtractErroneous
  public void testMergeExtractErroneous() throws Exception {
    assertEquals(-1, Node.mergeLineCharNo(-5, 90));
    assertEquals(-1, Node.mergeLineCharNo(0, -1));
    assertEquals(-1, Node.extractLineno(-1));
    assertEquals(-1, Node.extractCharno(-1));
  }

// com.google.javascript.rhino.NodeTest::testMergeOverflowGraciously
  public void testMergeOverflowGraciously() throws Exception {
    int linecharno = Node.mergeLineCharNo(89, 4096);
    assertEquals(89, Node.extractLineno(linecharno));
    assertEquals(4095, Node.extractCharno(linecharno));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplSame
  public void testCheckTreeEqualsImplSame() {
    Node node1 = new Node(1, new Node(2));
    Node node2 = new Node(1, new Node(2));
    assertEquals(null, node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentType
  public void testCheckTreeEqualsImplDifferentType() {
    Node node1 = new Node(1, new Node(2));
    Node node2 = new Node(2, new Node(2));
    assertEquals(new NodeMismatch(node1, node2),
        node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentChildCount
  public void testCheckTreeEqualsImplDifferentChildCount() {
    Node node1 = new Node(1, new Node(2));
    Node node2 = new Node(1);
    assertEquals(new NodeMismatch(node1, node2),
        node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentChild
  public void testCheckTreeEqualsImplDifferentChild() {
    Node child1 = new Node(1);
    Node child2 = new Node(2);
    Node node1 = new Node(1, child1);
    Node node2 = new Node(1, child2);
    assertEquals(new NodeMismatch(child1, child2),
        node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsSame
  public void testCheckTreeEqualsSame() {
    Node node1 = new Node(1);
    assertEquals(null, node1.checkTreeEquals(node1));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsStringDifferent
  public void testCheckTreeEqualsStringDifferent() {
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    assertNotNull(node1.checkTreeEquals(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsBooleanSame
  public void testCheckTreeEqualsBooleanSame() {
    Node node1 = new Node(1);
    assertEquals(true, node1.checkTreeEqualsSilent(node1));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsBooleanDifferent
  public void testCheckTreeEqualsBooleanDifferent() {
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    assertEquals(false, node1.checkTreeEqualsSilent(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentIncProp
  public void testCheckTreeEqualsImplDifferentIncProp() {
    Node node1 = new Node(Token.INC);
    node1.putIntProp(Node.INCRDECR_PROP, 1);
    Node node2 = new Node(Token.INC);
    assertNotNull(node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsSame
  public void testCheckTreeTypeAwareEqualsSame() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    node2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertTrue(node1.checkTreeTypeAwareEqualsSilent(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsSameNull
  public void testCheckTreeTypeAwareEqualsSameNull() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    Node node2 = Node.newString(Token.NAME, "f");
    assertTrue(node1.checkTreeTypeAwareEqualsSilent(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsDifferent
  public void testCheckTreeTypeAwareEqualsDifferent() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    node2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(node1.checkTreeTypeAwareEqualsSilent(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsDifferentNull
  public void testCheckTreeTypeAwareEqualsDifferentNull() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    assertFalse(node1.checkTreeTypeAwareEqualsSilent(node2));
  }

// com.google.javascript.rhino.NodeTest::testVarArgs1
  public void testVarArgs1() {
    assertFalse(new Node(1).isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testVarArgs2
  public void testVarArgs2() {
    Node n = new Node(1);
    n.setVarArgs(false);
    assertFalse(n.isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testVarArgs3
  public void testVarArgs3() {
    Node n = new Node(1);
    n.setVarArgs(true);
    assertTrue(n.isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testFileLevelJSDocAppender
  public void testFileLevelJSDocAppender() {
    Node n = new Node(1);
    Node.FileLevelJsDocBuilder builder = n.getJsDocBuilderForNode();
    builder.append("foo");
    builder.append("bar");
    assertEquals("foobar", n.getJSDocInfo().getLicense());
  }

// com.google.javascript.rhino.NodeTest::testIsQualifiedName
  public void testIsQualifiedName() {
    assertTrue(getNode("a").isQualifiedName());
    assertTrue(getNode("$").isQualifiedName());
    assertTrue(getNode("_").isQualifiedName());
    assertTrue(getNode("a.b").isQualifiedName());
    assertTrue(getNode("a_b.cccccc$d4.x.y.zA$").isQualifiedName());
    assertTrue(getNode("this.foo").isQualifiedName());
    assertFalse(getNode("0").isQualifiedName());
    assertFalse(getNode("[]").isQualifiedName());
    assertFalse(getNode("{}").isQualifiedName());
    assertFalse(getNode("''").isQualifiedName());
    assertFalse(getNode("a[b]").isQualifiedName());
    assertFalse(getNode("a[b].c").isQualifiedName());
    assertFalse(getNode("c.a[b]").isQualifiedName());
    assertFalse(getNode("a()").isQualifiedName());
    assertFalse(getNode("a().b").isQualifiedName());
    assertFalse(getNode("b.a()").isQualifiedName());
    assertFalse(getNode("'a'").isQualifiedName());
    assertFalse(getNode("/x/").isQualifiedName());
    assertFalse(getNode("++x").isQualifiedName());
  }

// com.google.javascript.rhino.NodeTest::testCloneAnnontations
  public void testCloneAnnontations() {
    Node n = getNode("a");
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    Node nodeClone = n.cloneNode();
    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

// com.google.javascript.rhino.NodeTest::testBooleanProp
  public void testBooleanProp() {
    Node n = getNode("a");

    n.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    assertNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    assertNotNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    n.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    assertNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

// com.google.javascript.rhino.NodeTest::testCloneAnnontations2
  public void testCloneAnnontations2() {
    Node n = getNode("a");
    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    n.putBooleanProp(Node.IS_DISPATCHER, true);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(n.getBooleanProp(Node.IS_DISPATCHER));

    Node nodeClone = n.cloneNode();
    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(nodeClone.getBooleanProp(Node.IS_DISPATCHER));

    n.putBooleanProp(Node.IS_DISPATCHER, false);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_DISPATCHER));

    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(nodeClone.getBooleanProp(Node.IS_DISPATCHER));
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoAssign1
  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(0, assign.getLineno());
    assertEquals(2, assign.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(12, assign.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(1, call.getLineno());
    assertEquals(4, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetProp1
  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(1, getprop.getLineno());
    assertEquals(4, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(1, name.getLineno());
    assertEquals(5, name.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetProp2
  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(1, getprop.getLineno());
    assertEquals(4, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(0, name.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetelem1
  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(1, call.getLineno());
    assertEquals(4, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetelem2
  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(6, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetelem3
  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(9, call.getCharno());
  }
