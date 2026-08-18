// buggy function
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

// trigger testcase
// com/google/javascript/jscomp/CodePrinterTest.java::testEmitUnknownParamTypesAsAllType
public void testEmitUnknownParamTypesAsAllType() {
    assertTypeAnnotations(
        "var a = function(x) {}",
        "/**\n" +
        " * @param {*} x\n" +
        " * @return {undefined}\n" +
        " */\n" + 
        "var a = function(x) {\n}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testOptionalTypesAnnotation
public void testOptionalTypesAnnotation() {
    assertTypeAnnotations(
        "/**\n" +
        " * @param {string=} x \n" +
        " */\n" +
        "var a = function(x) {}",
        "/**\n" +
        " * @param {string=} x\n" +
        " * @return {undefined}\n" +
        " */\n" +
        "var a = function(x) {\n}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTempConstructor
public void testTempConstructor() {
    assertTypeAnnotations(
        "var x = function() {\n/**\n * @constructor\n */\nfunction t1() {}\n" +
        " /**\n * @constructor\n */\nfunction t2() {}\n" +
        " t1.prototype = t2.prototype}",
        "/**\n * @return {undefined}\n */\nvar x = function() {\n" +
        "  /**\n * @return {undefined}\n * @constructor\n */\n" +
        "function t1() {\n  }\n" +
        "  /**\n * @return {undefined}\n * @constructor\n */\n" +
        "function t2() {\n  }\n" +
        "  t1.prototype = t2.prototype\n}"
    );
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotations
public void testTypeAnnotations() {
    assertTypeAnnotations(
        "/** @constructor */ function Foo(){}",
        "/**\n * @return {undefined}\n * @constructor\n */\n"
        + "function Foo() {\n}\n");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsAssign
public void testTypeAnnotationsAssign() {
    assertTypeAnnotations("/** @constructor */ var Foo = function(){}",
        "/**\n * @return {undefined}\n * @constructor\n */\n"
        + "var Foo = function() {\n}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsDispatcher1
public void testTypeAnnotationsDispatcher1() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "/** \n" +
        " * @constructor \n" +
        " * @javadispatch \n" +
        " */\n" +
        "a.Foo = function(){}",
        "var a = {};\n" +
        "/**\n" +
        " * @return {undefined}\n" +
        " * @constructor\n" +
        " * @javadispatch\n" +
        " */\n" +
        "a.Foo = function() {\n" +
        "}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsDispatcher2
public void testTypeAnnotationsDispatcher2() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "/** \n" +
        " * @constructor \n" +
        " */\n" +
        "a.Foo = function(){}\n" +
        "/**\n" +
        " * @javadispatch\n" +
        " */\n" +
        "a.Foo.prototype.foo = function() {};",

        "var a = {};\n" +
        "/**\n" +
        " * @return {undefined}\n" +
        " * @constructor\n" +
        " */\n" +
        "a.Foo = function() {\n" +
        "};\n" +
        "/**\n" +
        " * @return {undefined}\n" +
        " * @javadispatch\n" +
        " */\n" +
        "a.Foo.prototype.foo = function() {\n" +
        "}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsImplements
public void testTypeAnnotationsImplements() {
    assertTypeAnnotations("var a = {};"
        + "/** @constructor */ a.Foo = function(){};\n"
        + "/** @interface */ a.I = function(){};\n"
        + "/** @interface */ a.I2 = function(){};\n"
        + "/** @constructor \n @extends {a.Foo}\n"
        + " * @implements {a.I} \n @implements {a.I2}\n"
        + "*/ a.Bar = function(){}",
        "var a = {};\n"
        + "/**\n * @return {undefined}\n * @constructor\n */\n"
        + "a.Foo = function() {\n};\n"
        + "/**\n * @interface\n */\n"
        + "a.I = function() {\n};\n"
        + "/**\n * @interface\n */\n"
        + "a.I2 = function() {\n};\n"
        + "/**\n * @return {undefined}\n * @extends {a.Foo}\n"
        + " * @implements {a.I}\n"
        + " * @implements {a.I2}\n * @constructor\n */\n"
        + "a.Bar = function() {\n}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsMember
public void testTypeAnnotationsMember() {
    assertTypeAnnotations("var a = {};"
        + "/** @constructor */ a.Foo = function(){}"
        + "/** @param {string} foo\n"
        + "  * @return {number} */\n"
        + "a.Foo.prototype.foo = function(foo) { return 3; };"
        + "/** @type {string|undefined} */"
        + "a.Foo.prototype.bar = '';",
        "var a = {};\n"
        + "/**\n * @return {undefined}\n * @constructor\n */\n"
        + "a.Foo = function() {\n};\n"
        + "/**\n"
        + " * @param {string} foo\n"
        + " * @return {number}\n"
        + " */\n"
        + "a.Foo.prototype.foo = function(foo) {\n  return 3\n};\n"
        + "/** @type {string} */\n"
        + "a.Foo.prototype.bar = \"\"");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsMemberSubclass
public void testTypeAnnotationsMemberSubclass() {
    assertTypeAnnotations("var a = {};"
        + "/** @constructor */ a.Foo = function(){};"
        + "/** @constructor \n @extends {a.Foo} */ a.Bar = function(){}",
        "var a = {};\n"
        + "/**\n * @return {undefined}\n * @constructor\n */\n"
        + "a.Foo = function() {\n};\n"
        + "/**\n * @return {undefined}\n * @extends {a.Foo}\n"
        + " * @constructor\n */\n"
        + "a.Bar = function() {\n}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testTypeAnnotationsNamespace
public void testTypeAnnotationsNamespace() {
    assertTypeAnnotations("var a = {};"
        + "/** @constructor */ a.Foo = function(){}",
        "var a = {};\n"
        + "/**\n * @return {undefined}\n * @constructor\n */\n"
        + "a.Foo = function() {\n}");
  }

// com/google/javascript/jscomp/CodePrinterTest.java::testVariableArgumentsTypesAnnotation
public void testVariableArgumentsTypesAnnotation() {
    assertTypeAnnotations(
        "/**\n" +
        " * @param {...string} x \n" +
        " */\n" +
        "var a = function(x) {}",
        "/**\n" +
        " * @param {...string} x\n" +
        " * @return {undefined}\n" +
        " */\n" +
        "var a = function(x) {\n}");
  }

// com/google/javascript/jscomp/DevirtualizePrototypeMethodsTest.java::testRewritePrototypeMethods2
public void testRewritePrototypeMethods2() throws Exception {
    // type checking on
    enableTypeCheck(CheckLevel.ERROR);
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_ON);
  }

// com/google/javascript/jscomp/DisambiguatePropertiesTest.java::testStaticProperty
public void testStaticProperty() {
    String js = ""
      + "/** @constructor */ function Foo() {} \n"
      + "/** @constructor */ function Bar() {}\n"
      + "Foo.a = 0;"
      + "Bar.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "function Bar(){}"
        + "Foo.function__this_Foo___undefined$a = 0;"
        + "Bar.function__this_Bar___undefined$a = 0;";

    testSets(false, js, output,
        "{a=[[function (this:Bar): undefined]," +
        " [function (this:Foo): undefined]]}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportDontEmitPrototypePathPrefix
public void testExportDontEmitPrototypePathPrefix() { 
    compileAndCheck(
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "var Foo = function() {};" +
        "/**\n" +
        " * @return {number}\n" +
        " */\n" +
        "Foo.prototype.m = function() {return 6;};\n" +
        "goog.exportSymbol('Foo', Foo);\n" +
        "goog.exportProperty(Foo.prototype, 'm', Foo.prototype.m);",
        "/**\n" +
        " * @return {undefined}\n" +
        " * @constructor\n" +
        " */\n" +
        "var Foo = function() {\n};\n" +
        "/**\n" +
        " * @return {number}\n" +
        " */\n" +
        "Foo.prototype.m = function() {\n}"
    );  
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportMultiple
public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a = {};\n" +
                    "/**\n" +
                    " * @param {*} p1\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "a.b = function(p1) {\n};\n" +
                    "/**\n" +
                    " * @param {*} d\n" +
                    " * @param {*} e\n" +
                    " * @param {*} f\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "/**\n" +
                    " * @param {*} g\n" +
                    " * @param {*} h\n" +
                    " * @param {*} i\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "a.b.prototype.c = function(g, h, i) {\n}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportMultiple2
public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var hello = {};\n" +
                    "hello.b = {};\n" +
                    "/**\n" +
                    " * @param {*} d\n" +
                    " * @param {*} e\n" +
                    " * @param {*} f\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "/**\n" +
                    " * @param {*} g\n" +
                    " * @param {*} h\n" +
                    " * @param {*} i\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportMultiple3
public void testExportMultiple3() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('prefix', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);",

                    "/**\n" +
                    " * @param {*} p1\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "var prefix = function(p1) {\n};\n" +
                    "/**\n" +
                    " * @param {*} d\n" +
                    " * @param {*} e\n" +
                    " * @param {*} f\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "prefix.c = function(d, e, f) {\n}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportProperty
public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a = {};\n" +
                    "a.b = {};\n" +
                    "/**\n" +
                    " * @param {*} d\n" +
                    " * @param {*} e\n" +
                    " * @param {*} f\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "a.b.cprop = function(d, e, f) {\n}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportSymbol
public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "/**\n" +
                    " * @param {*} d\n" +
                    " * @param {*} e\n" +
                    " * @param {*} f\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "var foobar = function(d, e, f) {\n}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportSymbolDefinedInVar
public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "/**\n" +
                    " * @param {*} d\n" +
                    " * @param {*} e\n" +
                    " * @param {*} f\n" +
                    " * @return {undefined}\n" +
                    " */\n" +
                    "var foobar = function(d, e, f) {\n}");
  }

// com/google/javascript/jscomp/ExternExportsPassTest.java::testExportSymbolWithConstructor
public void testExportSymbolWithConstructor() {
    compileAndCheck("var internalName;\n" +
                    "/**\n" +
                    " * @constructor\n" +
                    " */\n" +  
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "/**\n" +
                    " * @return {undefined}\n" +
                    " * @constructor\n" +
                    " */\n" + 
                    "var externalName = function() {\n}");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testBadConstructorCall
public void testBadConstructorCall() throws Exception {
    testTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo();",
        "Constructor function (this:Foo): undefined should be called " +
        "with the \"new\" keyword");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testBug911118
public void testBug911118() throws Exception {
    // verifying the type assigned to anonymous functions assigned variables
    Scope s = parseAndTypeCheckWithScope("var a = function(){};").scope;
    JSType type = s.getVar("a").getType();
    assertEquals("function (): undefined", type.toString());

    // verifying the bug example
    testTypes("function nullFunction() {};" +
        "var foo = nullFunction;" +
        "foo = function() {};" +
        "foo();");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testDontAddMethodsIfNoConstructor
public void testDontAddMethodsIfNoConstructor()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    JSType functionAType = js1Node.getFirstChild().getJSType();
    assertEquals("function (): undefined", functionAType.toString());
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m1"));
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m2"));
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testDuplicateOldTypeDef
public void testDuplicateOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        "/** @constructor */ goog.Bar = function() {};" +
        "/** @type {number} */ goog.Bar = goog.typedef",
        "variable goog.Bar redefined with type number, " +
        "original definition at [testcode]:1 " +
        "with type function (this:goog.Bar): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testDuplicateStaticMethodDecl1
public void testDuplicateStaticMethodDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "/** @param {number} x */ goog.foo = function(x) {};" +
        "/** @param {number} x */ goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (number): undefined, " +
        "original definition at [testcode]:1 " +
        "with type function (number): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testDuplicateStaticMethodDecl5
public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "/** @return {undefined} */ goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testDuplicateTypeDef
public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        "/** @constructor */ goog.Bar = function() {};" +
        "/** @typedef {number} */ goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (this:goog.Bar): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testErrorMismatchingPropertyOnInterface5
public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes("/** @interface */ function T() {};\n" +
        "/** @type {number} */T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference1
public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference12
public void testFunctionInference12() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference13
public void testFunctionInference13() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "/** @constructor */ goog.Foo = function(){};" +
        "/** @param {!goog.Foo} f */function eatFoo(f){};",
        "eatFoo",
        "function (goog.Foo): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference15
public void testFunctionInference15() throws Exception {
    testFunctionType(
        "/** @constructor */ function f() {};" +
        "f.prototype.foo = function(){};",
        "f.prototype.foo",
        "function (this:f): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference16
public void testFunctionInference16() throws Exception {
    testFunctionType(
        "/** @constructor */ function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference2
public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference3
public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference4
public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference7
public void testFunctionInference7() throws Exception {
    testFunctionType(
        "/** @this Date */function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference8
public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testFunctionInference9
public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testGoodExtends7
public void testGoodExtends7() throws Exception {
    testFunctionType(
        "Function.prototype.inherits = function(x) {};" +
        "/** @constructor */function base() {}\n" +
        "/** @extends {base}\n * @constructor */function derived() {}\n" +
        "derived.inherits(base);",
        "(new derived).constructor",
        "function (this:derived): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testInterfaceInheritanceCheck11
public void testInterfaceInheritanceCheck11() throws Exception {
    testTypes(
        "/** @constructor */function Super() {};" +
        "/** @param {number} bar */Super.prototype.foo = function(bar) {};" +
        "/** @constructor\n @extends {Super} */function Sub() {};" +
        "/** @override\n  @param {string} bar */Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testInterfaceInheritanceCheck7
public void testInterfaceInheritanceCheck7() throws Exception {
    testTypes(
        "/** @interface */function Super() {};" +
        "/** @param {number} bar */Super.prototype.foo = function(bar) {};" +
        "/** @constructor\n @implements {Super} */function Sub() {};" +
        "/** @override\n  @param {string} bar */Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testNestedFunctionInference1
public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        "/** @constructor */ function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testPrototypePropertyReference
public void testPrototypePropertyReference() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(""
        + "/** @constructor */\n"
        + "function Foo() {}\n"
        + "/** @param {number} a */\n"
        + "Foo.prototype.bar = function(a){};\n"
        + "/** @param {Foo} f */\n"
        + "function baz(f) {\n"
        + "  Foo.prototype.bar.call(f, 3);\n"
        + "}");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());

    assertTrue(p.scope.getVar("Foo").getType() instanceof FunctionType);
    FunctionType fooType = (FunctionType) p.scope.getVar("Foo").getType();
    assertEquals("function (this:Foo, number): undefined",
                 fooType.getPrototype().getPropertyType("bar").toString());
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testScoping10
public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    // a declared, b is not
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    // checking that a has the correct assigned type
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com/google/javascript/jscomp/LooseTypeCheckTest.java::testTypeRedefinition
public void testTypeRedefinition() throws Exception {
    testTypes("a={};/**@enum {string}*/ a.A = {ZOR:'b'};"
        + "/** @constructor */ a.A = function() {}",
        "variable a.A redefined with type function (this:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testBadConstructorCall
public void testBadConstructorCall() throws Exception {
    testTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo();",
        "Constructor function (this:Foo): undefined should be called " +
        "with the \"new\" keyword");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testBug911118
public void testBug911118() throws Exception {
    // verifying the type assigned to function expressions assigned variables
    Scope s = parseAndTypeCheckWithScope("var a = function(){};").scope;
    JSType type = s.getVar("a").getType();
    assertEquals("function (): undefined", type.toString());

    // verifying the bug example
    testTypes("function nullFunction() {};" +
        "var foo = nullFunction;" +
        "foo = function() {};" +
        "foo();");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testDontAddMethodsIfNoConstructor
public void testDontAddMethodsIfNoConstructor()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    JSType functionAType = js1Node.getFirstChild().getJSType();
    assertEquals("function (): undefined", functionAType.toString());
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m1"));
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m2"));
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testDuplicateOldTypeDef
public void testDuplicateOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        "/** @constructor */ goog.Bar = function() {};" +
        "/** @type {number} */ goog.Bar = goog.typedef",
        "variable goog.Bar redefined with type number, " +
        "original definition at [testcode]:1 " +
        "with type function (this:goog.Bar): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testDuplicateStaticMethodDecl1
public void testDuplicateStaticMethodDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "/** @param {number} x */ goog.foo = function(x) {};" +
        "/** @param {number} x */ goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (number): undefined, " +
        "original definition at [testcode]:1 with type function (number): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testDuplicateStaticMethodDecl5
public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "/** @return {undefined} */ goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testDuplicateTypeDef
public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        "/** @constructor */ goog.Bar = function() {};" +
        "/** @typedef {number} */ goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (this:goog.Bar): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testErrorMismatchingPropertyOnInterface5
public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes("/** @interface */ function T() {};\n" +
        "/** @type {number} */T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference1
public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference12
public void testFunctionInference12() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference13
public void testFunctionInference13() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "/** @constructor */ goog.Foo = function(){};" +
        "/** @param {!goog.Foo} f */function eatFoo(f){};",
        "eatFoo",
        "function (goog.Foo): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference15
public void testFunctionInference15() throws Exception {
    testFunctionType(
        "/** @constructor */ function f() {};" +
        "f.prototype.foo = function(){};",
        "f.prototype.foo",
        "function (this:f): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference16
public void testFunctionInference16() throws Exception {
    testFunctionType(
        "/** @constructor */ function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference2
public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference3
public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference4
public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference7
public void testFunctionInference7() throws Exception {
    testFunctionType(
        "/** @this Date */function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference8
public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionInference9
public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testGoodExtends7
public void testGoodExtends7() throws Exception {
    testFunctionType(
        "Function.prototype.inherits = function(x) {};" +
        "/** @constructor */function base() {}\n" +
        "/** @extends {base}\n * @constructor */function derived() {}\n" +
        "derived.inherits(base);",
        "(new derived).constructor",
        "function (this:derived): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInferredReturn1
public void testInferredReturn1() throws Exception {
    testTypes(
        "function f() {} /** @param {number} x */ function g(x) {}" +
        "g(f());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInferredReturn2
public void testInferredReturn2() throws Exception {
    testTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype.bar = function() {}; " +
        "/** @param {number} x */ function g(x) {}" +
        "g((new Foo()).bar());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInferredReturn3
public void testInferredReturn3() throws Exception {
    testTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype.bar = function() {}; " +
        "/** @constructor \n * @extends {Foo} */ function SubFoo() {}" +
        "/** @return {number} \n * @override  */ " +
        "SubFoo.prototype.bar = function() { return 3; }; ",
        "mismatch of the bar property type and the type of the property " +
        "it overrides from superclass Foo\n" +
        "original: function (this:Foo): undefined\n" +
        "override: function (this:SubFoo): number");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInferredReturn4
public void testInferredReturn4() throws Exception {
    // By design, this throws a warning. if you want global x to be
    // defined to some other type of function, then you need to declare it
    // as a greater type.
    testTypes(
        "var x = function() {};" +
        "x = /** @type {function(): number} */ (function() { return 3; });",
        "assignment\n" +
        "found   : function (): number\n" +
        "required: function (): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInferredReturn6
public void testInferredReturn6() throws Exception {
    testTypes(
        "/** @return {string} */" +
        "function f() {" +
        "  var x = function() {};" +
        "  if (f()) " +
        "    x = /** @type {function(): number} */ " +
        "        (function() { return 3; });" +
        "  return x();" +
        "}",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInterfaceInheritanceCheck11
public void testInterfaceInheritanceCheck11() throws Exception {
    testTypes(
        "/** @constructor */function Super() {};" +
        "/** @param {number} bar */Super.prototype.foo = function(bar) {};" +
        "/** @constructor\n @extends {Super} */function Sub() {};" +
        "/** @override\n  @param {string} bar */Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testInterfaceInheritanceCheck7
public void testInterfaceInheritanceCheck7() throws Exception {
    testTypes(
        "/** @interface */function Super() {};" +
        "/** @param {number} bar */Super.prototype.foo = function(bar) {};" +
        "/** @constructor\n @implements {Super} */function Sub() {};" +
        "/** @override\n  @param {string} bar */Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testNestedFunctionInference1
public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        "/** @constructor */ function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testPrototypePropertyReference
public void testPrototypePropertyReference() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(""
        + "/** @constructor */\n"
        + "function Foo() {}\n"
        + "/** @param {number} a */\n"
        + "Foo.prototype.bar = function(a){};\n"
        + "/** @param {Foo} f */\n"
        + "function baz(f) {\n"
        + "  Foo.prototype.bar.call(f, 3);\n"
        + "}");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());

    assertTrue(p.scope.getVar("Foo").getType() instanceof FunctionType);
    FunctionType fooType = (FunctionType) p.scope.getVar("Foo").getType();
    assertEquals("function (this:Foo, number): undefined",
                 fooType.getPrototype().getPropertyType("bar").toString());
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testScoping10
public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    // a declared, b is not
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    // checking that a has the correct assigned type
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com/google/javascript/jscomp/TypeCheckTest.java::testTypeRedefinition
public void testTypeRedefinition() throws Exception {
    testTypes("a={};/**@enum {string}*/ a.A = {ZOR:'b'};"
        + "/** @constructor */ a.A = function() {}",
        "variable a.A redefined with type function (this:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }

// com/google/javascript/jscomp/TypedScopeCreatorTest.java::testConstructorNode
public void testConstructorNode() {
    testSame("var goog = {}; /** @constructor */ goog.Foo = function() {};");

    ObjectType ctor = (ObjectType) (findNameType("goog.Foo", globalScope));
    assertNotNull(ctor);
    assertTrue(ctor.isConstructor());
    assertEquals("function (this:goog.Foo): undefined", ctor.toString());
  }

// com/google/javascript/jscomp/TypedScopeCreatorTest.java::testConstructorProperty
public void testConstructorProperty() {
    testSame("var foo = {}; /** @constructor */ foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (this:foo.Bar): undefined", fooBar.toString());
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("Bar"));
  }

// com/google/javascript/jscomp/TypedScopeCreatorTest.java::testMethodBeforeFunction
public void testMethodBeforeFunction() throws Exception {
    testSame(
        "var y = Window.prototype;" +
        "Window.prototype.alert = function(message) {};" +
        "/** @constructor */ function Window() {}\n" +
        "var window = new Window(); \n" +
        "var x = window;");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Window", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("alert"));
    assertEquals("function (this:Window, ?): undefined",
        x.getPropertyType("alert").toString());
    assertTrue(x.isPropertyTypeDeclared("alert"));

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertEquals("function (this:Window, ?): undefined",
        y.getPropertyType("alert").toString());
  }

// com/google/javascript/jscomp/TypedScopeCreatorTest.java::testPropertiesOnInterface
public void testPropertiesOnInterface() throws Exception {
    testSame("/** @interface */ var I = function() {};" +
        "/** @type {number} */ I.prototype.bar;" +
        "I.prototype.baz = function(){};");

    Var i = globalScope.getVar("I");
    assertEquals("function (this:I): ?", i.getType().toString());
    assertTrue(i.getType().isInterface());

    ObjectType iPrototype = (ObjectType)
        ((ObjectType) i.getType()).getPropertyType("prototype");
    assertEquals("I.prototype", iPrototype.toString());
    assertTrue(iPrototype.isFunctionPrototypeType());

    assertEquals("number", iPrototype.getPropertyType("bar").toString());
    assertEquals("function (this:I): undefined",
        iPrototype.getPropertyType("baz").toString());

    assertEquals(iPrototype, globalScope.getVar("I.prototype").getType());
  }

// com/google/javascript/jscomp/TypedScopeCreatorTest.java::testReturnTypeInference1
public void testReturnTypeInference1() {
    testSame("function f() {}");
    assertEquals(
        "function (): undefined",
        findNameType("f", globalScope).toString());
  }
