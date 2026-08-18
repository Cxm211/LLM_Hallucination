// buggy code
    private void annotateCalls(Node n) {
      Preconditions.checkState(n.isCall());

      // Keep track of of the "this" context of a call.  A call without an
      // explicit "this" is a free call.
      Node first = n.getFirstChild();

      // ignore cast nodes.

      if (!NodeUtil.isGet(first)) {
        n.putBooleanProp(Node.FREE_CALL, true);
      }

      // Keep track of the context in which eval is called. It is important
      // to distinguish between "(0, eval)()" and "eval()".
      if (first.isName() &&
          "eval".equals(first.getString())) {
        first.putBooleanProp(Node.DIRECT_EVAL, true);
      }
    }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testLends7
  public void testLends7() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends8
  public void testLends8() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " var Foo = 3;" +
        "extend(Foo,  ({bar: 1}));",
        "May only lend properties to object types. Foo has type number.");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends9
  public void testLends9() throws Exception {
    testClosureTypesMultipleWarnings(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        Lists.newArrayList(
            "Bad type annotation. expected closing }",
            "Bad type annotation. missing object name in @lends tag"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends10
  public void testLends10() throws Exception {
    testTypes(
        "function defineClass(x) { return function() {}; } " +
        "" +
        "var Foo = defineClass(" +
        "     ({ bar: 1}));" +
        " function f() { return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends11
  public void testLends11() throws Exception {
    testTypes(
        "function defineClass(x, y) { return function() {}; } " +
        "" +
        "var Foo = function() {};" +
        " Foo.prototype.bar = function() { return 3; };" +
        "\n" +
        "var SubFoo = defineClass(Foo, " +
        "     ({\n" +
        "       bar: function() { return 3; }}));" +
        " function f() { return (new SubFoo()).bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertTypeEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertTypeEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug1
  public void testFlowScopeBug1() throws Exception {
    Node n = parseAndTypeCheck("\n"
        + "function f(a, b) {\n"
        + ""
        + "var i = 0;"
        + "for (; (i + a) < b; ++i) {}}");

    
    assertTypeEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        n.getFirstChild().getLastChild().getLastChild().getFirstChild()
        .getNext().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug2
  public void testFlowScopeBug2() throws Exception {
    Node n = parseAndTypeCheck(" function Foo() {};\n"
        + "Foo.prototype.hi = false;"
        + "function foo(a, b) {\n"
        + "  "
        + "  var arr;"
        + "  "
        + "  var iter;"
        + "  for (iter = 0; iter < arr.length; ++ iter) {"
        + "    "
        + "    var afoo = arr[iter];"
        + "    afoo;"
        + "  }"
        + "}");

    
    assertTypeEquals(registry.createNullableType(registry.getType("Foo")),
        n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddSingletonGetter
  public void testAddSingletonGetter() {
    Node n = parseAndTypeCheck(
        " function Foo() {};\n" +
        "goog.addSingletonGetter(Foo);");
    ObjectType o = (ObjectType) n.getFirstChild().getJSType();
    assertEquals("function (): Foo",
        o.getPropertyType("getInstance").toString());
    assertEquals("Foo", o.getPropertyType("instance_").toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckStandaloneAST
  public void testTypeCheckStandaloneAST() throws Exception {
    Node n = compiler.parseTestCode("function Foo() { }");
    typeCheck(n);
    MemoizedScopeCreator scopeCreator = new MemoizedScopeCreator(
        new TypedScopeCreator(compiler));
    Scope topScope = scopeCreator.createScope(n, null);

    Node second = compiler.parseTestCode("new Foo");

    Node externs = new Node(Token.BLOCK);
    Node externAndJsRoot = new Node(Token.BLOCK, externs, second);
    externAndJsRoot.setIsSyntheticBlock(true);

    new TypeCheck(
        compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), registry),
        registry, topScope, scopeCreator, CheckLevel.WARNING, CheckLevel.OFF)
        .process(null, second);

    assertEquals(1, compiler.getWarningCount());
    assertEquals("cannot instantiate non-constructor",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.TypeCheckTest::testUpdateParameterTypeOnClosure
  public void testUpdateParameterTypeOnClosure() throws Exception {
    testTypes(
        "\n" +
        "function Object(opt_value) {}\n" +
        "\n" +
        "function Function(var_args) {}\n" +
        "\n" +
        
        
        "Object.prototype.constructor = function() {};\n",
        "\n" +
        "function f(fn) {}\n" +
        "f(function(g) { });\n",
        null,
        false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatedThisType1
  public void testTemplatedThisType1() throws Exception {
    testTypes(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.method = function() {};\n" +
        "\n" +
        "function Bar() {}\n" +
        "var g = new Bar().method();\n" +
        "\n" +
        "function compute(a) {};\n" +
        "compute(g);\n",

        "actual parameter 1 of compute does not match formal parameter\n" +
        "found   : Bar\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatedThisType2
  public void testTemplatedThisType2() throws Exception {
    testTypes(
        "\n" +
        "Array.prototype.method = function() {};\n" +
        "(function(){\n" +
        "  Array.prototype.method.call(arguments);" +
        "})();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType1
  public void testTemplateType1() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y, z) {}\n" +
        "f(this, this, function() { this });");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType2
  public void testTemplateType2() throws Exception {
    
    
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType3
  public void testTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function call(v, f) { f.call(null, v); }" +
        " var s;" +
        "call(3, function(x) {" +
        " x = true;" +
        " s = x;" +
        "});",
        "assignment\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType4
  public void testTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function fn(p) { return p; }\n" +
        " var x;" +
        "x = fn(3, null);",
        "assignment\n" +
        "found   : (null|number)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType5
  public void testTemplateType5() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(
        "var CGI_PARAM_RETRY_COUNT = 'rc';" +
        "" +
        "\n" +
        "function fn(p) { return p; }\n" +
        " var x;" +
        "" +
        "\n" +
        "function aScope() {\n" +
        "  x = fn(CGI_PARAM_RETRY_COUNT, 1);\n" +
        "}",
        "assignment\n" +
        "found   : (number|string)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType6
  public void testTemplateType6() throws Exception {
    testTypes(
        "\n" +
        "function fn(arr, f) { return arr[0]; }\n" +
        " function g(arr) {" +
        "   var x = fn.call(null, arr, null);" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType7
  public void testTemplateType7() throws Exception {
    
    
    
    testTypes(
        "\n" +
        "var query = [];\n" +
        "query.push(1);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType8
  public void testTemplateType8() throws Exception {
    testTypes(
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "function fn(bar) {}\n" +
        " function g(bar) {" +
        "   var x = fn(bar);" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType9
  public void testTemplateType9() throws Exception {
    
    testTypes(
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "function fn(bar) {}\n" +
        " function g(bar) {" +
        "   var x = fn(bar);" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument
  public void testFunctionLiteralDefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; }, {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument2
  public void testFunctionLiteralDefinedThisArgument2() throws Exception {
    testTypes(""
        + " function f(x) {}"
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "function g() { baz(function() { f(this.length); }, []); }",
        "actual parameter 1 of f does not match formal parameter\n"
        + "found   : number\n"
        + "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUnreadNullThisArgument
  public void testFunctionLiteralUnreadNullThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() {}, null);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnionTemplateThisType
  public void testUnionTemplateThisType() throws Exception {
    testTypes(
        " function F() {}" +
        " function g() { return []; }" +
        " function h(x) { }" +
        "\n" +
        "function f(x, y) {}\n" +
        "f(g(), function() { h(this); });",
        "actual parameter 1 of h does not match formal parameter\n" +
        "found   : (Array|F|null)\n" +
        "required: (F|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testActiveXObject
  public void testActiveXObject() throws Exception {
    testTypes(
        " var x = new ActiveXObject();" +
        " var y = new ActiveXObject();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType1
  public void testRecordType1() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f({});",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : {prop: (number|undefined)}\n" +
        "required: {prop: number}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType2
  public void testRecordType2() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f({});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType3
  public void testRecordType3() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f({prop: 'x'});",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : {prop: (number|string)}\n" +
        "required: {prop: number}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType4
  public void testRecordType4() throws Exception {
    
    
    
    testClosureTypesMultipleWarnings(
        "" +
        "function f(x) {}" +
        "" +
        "function g(x) {}" +
        "var x = {}; f(x); g(x);",
        Lists.newArrayList(
            "actual parameter 1 of f does not match formal parameter\n" +
            "found   : {prop: (number|string|undefined)}\n" +
            "required: {prop: (number|undefined)}",
            "actual parameter 1 of g does not match formal parameter\n" +
            "found   : {prop: (number|string|undefined)}\n" +
            "required: {prop: (string|undefined)}"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType5
  public void testRecordType5() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "" +
        "function g(x) {}" +
        "var x = {}; f(x); g(x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType6
  public void testRecordType6() throws Exception {
    testTypes(
        "" +
        "function f() { return {}; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType7
  public void testRecordType7() throws Exception {
    testTypes(
        "" +
        "function f() { var x = {}; g(x); return x; }" +
        "" +
        "function g(x) {}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : {prop: (number|undefined)}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType8
  public void testRecordType8() throws Exception {
    testTypes(
        "" +
        "function f() { var x = {prop: 3}; g(x.prop); return x; }" +
        "" +
        "function g(x) {}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateRecordFields1
  public void testDuplicateRecordFields1() throws Exception {
    testTypes(""
         + "function f(a) {};",
         "Parse error. Duplicate record field x");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateRecordFields2
  public void testDuplicateRecordFields2() throws Exception {
    testTypes(""
         + "function f(a) {};",
         new String[] {"Bad type annotation. Unknown type x",
           "Parse error. Duplicate record field number",
           "Bad type annotation. Unknown type y"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface1
  public void testMultipleExtendsInterface1() throws Exception {
    testTypes(" function base1() {}\n"
        + " function base2() {}\n"
        + "\n"
        + "function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface2
  public void testMultipleExtendsInterface2() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "Int0.prototype.foo = function() {};" +
        "" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int0 is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface3
  public void testMultipleExtendsInterface3() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "Int1.prototype.foo = function() {};" +
        "" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int1 is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface4
  public void testMultipleExtendsInterface4() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "function Int2() {};" +
        "function Foo() {};",
        "Int2 @extends non-object type number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface5
  public void testMultipleExtendsInterface5() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "" +
        "function Int2() {};",
        "Int2 cannot extend this type; interfaces can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface6
  public void testMultipleExtendsInterface6() throws Exception {
    testTypes(
        "function Super1() {};" +
        "function Super2() {};" +
        "Super2.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super2\n" +
        "original: function (this:Super2, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterfaceAssignment
  public void testMultipleExtendsInterfaceAssignment() throws Exception {
    testTypes("var I1 = function() {};\n" +
        " var I2 = function() {}\n" +
        "" +
        "var I3 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
         "var i1 = t;\n" +
         "var i2 = t;\n" +
         "var i3 = t;\n" +
         "i1 = i3;\n" +
         "i2 = i3;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterfaceParamPass
  public void testMultipleExtendsInterfaceParamPass() throws Exception {
    testTypes("var I1 = function() {};\n" +
        " var I2 = function() {}\n" +
        "" +
        "var I3 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "function foo(x,y,z){};\n" +
        "foo(t,t,t)\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadMultipleExtendsClass
  public void testBadMultipleExtendsClass() throws Exception {
    testTypes(" function base1() {}\n"
        + " function base2() {}\n"
        + "\n"
        + "function derived() {}",
        "Bad type annotation. type annotation incompatible "
        + "with other annotations");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtendsResolution
  public void testInterfaceExtendsResolution() throws Exception {
    testTypes(" function B() {};\n" +
        " function C() {};\n" +
        " function A() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyCanBeDefinedInObject
  public void testPropertyCanBeDefinedInObject() throws Exception {
    testTypes(" function I() {};" +
        "I.prototype.bar = function() {};" +
        " var foo;" +
        "foo.bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility1
  public void testExtendedInterfacePropertiesCompatibility1() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        "" +
        "function Int2() {};",
        "Interface Int2 has a property foo with incompatible types in its " +
        "super interfaces Int0 and Int1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility2
  public void testExtendedInterfacePropertiesCompatibility2() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "function Int2() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        "" +
        "Int2.prototype.foo;" +
        "" +
        "function Int3() {};",
        new String[] {
            "Interface Int3 has a property foo with incompatible types in " +
            "its super interfaces Int0 and Int1",
            "Interface Int3 has a property foo with incompatible types in " +
            "its super interfaces Int1 and Int2"
        });
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility3
  public void testExtendedInterfacePropertiesCompatibility3() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};",
        "Interface Int3 has a property foo with incompatible types in its " +
        "super interfaces Int0 and Int1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility4
  public void testExtendedInterfacePropertiesCompatibility4() throws Exception {
    testTypes(
        "function Int0() {};" +
        " function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "function Int2() {};" +
        " function Int3() {};" +
        "" +
        "Int2.prototype.foo;" +
        "" +
        "function Int4() {};",
        "Interface Int4 has a property foo with incompatible types in its " +
        "super interfaces Int0 and Int2");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility5
  public void testExtendedInterfacePropertiesCompatibility5() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "function Int5() {};",
        new String[] {
            "Interface Int3 has a property foo with incompatible types in its" +
            " super interfaces Int0 and Int1",
            "Interface Int5 has a property foo with incompatible types in its" +
            " super interfaces Int1 and Int4"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility6
  public void testExtendedInterfacePropertiesCompatibility6() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "function Int5() {};",
        "Interface Int3 has a property foo with incompatible types in its" +
        " super interfaces Int0 and Int1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility7
  public void testExtendedInterfacePropertiesCompatibility7() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "function Int5() {};",
        new String[] {
            "Interface Int3 has a property foo with incompatible types in its" +
            " super interfaces Int0 and Int1",
            "Interface Int5 has a property foo with incompatible types in its" +
            " super interfaces Int1 and Int4"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility8
  public void testExtendedInterfacePropertiesCompatibility8() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.bar;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "Int4.prototype.bar;" +
        "" +
        "function Int5() {};",
        new String[] {
            "Interface Int5 has a property bar with incompatible types in its" +
            " super interfaces Int1 and Int4",
            "Interface Int5 has a property foo with incompatible types in its" +
            " super interfaces Int0 and Int4"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testGenerics1
  public void testGenerics1() throws Exception {
    String fnDecl = " \n" +
        "function f(x,y) { return y(x); }\n";

    testTypes(
        fnDecl +
        "" +
        "var out;" +
        "" +
        "var result = f('hi', function(x){ out = x; return x; });");

    testTypes(
        fnDecl +
        "" +
        "var out;" +
        "var result = f(0, function(x){ out = x; return x; });",
        "assignment\n" +
        "found   : number\n" +
        "required: string");

    testTypes(
        fnDecl +
        "var out;" +
        "" +
        "var result = f(0, function(x){ out = x; return x; });",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter0
  public void testFilter0()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter1
  public void testFilter1()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter2
  public void testFilter2()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);",
        "initializing variable\n" +
        "found   : Array.<string>\n" +
        "required: Array.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter3
  public void testFilter3()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);",
        "initializing variable\n" +
        "found   : (Array.<string>|null)\n" +
        "required: (Array.<number>|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter1
  public void testBackwardsInferenceGoogArrayFilter1()
      throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var arr;\n" +
        "" +
        "var result = goog.array.filter(" +
        "   arr," +
        "   function(item,index,src) {return false;});",
        "initializing variable\n" +
        "found   : Array.<string>\n" +
        "required: Array.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter2
  public void testBackwardsInferenceGoogArrayFilter2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var out;" +
        "" +
        "var arr;\n" +
        "var out4 = goog.array.filter(" +
        "   arr," +
        "   function(item,index,src) {out = item;});",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter3
  public void testBackwardsInferenceGoogArrayFilter3() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var out;" +
        " var arr;\n" +
        "var result = goog.array.filter(" +
        "   arr," +
        "   function(item,index,src) {out = index;});",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter4
  public void testBackwardsInferenceGoogArrayFilter4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var out;" +
        " var arr;\n" +
        "var out4 = goog.array.filter(" +
        "   arr," +
        "   function(item,index,srcArr) {out = srcArr;});",
        "assignment\n" +
        "found   : (null|{length: number})\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCatchExpression1
  public void testCatchExpression1() throws Exception {
    testTypes(
        "function fn() {" +
        "  " +
        "  var out = 0;" +
        "  try {\n" +
        "    foo();\n" +
        "  } catch ( e) {\n" +
        "    out = e;" +
        "  }" +
        "}\n",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCatchExpression2
  public void testCatchExpression2() throws Exception {
    testTypes(
        "function fn() {" +
        "  " +
        "  var out = 0;" +
        "  " +
        "  var e;" +
        "  try {\n" +
        "    foo();\n" +
        "  } catch (e) {\n" +
        "    out = e;" +
        "  }" +
        "}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized1
  public void testTemplatized1() throws Exception {
    testTypes(
        "" +
        "var arr1 = [];\n" +
        "" +
        "var arr2 = [];\n" +
        "arr1 = arr2;",
        "assignment\n" +
        "found   : Array.<number>\n" +
        "required: Array.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized2
  public void testTemplatized2() throws Exception {
    testTypes(
        "" +
        "var arr1 = ([]);\n",
        "initializing variable\n" +
        "found   : Array.<number>\n" +
        "required: Array.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized3
  public void testTemplatized3() throws Exception {
    testTypes(
        "" +
        "var arr1 = ([]);\n",
        "initializing variable\n" +
        "found   : Array.<number>\n" +
        "required: (Array.<string>|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized4
  public void testTemplatized4() throws Exception {
    testTypes(
        "" +
        "var arr1 = [];\n" +
        "" +
        "var arr2 = arr1;\n",
        "initializing variable\n" +
        "found   : (Array.<string>|null)\n" +
        "required: (Array.<number>|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized5
  public void testTemplatized5() throws Exception {
    testTypes(
        "\n" +
        "var some = function(obj) {" +
        "  for (var key in obj) if (obj[key]) return true;" +
        "};" +
        " function f() { return []; }" +
        " function g() { return []; }" +
        "some(f());\n" +
        "some(g());\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedTypeSubtypes2
  public void testTemplatizedTypeSubtypes2() throws Exception {
    JSType arrayOfNumber = createTemplatizedType(
        ARRAY_TYPE, NUMBER_TYPE);
    JSType arrayOfString = createTemplatizedType(
        ARRAY_TYPE, STRING_TYPE);
    assertFalse(arrayOfString.isSubtype(createUnionType(arrayOfNumber, NULL_VOID)));

  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssumption
  public void testAssumption() {
    assuming("x", NUMBER_TYPE);
    inFunction("");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testVar
  public void testVar() {
    inFunction("var x = 1;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEmptyVar
  public void testEmptyVar() {
    inFunction("var x;");
    verify("x", VOID_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignment
  public void testAssignment() {
    assuming("x", OBJECT_TYPE);
    inFunction("x = 1;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testExprWithinCast
  public void testExprWithinCast() {
    assuming("x", OBJECT_TYPE);
    inFunction(" (x = 1);");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetProp
  public void testGetProp() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x.y();");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetElemDereference
  public void testGetElemDereference() {
    assuming("x", createUndefinableType(OBJECT_TYPE));
    inFunction("x['z'] = 3;");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf1
  public void testIf1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {}; if (x) { y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf1a
  public void testIf1a() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {}; if (x != null) { y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf2
  public void testIf2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = x; if (x) { y = x; } else { y = {}; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf3
  public void testIf3() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 1; if (x) { y = x; }");
    verify("y", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference1
  public void testPropertyInference1() {
    ObjectType thisType = registry.createAnonymousObjectType(null);
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; if (this.foo) { y = this.foo; }");
    verify("y", createUnionType(NUMBER_TYPE, STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference2
  public void testPropertyInference2() {
    ObjectType thisType = registry.createAnonymousObjectType(null);
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; this.foo = 'x'; y = this.foo;");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference3
  public void testPropertyInference3() {
    ObjectType thisType = registry.createAnonymousObjectType(null);
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; this.foo = x; y = this.foo;");
    verify("y", CHECKED_UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert1
  public void testAssert1() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assert(x); out2 = x;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert1a
  public void testAssert1a() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assert(x !== null); out2 = x;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert2
  public void testAssert2() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("goog.asserts.assert(1, x); out1 = x;");
    verify("out1", startType);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert3
  public void testAssert3() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("out1 = x; goog.asserts.assert(x && y); out2 = x; out3 = y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
    verify("out3", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert4
  public void testAssert4() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("out1 = x; goog.asserts.assert(x && !y); out2 = x; out3 = y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
    verify("out3", NULL_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert5
  public void testAssert5() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("goog.asserts.assert(x || y); out1 = x; out2 = y;");
    verify("out1", startType);
    verify("out2", startType);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert6
  public void testAssert6() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x.y", startType);
    inFunction("out1 = x.y; goog.asserts.assert(x.y); out2 = x.y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert7
  public void testAssert7() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assert(x);");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert8
  public void testAssert8() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assert(x != null);");
    verify("out1", startType);
    verify("out2", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert9
  public void testAssert9() {
    JSType startType = createNullableType(NUMBER_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assert(y = x);");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert10
  public void testAssert10() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("out1 = x; out2 = goog.asserts.assert(x && y); out3 = x;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
    verify("out3", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertNumber
  public void testAssertNumber() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertNumber(x); out2 = x;");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertNumber2
  public void testAssertNumber2() {
    
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("goog.asserts.assertNumber(x + x); out1 = x;");
    verify("out1", startType);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertNumber3
  public void testAssertNumber3() {
    
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assertNumber(x + x);");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertString
  public void testAssertString() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertString(x); out2 = x;");
    verify("out1", startType);
    verify("out2", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertFunction
  public void testAssertFunction() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertFunction(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", FUNCTION_INSTANCE_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject
  public void testAssertObject() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertObject(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject2
  public void testAssertObject2() {
    JSType startType = createNullableType(ARRAY_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertObject(x); out2 = x;");
    verify("out1", startType);
    verify("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject3
  public void testAssertObject3() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x.y", startType);
    inFunction("out1 = x.y; goog.asserts.assertObject(x.y); out2 = x.y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject4
  public void testAssertObject4() {
    JSType startType = createNullableType(ARRAY_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assertObject(x);");
    verify("out1", startType);
    verify("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject5
  public void testAssertObject5() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction(
        "out1 = x;" +
        "out2 =  (goog.asserts.assertObject(x));");
    verify("out1", startType);
    verify("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertArray
  public void testAssertArray() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertArray(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof1
  public void testAssertInstanceof1() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertInstanceof(x); out2 = x;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof2
  public void testAssertInstanceof2() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertInstanceof(x, String); out2 = x;");
    verify("out1", startType);
    verify("out2", STRING_OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof3
  public void testAssertInstanceof3() {
    JSType startType = registry.getNativeType(UNKNOWN_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertInstanceof(x, String); out2 = x;");
    verify("out1", startType);
    verify("out2", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof4
  public void testAssertInstanceof4() {
    JSType startType = registry.getNativeType(STRING_OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertInstanceof(x, Object); out2 = x;");
    verify("out1", startType);
    verify("out2", STRING_OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof5
  public void testAssertInstanceof5() {
    JSType startType = registry.getNativeType(ALL_TYPE);
    assuming("x", startType);
    inFunction(
        "out1 = x; goog.asserts.assertInstanceof(x, String); var r = x;");
    verify("out1", startType);
    verify("x", STRING_OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertWithIsDefAndNotNull
  public void testAssertWithIsDefAndNotNull() {
    JSType startType = createNullableType(NUMBER_TYPE);
    assuming("x", startType);
    inFunction(
        "out1 = x;" +
        "goog.asserts.assert(goog.isDefAndNotNull(x));" +
        "out2 = x;");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIsDefAndNoResolvedType
  public void testIsDefAndNoResolvedType() {
    JSType startType = createUndefinableType(NO_RESOLVED_TYPE);
    assuming("x", startType);
    inFunction(
        "out1 = x;" +
        "if (goog.isDef(x)) { out2a = x; out2b = x.length; out2c = x; }" +
        "out3 = x;" +
        "if (goog.isDef(x)) { out4 = x; }");
    verify("out1", startType);
    verify("out2a", NO_RESOLVED_TYPE);
    verify("out2b", CHECKED_UNKNOWN_TYPE);
    verify("out2c", NO_RESOLVED_TYPE);
    verify("out3", startType);
    verify("out4", NO_RESOLVED_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertWithNotIsNull
  public void testAssertWithNotIsNull() {
    JSType startType = createNullableType(NUMBER_TYPE);
    assuming("x", startType);
    inFunction(
        "out1 = x;" +
        "goog.asserts.assert(!goog.isNull(x));" +
        "out2 = x;");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testReturn1
  public void testReturn1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("if (x) { return x; }\nx = {};\nreturn x;");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testReturn2
  public void testReturn2() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("if (!x) { x = 0; }\nreturn x;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testWhile1
  public void testWhile1() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("while (!x) { if (x == null) { x = 0; } else { x = 1; } }");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testWhile2
  public void testWhile2() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("while (!x) { x = {}; }");
    verifySubtypeOf("x", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testDo
  public void testDo() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("do { x = 1; } while (!x);");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor1
  public void testFor1() {
    assuming("y", NUMBER_TYPE);
    inFunction("var x = null; var i = null; for (i=y; !i; i=1) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor2
  public void testFor2() {
    assuming("y", OBJECT_TYPE);
    inFunction("var x = null; var i = null; for (i in y) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", createNullableType(STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor3
  public void testFor3() {
    assuming("y", OBJECT_TYPE);
    inFunction("var x = null; var i = null; for (var i in y) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", createNullableType(STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor4
  public void testFor4() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {};\n"  +
        "if (x) { for (var i = 0; i < 10; i++) { break; } y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor5
  public void testFor5() {
    assuming("y", templatize(
        getNativeObjectType(ARRAY_TYPE),
        ImmutableList.of(getNativeType(NUMBER_TYPE))));
    inFunction(
        "var x = null; for (var i = 0; i < y.length; i++) { x = y[i]; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor6
  public void testFor6() {
    assuming("y", getNativeObjectType(ARRAY_TYPE));
    inFunction(
        "var x = null;" +
        "for (var i = 0; i < y.length; i++) { " +
        " if (y[i] == 'z') { x = y[i]; } " +
        "}");
    verify("x", getNativeType(UNKNOWN_TYPE));
    verify("i", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch1
  public void testSwitch1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; switch(x) {\n" +
        "case 1: y = 1; break;\n" +
        "case 2: y = {};\n" +
        "case 3: y = {};\n" +
        "default: y = 0;}");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch2
  public void testSwitch2() {
    assuming("x", ALL_TYPE);
    inFunction("var y = null; switch (typeof x) {\n" +
        "case 'string':\n" +
        "  y = x;\n" +
        "  return;" +
        "default:\n" +
        "  y = 'a';\n" +
        "}");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch3
  public void testSwitch3() {
    assuming("x",
        createNullableType(createUnionType(NUMBER_TYPE, STRING_TYPE)));
    inFunction("var y; var z; switch (typeof x) {\n" +
        "case 'string':\n" +
        "  y = 1; z = null;\n" +
        "  return;\n" +
        "case 'number':\n" +
        "  y = x; z = null;\n" +
        "  return;" +
        "default:\n" +
        "  y = 1; z = x;\n" +
        "}");
    verify("y", NUMBER_TYPE);
    verify("z", NULL_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch4
  public void testSwitch4() {
    assuming("x", ALL_TYPE);
    inFunction("var y = null; switch (typeof x) {\n" +
        "case 'string':\n" +
        "case 'number':\n" +
        "  y = x;\n" +
        "  return;\n" +
        "default:\n" +
        "  y = 1;\n" +
        "}\n");
    verify("y", createUnionType(NUMBER_TYPE, STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCall1
  public void testCall1() {
    assuming("x",
        createNullableType(
            registry.createFunctionType(registry.getNativeType(NUMBER_TYPE))));
    inFunction("var y = x();");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testNew1
  public void testNew1() {
    assuming("x",
        createNullableType(
            registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE)));
    inFunction("var y = new x();");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInnerFunction1
  public void testInnerFunction1() {
    inFunction("var x = 1; function f() { x = null; };");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInnerFunction2
  public void testInnerFunction2() {
    inFunction("var x = 1; var f = function() { x = null; };");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testHook
  public void testHook() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = x ? x : {};");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testThrow
  public void testThrow() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("var y = 1;\n" +
        "if (x == null) { throw new Error('x is null') }\n" +
        "y = x;");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry1
  public void testTry1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; try { y = null; } finally { y = x; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry2
  public void testTry2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null;\n" +
        "try {  } catch (e) { y = null; } finally { y = x; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry3
  public void testTry3() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; try { y = x; } catch (e) { }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCatch1
  public void testCatch1() {
    inFunction("var y = null; try { foo(); } catch (e) { y = e; }");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCatch2
  public void testCatch2() {
    inFunction("var y = null; var e = 3; try { foo(); } catch (e) { y = e; }");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnknownType1
  public void testUnknownType1() {
    inFunction("var y = 3; y = x;");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnknownType2
  public void testUnknownType2() {
    assuming("x", ARRAY_TYPE);
    inFunction("var y = 5; y = x[0];");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInfiniteLoop1
  public void testInfiniteLoop1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x = {}; while(x != null) { x = {}; }");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInfiniteLoop2
  public void testInfiniteLoop2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x = {}; do { x = null; } while (x == null);");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testJoin1
  public void testJoin1() {
    JSType unknownOrNull = createUnionType(NULL_TYPE, UNKNOWN_TYPE);
    assuming("x", BOOLEAN_TYPE);
    assuming("unknownOrNull", unknownOrNull);
    inFunction("var y; if (x) y = unknownOrNull; else y = null;");
    verify("y", unknownOrNull);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testJoin2
  public void testJoin2() {
    JSType unknownOrNull = createUnionType(NULL_TYPE, UNKNOWN_TYPE);
    assuming("x", BOOLEAN_TYPE);
    assuming("unknownOrNull", unknownOrNull);
    inFunction("var y; if (x) y = null; else y = unknownOrNull;");
    verify("y", unknownOrNull);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testArrayLit
  public void testArrayLit() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 3; if (x) { x = [y = x]; }");
    verify("x", createUnionType(NULL_TYPE, ARRAY_TYPE));
    verify("y", createUnionType(NUMBER_TYPE, OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetElem
  public void testGetElem() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 3; if (x) { x = x[y = x]; }");
    verify("x", UNKNOWN_TYPE);
    verify("y", createUnionType(NUMBER_TYPE, OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI1
  public void testEnumRAI1() {
    JSType enumType = createEnumType("MyEnum", ARRAY_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (x) y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI2
  public void testEnumRAI2() {
    JSType enumType = createEnumType("MyEnum", NUMBER_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (typeof x == 'number') y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI3
  public void testEnumRAI3() {
    JSType enumType = createEnumType("MyEnum", NUMBER_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (x && typeof x == 'number') y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI4
  public void testEnumRAI4() {
    JSType enumType = createEnumType("MyEnum",
        createUnionType(STRING_TYPE, NUMBER_TYPE)).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (typeof x == 'number') y = x;");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingAnd
  public void testShortCircuitingAnd() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; if (x && (y = 3)) { }");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingAnd2
  public void testShortCircuitingAnd2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; var z = 4; if (x && (y = 3)) { z = y; }");
    verify("z", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingOr
  public void testShortCircuitingOr() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; if (x || (y = 3)) { }");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingOr2
  public void testShortCircuitingOr2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; var z = 4; if (x || (y = 3)) { z = y; }");
    verify("z", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignInCondition
  public void testAssignInCondition() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("var y; if (!(y = x)) { y = 3; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf1
  public void testInstanceOf1() {
    assuming("x", OBJECT_TYPE);
    inFunction("var y = null; if (x instanceof String) y = x;");
    verify("y", createNullableType(STRING_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf2
  public void testInstanceOf2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 1; if (x instanceof String) y = x;");
    verify("y", createUnionType(STRING_OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf3
  public void testInstanceOf3() {
    assuming("x", createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE));
    inFunction("var y = null; if (x instanceof String) y = x;");
    verify("y", createNullableType(STRING_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf4
  public void testInstanceOf4() {
    assuming("x", createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE));
    inFunction("var y = null; if (x instanceof String); else y = x;");
    verify("y", createNullableType(NUMBER_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf5
  public void testInstanceOf5() {
    assuming("x", OBJECT_TYPE);
    inFunction("var y = null; if (x instanceof String); else y = x;");
    verify("y", createNullableType(OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf6
  public void testInstanceOf6() {
    
    
    
    
    
    
    
    JSType startType = registry.getNativeType(UNKNOWN_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; if (x instanceof String) out2 = x;");
    verify("out1", startType);
    verify("out2", STRING_OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFlattening
  public void testFlattening() {
    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      assuming("s" + i, ALL_TYPE);
    }
    assuming("b", JSTypeNative.BOOLEAN_TYPE);
    StringBuilder body = new StringBuilder();
    body.append("if (b) {");
    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      body.append("s");
      body.append(i);
      body.append(" = 1;\n");
    }
    body.append(" } else { ");
    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      body.append("s");
      body.append(i);
      body.append(" = 'ONE';\n");
    }
    body.append("}");
    JSType numberORString = createUnionType(NUMBER_TYPE, STRING_TYPE);
    inFunction(body.toString());

    for (int i = 0; i < LinkedFlowScope.MAX_DEPTH + 1; i++) {
      verify("s" + i, numberORString);
    }
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnary
  public void testUnary() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = +x;");
    verify("y", NUMBER_TYPE);
    inFunction("var z = -x;");
    verify("z", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAdd1
  public void testAdd1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = x + 5;");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAdd2
  public void testAdd2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = x + '5';");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAdd3
  public void testAdd3() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = '5' + x;");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignAdd
  public void testAssignAdd() {
    assuming("x", NUMBER_TYPE);
    inFunction("x += '5';");
    verify("x", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testComparison
  public void testComparison() {
    inFunction("var x = 'foo'; var y = (x = 3) < 4;");
    verify("x", NUMBER_TYPE);
    inFunction("var x = 'foo'; var y = (x = 3) > 4;");
    verify("x", NUMBER_TYPE);
    inFunction("var x = 'foo'; var y = (x = 3) <= 4;");
    verify("x", NUMBER_TYPE);
    inFunction("var x = 'foo'; var y = (x = 3) >= 4;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testThrownExpression
  public void testThrownExpression() {
    inFunction("var x = 'foo'; "
               + "try { throw new Error(x = 3); } catch (ex) {}");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testObjectLit
  public void testObjectLit() {
    inFunction("var x = {}; var out = x.a;");
    verify("out", UNKNOWN_TYPE);  

    inFunction("var x = {a:1}; var out = x.a;");
    verify("out", NUMBER_TYPE);

    inFunction("var x = {a:1}; var out = x.a; x.a = 'string'; var out2 = x.a;");
    verify("out", NUMBER_TYPE);
    verify("out2", STRING_TYPE);

    inFunction("var x = { get a() {return 1} }; var out = x.a;");
    verify("out", UNKNOWN_TYPE);

    inFunction(
        "var x = {" +
        "   get a() {return 1}" +
        "};" +
        "var out = x.a;");
    verify("out", NUMBER_TYPE);

    inFunction("var x = { set a(b) {} }; var out = x.a;");
    verify("out", UNKNOWN_TYPE);

    inFunction("var x = { " +
            " set a(b) {} };" +
            "var out = x.a;");
    verify("out", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCast1
  public void testCast1() {
    inFunction("var x =  (this);");
    verify("x", createNullableType(OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCast2
  public void testCast2() {
    inFunction(
        "" +
        "Object.prototype.method = function() { return true; };" +
        "var x =  (this).method;");
    verify(
        "x",
        registry.createFunctionType(
            registry.getNativeObjectType(OBJECT_TYPE),
            registry.getNativeType(BOOLEAN_TYPE),
            ImmutableList.<JSType>of() ));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testBackwardsInferenceCall
  public void testBackwardsInferenceCall() {
    inFunction(
        "" +
        "function f(x) {}" +
        "var y = {};" +
        "f(y);");

    assertEquals("{foo: (number|undefined)}", getType("y").toString());
  }

// com.google.javascript.jscomp.TypeInferenceTest::testBackwardsInferenceNew
  public void testBackwardsInferenceNew() {
    inFunction(
        "" +
        "function F(x) {}" +
        "var y = {};" +
        "new F(y);");

    assertEquals("{foo: (number|undefined)}", getType("y").toString());
  }

// com.google.javascript.jscomp.TypeInferenceTest::testNoThisInference
  public void testNoThisInference() {
    JSType thisType = createNullableType(OBJECT_TYPE);
    assumingThisType(thisType);
    inFunction("var out = 3; if (goog.isNull(this)) out = this;");
    verify("out", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testRecordInference
  public void testRecordInference() {
    inFunction(
        "" +
        "function f(x) {}" +
        "var out = {};" +
        "f(out);");
    assertEquals("{a: (boolean|undefined), b: (string|undefined)}",
        getType("out").toString());
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIssue785
  public void testIssue785() {
    inFunction("" +
               "function f(x) {}" +
               "var out = {};" +
               "f(out);");
    assertEquals("{prop: (string|undefined)}", getType("out").toString());
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertTypeofProp
  public void testAssertTypeofProp() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction(
        "goog.asserts.assert(typeof x.prop != 'undefined');" +
        "out = x.prop;");
    verify("out", CHECKED_UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeValidatorTest::testBasicMismatch
  public void testBasicMismatch() throws Exception {
    testSame(" function f(x) {} f('a');",
        TYPE_MISMATCH_WARNING);
    assertMismatches(Lists.newArrayList(fromNatives(STRING_TYPE, NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testFunctionMismatch
  public void testFunctionMismatch() throws Exception {
    testSame(
        " function f(x) { return x; }",
        TYPE_MISMATCH_WARNING);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType string = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    JSType number = registry.getNativeType(NUMBER_TYPE);
    JSType firstFunction = registry.createFunctionType(number, string);
    JSType secondFunction = registry.createFunctionType(string, bool);

    assertMismatches(
        Lists.newArrayList(
            new TypeMismatch(firstFunction, secondFunction, null),
            fromNatives(STRING_TYPE, BOOLEAN_TYPE),
            fromNatives(NUMBER_TYPE, STRING_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testFunctionMismatch2
  public void testFunctionMismatch2() throws Exception {
    testSame(
        " function f(x) { return x; }",
        TYPE_MISMATCH_WARNING);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType string = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    JSType number = registry.getNativeType(NUMBER_TYPE);
    JSType firstFunction = registry.createFunctionType(number, string);
    JSType secondFunction = registry.createFunctionType(number, bool);

    assertMismatches(
        Lists.newArrayList(
            new TypeMismatch(firstFunction, secondFunction, null),
            fromNatives(STRING_TYPE, BOOLEAN_TYPE)));
  }

// com.google.javascript.jscomp.TypeValidatorTest::testNullUndefined
  public void testNullUndefined() {
    testSame(" function f(x) {}\n" +
             "f( ('a'));",
             TYPE_MISMATCH_WARNING);
    assertMismatches(Collections.<TypeMismatch>emptyList());
  }

// com.google.javascript.jscomp.TypeValidatorTest::testSubclass
  public void testSubclass() {
    testSame("\n"  +
             "function Super() {}\n" +
             "\n" +
             "function Sub() {}\n" +
             " function f(x) {}\n" +
             "f( (new Sub));",
             TYPE_MISMATCH_WARNING);
    assertMismatches(Collections.<TypeMismatch>emptyList());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubProperty
  public void testStubProperty() {
    testSame("function Foo() {}; Foo.bar;");
    ObjectType foo = (ObjectType) globalScope.getVar("Foo").getType();
    assertFalse(foo.hasProperty("bar"));
    Asserts.assertTypeEquals(registry.getNativeType(UNKNOWN_TYPE),
        foo.getPropertyType("bar"));
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(foo), registry.getTypesWithProperty("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorProperty
  public void testConstructorProperty() {
    testSame("var foo = {};  foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (new:foo.Bar): undefined", fooBar.toString());
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPrototypePropertyMethodWithoutAnnotation
  public void testPrototypePropertyMethodWithoutAnnotation() {
    testSame("var Foo = function Foo() {};" +
             "var proto = Foo.prototype = {" +
             "   bar: function(a, b){}" +
             "};" +
             "proto.baz = function(c) {};" +
             "(function() { proto.baz = function() {}; })();");
    ObjectType foo = (ObjectType) findNameType("Foo", globalScope);
    assertTrue(foo.hasProperty("prototype"));

    ObjectType fooProto = (ObjectType) foo.getPropertyType("prototype");
    assertTrue(fooProto.hasProperty("bar"));
    assertEquals("function (?, ?): undefined",
        fooProto.getPropertyType("bar").toString());

    assertTrue(fooProto.hasProperty("baz"));
    assertEquals("function (?): undefined",
        fooProto.getPropertyType("baz").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumProperty
  public void testEnumProperty() {
    testSame("var foo = {};  foo.Bar = {XXX: 'xxx'};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));
    assertTrue(foo.isPropertyTypeDeclared("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("enum{foo.Bar}", fooBar.toString());
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty1
  public void testInferredProperty1() {
    testSame("var foo = {}; foo.Bar = 3;");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty1a
  public void testInferredProperty1a() {
    testSame("var foo = {};  foo.Bar = 3;");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty2
  public void testInferredProperty2() {
    testSame("var foo = { Bar: 3 };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty2b
  public void testInferredProperty2b() {
    testSame("var foo = {  Bar: 3 };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty2c
  public void testInferredProperty2c() {
    testSame("var foo = {  Bar: 3 };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("function (): number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty3
  public void testInferredProperty3() {
    testSame("var foo = {  get Bar() { return 3 } };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("?", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty4
  public void testInferredProperty4() {
    testSame("var foo = {  set Bar(a) {} };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("?", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty5
  public void testInferredProperty5() {
    testSame("var foo = {  get Bar() { return 3 } };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty6
  public void testInferredProperty6() {
    testSame("var foo = {  set Bar(a) {} };");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertFalse(foo.isPropertyTypeInferred("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPrototypeInit
  public void testPrototypeInit() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype = {bar: 1}; var foo = new Foo();");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("bar"));
    assertEquals("number", foo.getPropertyType("bar").toString());
    assertTrue(foo.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBogusPrototypeInit
  public void testBogusPrototypeInit() {
    
    testSame(" var goog = {}; " +
        "goog.F = {};  goog.F.prototype = {};" +
        " goog.F = function() {};");
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredPrototypeProperty1
  public void testInferredPrototypeProperty1() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype.bar = 1; var x = new Foo();");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.hasProperty("bar"));
    assertEquals("number", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredPrototypeProperty2
  public void testInferredPrototypeProperty2() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype = {bar: 1}; var x = new Foo();");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.hasProperty("bar"));
    assertEquals("number", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnum
  public void testEnum() {
    testSame(" var Foo = {BAR: 1}; var f = Foo;");
    ObjectType f = (ObjectType) findNameType("f", globalScope);
    assertTrue(f.hasProperty("BAR"));
    assertEquals("Foo.<number>", f.getPropertyType("BAR").toString());
    assertTrue(f instanceof EnumType);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumElement
  public void testEnumElement() {
    testSame(" var Foo = {BAR: 1}; var f = Foo;");
    Var bar = globalScope.getVar("Foo.BAR");
    assertNotNull(bar);
    assertEquals("Foo.<number>", bar.getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedEnum
  public void testNamespacedEnum() {
    testSame("var goog = {}; goog.ui = {};" +
        "goog.ui.Zippy = function() {};" +
        "goog.ui.Zippy.EventType = { TOGGLE: 'toggle' };" +
        "var x = goog.ui.Zippy.EventType;" +
        "var y = goog.ui.Zippy.EventType.TOGGLE;");

    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertTrue(x.isEnumType());
    assertTrue(x.hasProperty("TOGGLE"));
    assertEquals("enum{goog.ui.Zippy.EventType}", x.getReferenceName());

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertTrue(y.isSubtype(getNativeType(STRING_TYPE)));
    assertTrue(y.isEnumElementType());
    assertEquals("goog.ui.Zippy.EventType", y.getReferenceName());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testEnumAlias
  public void testEnumAlias() {
    testSame(" var Foo = {BAR: 1}; " +
        " var FooAlias = Foo; var f = FooAlias;");

    assertEquals("Foo.<number>",
        registry.getType("FooAlias").toString());
    Asserts.assertTypeEquals(registry.getType("FooAlias"),
        registry.getType("Foo"));

    ObjectType f = (ObjectType) findNameType("f", globalScope);
    assertTrue(f.hasProperty("BAR"));
    assertEquals("Foo.<number>", f.getPropertyType("BAR").toString());
    assertTrue(f instanceof EnumType);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacesEnumAlias
  public void testNamespacesEnumAlias() {
    testSame("var goog = {};  goog.Foo = {BAR: 1}; " +
        " goog.FooAlias = goog.Foo;");

    assertEquals("goog.Foo.<number>",
        registry.getType("goog.FooAlias").toString());
    Asserts.assertTypeEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedFunctionStub
  public void testCollectedFunctionStub() {
    testSame(
        " function f() { " +
        "   this.foo;" +
        "}" +
        "var x = new f();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("function (this:f): number",
        x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedFunctionStubLocal
  public void testCollectedFunctionStubLocal() {
    testSame(
        "(function() {" +
        " function f() { " +
        "   this.foo;" +
        "}" +
        "var x = new f();" +
        "});");
    ObjectType x = (ObjectType) findNameType("x", lastLocalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("function (this:f): number",
        x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedFunctionStub
  public void testNamespacedFunctionStub() {
    testSame(
        "var goog = {};" +
        " goog.foo;");

    ObjectType goog = (ObjectType) findNameType("goog", globalScope);
    assertTrue(goog.hasProperty("foo"));
    assertEquals("function (number): ?",
        goog.getPropertyType("foo").toString());
    assertTrue(goog.isPropertyTypeDeclared("foo"));

    Asserts.assertTypeEquals(globalScope.getVar("goog.foo").getType(),
        goog.getPropertyType("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedFunctionStubLocal
  public void testNamespacedFunctionStubLocal() {
    testSame(
        "(function() {" +
        "var goog = {};" +
        " goog.foo;" +
        "});");

    ObjectType goog = (ObjectType) findNameType("goog", lastLocalScope);
    assertTrue(goog.hasProperty("foo"));
    assertEquals("function (number): ?",
        goog.getPropertyType("foo").toString());
    assertTrue(goog.isPropertyTypeDeclared("foo"));

    Asserts.assertTypeEquals(lastLocalScope.getVar("goog.foo").getType(),
        goog.getPropertyType("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testCollectedCtorProperty
  public void testCollectedCtorProperty() {
    testSame(
        " function f() { " +
        "   this.foo = 3;" +
        "}" +
        "var x = new f();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("f", x.toString());
    assertTrue(x.hasProperty("foo"));
    assertEquals("number", x.getPropertyType("foo").toString());
    assertFalse(x.isPropertyTypeInferred("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyOnUnknownSuperClass1
  public void testPropertyOnUnknownSuperClass1() {
    testSame(
        "var goog = this.foo();" +
        "" +
        "function Foo() {}" +
        "Foo.prototype.bar = 1;" +
        "var x = new Foo();",
        RhinoErrorReporter.TYPE_PARSE_ERROR);
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertEquals("?", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyOnUnknownSuperClass2
  public void testPropertyOnUnknownSuperClass2() {
    testSame(
        "var goog = this.foo();" +
        "" +
        "function Foo() {}" +
        "Foo.prototype = {bar: 1};" +
        "var x = new Foo();",
        RhinoErrorReporter.TYPE_PARSE_ERROR);
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertEquals("Foo.prototype", x.getImplicitPrototype().toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertEquals("?", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction1
  public void testMethodBeforeFunction1() throws Exception {
    testSame(
        "var y = Window.prototype;" +
        "Window.prototype.alert = function(message) {};" +
        " function Window() {}\n" +
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction2
  public void testMethodBeforeFunction2() throws Exception {
    testSame(
        "var y = Window.prototype;" +
        "Window.prototype = {alert: function(message) {}};" +
        " function Window() {}\n" +
        "var window = new Window(); \n" +
        "var x = window;");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Window", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("alert"));
    assertEquals("function (this:Window, ?): undefined",
        x.getPropertyType("alert").toString());
    assertFalse(x.isPropertyTypeDeclared("alert"));

    ObjectType y = (ObjectType) findNameType("y", globalScope);
    assertEquals("?",
        y.getPropertyType("alert").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    testSame(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';" +
        "var x = new A();");

    ObjectType instanceType = (ObjectType) findNameType("x", globalScope);
    assertEquals(
        getNativeObjectType(OBJECT_TYPE).getPropertiesCount() + 3,
        instanceType.getPropertiesCount());
    Asserts.assertTypeEquals(getNativeType(NUMBER_TYPE),
        instanceType.getPropertyType("m1"));
    Asserts.assertTypeEquals(getNativeType(BOOLEAN_TYPE),
        instanceType.getPropertyType("m2"));
    Asserts.assertTypeEquals(getNativeType(STRING_TYPE),
        instanceType.getPropertyType("m3"));

    
    
    
    assertFalse(instanceType.hasOwnProperty("m1"));
    assertFalse(instanceType.hasOwnProperty("m2"));
    assertFalse(instanceType.hasOwnProperty("m3"));

    ObjectType proto1 = instanceType.getImplicitPrototype();
    assertTrue(proto1.hasOwnProperty("m1"));
    assertTrue(proto1.hasOwnProperty("m2"));
    assertTrue(proto1.hasOwnProperty("m3"));

    ObjectType proto2 = proto1.getImplicitPrototype();
    assertFalse(proto2.hasProperty("m1"));
    assertFalse(proto2.hasProperty("m2"));
    assertFalse(proto2.hasProperty("m3"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredVar
  public void testInferredVar() throws Exception {
    testSame("var x = 3; x = 'x'; x = true;");

    Var x = globalScope.getVar("x");
    assertEquals("(boolean|number|string)", x.getType().toString());
    assertTrue(x.isTypeInferred());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredVar
  public void testDeclaredVar() throws Exception {
    testSame(" var x = 3; var y = x;");

    Var x = globalScope.getVar("x");
    assertEquals("(null|number)", x.getType().toString());
    assertFalse(x.isTypeInferred());

    JSType y = findNameType("y", globalScope);
    assertEquals("(null|number)", y.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertiesOnInterface
  public void testPropertiesOnInterface() throws Exception {
    testSame(" var I = function() {};" +
        " I.prototype.bar;" +
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

    Asserts.assertTypeEquals(iPrototype, globalScope.getVar("I.prototype").getType());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertiesOnInterface2
  public void testPropertiesOnInterface2() throws Exception {
    testSame(" var I = function() {};" +
        "I.prototype = {baz: function(){}};" +
        " I.prototype.bar;");

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

    
    assertNull(globalScope.getVar("I.prototype"));
    
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns
  public void testStubsInExterns() {
    testSame(
        " function Extern() {}" +
        "Extern.prototype.bar;" +
        "var e = new Extern(); e.baz;",
        " function Foo() {}" +
        "Foo.prototype.bar;" +
        "var f = new Foo(); f.baz;", null);

    ObjectType e = (ObjectType) globalScope.getVar("e").getType();
    assertEquals("?", e.getPropertyType("bar").toString());
    assertEquals("?", e.getPropertyType("baz").toString());

    ObjectType f = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("?", f.getPropertyType("bar").toString());
    assertFalse(f.hasProperty("baz"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns2
  public void testStubsInExterns2() {
    testSame(
        " function Extern() {}" +
        " var myExtern;" +
        " myExtern.foo;",
        "", null);

    JSType e = globalScope.getVar("myExtern").getType();
    assertEquals("(Extern|null)", e.toString());

    ObjectType externType = (ObjectType) e.restrictByNotNullOrUndefined();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externType.hasOwnProperty("foo"));
    assertTrue(externType.isPropertyTypeDeclared("foo"));
    assertEquals("number", externType.getPropertyType("foo").toString());
    assertTrue(externType.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns3
  public void testStubsInExterns3() {
    testSame(
        " myExtern.foo;" +
        " var myExtern;" +
        " function Extern() {}",
        "", null);

    JSType e = globalScope.getVar("myExtern").getType();
    assertEquals("(Extern|null)", e.toString());

    ObjectType externType = (ObjectType) e.restrictByNotNullOrUndefined();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externType.hasOwnProperty("foo"));
    assertTrue(externType.isPropertyTypeDeclared("foo"));
    assertEquals("number", externType.getPropertyType("foo").toString());
    assertTrue(externType.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testStubsInExterns4
  public void testStubsInExterns4() {
    testSame(
        "Extern.prototype.foo;" +
        " function Extern() {}",
        "", null);

    JSType e = globalScope.getVar("Extern").getType();
    assertEquals("function (new:Extern): ?", e.toString());

    ObjectType externProto = ((FunctionType) e).getPrototype();
    assertTrue(globalScope.getRootNode().toStringTree(),
        externProto.hasOwnProperty("foo"));
    assertTrue(externProto.isPropertyTypeInferred("foo"));
    assertEquals("?", externProto.getPropertyType("foo").toString());
    assertTrue(externProto.isPropertyInExterns("foo"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns1
  public void testPropertyInExterns1() {
    testSame(
        " function Extern() {}" +
        " var extern;" +
        " extern.one;",
        " function Normal() {}" +
        " var normal;" +
        " normal.one;", null);

    JSType e = globalScope.getVar("Extern").getType();
    ObjectType externInstance = ((FunctionType) e).getInstanceType();
    assertTrue(externInstance.hasOwnProperty("one"));
    assertTrue(externInstance.isPropertyTypeDeclared("one"));
    assertEquals("function (): number",
        externInstance.getPropertyType("one").toString());

    JSType n = globalScope.getVar("Normal").getType();
    ObjectType normalInstance = ((FunctionType) n).getInstanceType();
    assertFalse(normalInstance.hasOwnProperty("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns2
  public void testPropertyInExterns2() {
    testSame(
        " var extern;" +
        " extern.one;",
        " var normal;" +
        " normal.one;", null);

    JSType e = globalScope.getVar("extern").getType();
    assertFalse(e.dereference().hasOwnProperty("one"));

    JSType normal = globalScope.getVar("normal").getType();
    assertFalse(normal.dereference().hasOwnProperty("one"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyInExterns3
  public void testPropertyInExterns3() {
    testSame(
        " function Object(x) {}" +
        " Object.one;", "", null);

    ObjectType obj = globalScope.getVar("Object").getType().dereference();
    assertTrue(obj.hasOwnProperty("one"));
    assertEquals("number", obj.getPropertyType("one").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTypedStubsInExterns
  public void testTypedStubsInExterns() {
    testSame(
        " " +
        "function Function(var_args) {}" +
        " Function.prototype.apply;",
        "var f = new Function();", null);

    ObjectType f = (ObjectType) globalScope.getVar("f").getType();

    
    
    assertEquals(
        "function (?=, (Object|null)=): ?",
        f.getPropertyType("apply").toString());

    
    
    FunctionType func = (FunctionType) globalScope.getVar("Function").getType();
    assertEquals("Function",
        func.getPrototype().getPropertyType("apply").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTypesInExterns
  public void testTypesInExterns() throws Exception {
    testSame(
        CompilerTypeTestCase.DEFAULT_EXTERNS,
        "", null);

    Var v = globalScope.getVar("Object");
    FunctionType obj = (FunctionType) v.getType();
    assertEquals("function (new:Object, *=): ?", obj.toString());
    assertNotNull(v.getNode());
    assertNotNull(v.input);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyDeclarationOnInstanceType
  public void testPropertyDeclarationOnInstanceType() {
    testSame(
        " var a = {};" +
        " a.name = 0;");

    assertEquals("number", globalScope.getVar("a.name").getType().toString());

    ObjectType a = (ObjectType) (globalScope.getVar("a").getType());
    assertFalse(a.hasProperty("name"));
    assertFalse(getNativeObjectType(OBJECT_TYPE).hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyDeclarationOnRecordType
  public void testPropertyDeclarationOnRecordType() {
    testSame(
        " var a = {foo: 3};" +
        " a.name = 0;");

    assertEquals("number", globalScope.getVar("a.name").getType().toString());

    ObjectType a = (ObjectType) (globalScope.getVar("a").getType());
    assertEquals("{foo: number}", a.toString());
    assertFalse(a.hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalThis1
  public void testGlobalThis1() {
    testSame(
        " function Window() {}" +
        "Window.prototype.alert = function() {};" +
        "var x = this;");

    ObjectType x = (ObjectType) (globalScope.getVar("x").getType());
    FunctionType windowCtor =
        (FunctionType) (globalScope.getVar("Window").getType());
    assertEquals("global this", x.toString());
    assertTrue(x.isSubtype(windowCtor.getInstanceType()));
    assertFalse(x.isEquivalentTo(windowCtor.getInstanceType()));
    assertTrue(x.hasProperty("alert"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalThis2
  public void testGlobalThis2() {
    testSame(
        " function Window() {}" +
        "Window.prototype = {alert: function() {}};" +
        "var x = this;");

    ObjectType x = (ObjectType) (globalScope.getVar("x").getType());
    FunctionType windowCtor =
        (FunctionType) (globalScope.getVar("Window").getType());
    assertEquals("global this", x.toString());
    assertTrue(x.isSubtype(windowCtor.getInstanceType()));
    assertFalse(x.isEquivalentTo(windowCtor.getInstanceType()));
    assertTrue(x.hasProperty("alert"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testObjectLiteralCast
  public void testObjectLiteralCast() {
    
    
    testSame(" A.B = function() {}\n" +
             "A.B.prototype.isEnabled = true;\n" +
             "goog.reflect.object(A.B, {isEnabled: 3})\n" +
             "var x = (new A.B()).isEnabled;");

    assertEquals("A.B",
        findTokenType(Token.OBJECTLIT, globalScope).toString());
    assertEquals("boolean",
        findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadObjectLiteralCast1
  public void testBadObjectLiteralCast1() {
    testSame(" A.B = function() {}\n" +
             "goog.reflect.object(A.B, 1)",
             ClosureCodingConvention.OBJECTLIT_EXPECTED);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadObjectLiteralCast2
  public void testBadObjectLiteralCast2() {
    testSame("goog.reflect.object(A.B, {})",
             TypedScopeCreator.CONSTRUCTOR_EXPECTED);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorNode
  public void testConstructorNode() {
    testSame("var goog = {};  goog.Foo = function() {};");

    ObjectType ctor = (ObjectType) (findNameType("goog.Foo", globalScope));
    assertNotNull(ctor);
    assertTrue(ctor.isConstructor());
    assertEquals("function (new:goog.Foo): undefined", ctor.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testForLoopIntegration
  public void testForLoopIntegration() {
    testSame("var y = 3; for (var x = true; x; y = x) {}");

    Var y = globalScope.getVar("y");
    assertTrue(y.isTypeInferred());
    assertEquals("(boolean|number)", y.getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorAlias
  public void testConstructorAlias() {
    testSame(
        " var Foo = function() {};" +
        " var FooAlias = Foo;");
    assertEquals("Foo", registry.getType("FooAlias").toString());
    Asserts.assertTypeEquals(registry.getType("Foo"), registry.getType("FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedConstructorAlias
  public void testNamespacedConstructorAlias() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;");
    assertEquals("goog.Foo", registry.getType("goog.FooAlias").toString());
    Asserts.assertTypeEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType1
  public void testTemplateType1() {
    testSame(
        "\n" +
        "function bind(fn, thisObj) {}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.baz = function() {};\n" +
        "bind(function() { var g = this; var f = this.baz(); }, new Foo());");
    assertEquals("Foo", findNameType("g", lastLocalScope).toString());
    assertEquals("number", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType2
  public void testTemplateType2() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val = 'hi';\n" +
        "var result = f(val);");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType2a
  public void testTemplateType2a() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val = 'hi';\n" +
        "var result = f(val);");
    assertEquals("(string|undefined)",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType2b
  public void testTemplateType2b() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val = 'hi';\n" +
        "var result = f(val);");
    assertEquals("(string|undefined)",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType3
  public void testTemplateType3() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var val1 = 'hi';\n" +
        "var result1 = f(val1);" +
        "\n" +
        "var val2 = 0;\n" +
        "var result2 = f(val2);");

    assertEquals("string", findNameType("result1", globalScope).toString());
    assertEquals("number", findNameType("result2", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType4
  public void testTemplateType4() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var arr = [];\n" +
        "(function () {var result = f(arr);})();");

    JSType resultType = findNameType("result", lastLocalScope);
    assertEquals("Array.<string>", resultType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType4a
  public void testTemplateType4a() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var g = function(){return 'hi'};\n" +
        "(function () {var result = f(g);})();");

    JSType resultType = findNameType("result", lastLocalScope);
    assertEquals("string", resultType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType4b
  public void testTemplateType4b() {
    testSame(
        "\n" +
        "function f(x) {\n" +
        "  return x;\n" +
        "}" +
        "\n" +
        "var g = function(x){};\n" +
        "(function () {var result = f(g);})();");

    JSType resultType = findNameType("result", lastLocalScope);
    assertEquals("string", resultType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType5
  public void testTemplateType5() {
    testSame(
        "\n" +
        "function f(arr) {\n" +
        "  return arr;\n" +
        "}" +
        "\n" +
        "var arr = [];\n" +
        "var result = f(arr);");

    assertEquals("Array.<string>", findNameTypeStr("result", globalScope));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType6
  public void testTemplateType6() {
    testSame(
        "\n" +
        "function f(arr) {\n" +
        "  return arr;\n" +
        "}" +
        "\n" +
        "var arr = [];\n" +
        "var result = f(arr);");

    assertEquals("Array.<string>", findNameTypeStr("result", globalScope));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType7
  public void testTemplateType7() {
    testSame(
        "var goog = {};\n" +
        "goog.array = {};\n" +
        "\n" +
        "goog.array.filter = function(arr, f, opt_obj) {\n" +
        "  var res = [];\n" +
        "  for (var i = 0; i < arr.length; i++) {\n" +
        "     if (f.call(opt_obj, arr[i], i, arr)) {\n" +
        "        res.push(val);\n" +
        "     }\n" +
        "  }\n" +
        "  return res;\n" +
        "}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "var arr = [];\n" +
        "var result = goog.array.filter(arr," +
        "  function(a,b,c) {var self=this;}, new Foo());");

    assertEquals("Foo", findNameType("self", lastLocalScope).toString());
    assertEquals("string", findNameType("a", lastLocalScope).toString());
    assertEquals("number", findNameType("b", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("c", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType7b
  public void testTemplateType7b() {
    testSame(
        "var goog = {};\n" +
        "goog.array = {};\n" +
        "\n" +
        "goog.array.filter = function(arr, f, opt_obj) {\n" +
        "  var res = [];\n" +
        "  for (var i = 0; i < arr.length; i++) {\n" +
        "     if (f.call(opt_obj, arr[i], i, arr)) {\n" +
        "        res.push(val);\n" +
        "     }\n" +
        "  }\n" +
        "  return res;\n" +
        "}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "var arr = [];\n" +
        "var result = goog.array.filter(arr," +
        "  function(a,b,c) {var self=this;}, new Foo());");

    assertEquals("Foo", findNameType("self", lastLocalScope).toString());
    assertEquals("string", findNameType("a", lastLocalScope).toString());
    assertEquals("number", findNameType("b", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("c", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType7c
  public void testTemplateType7c() {
    testSame(
        "var goog = {};\n" +
        "goog.array = {};\n" +
        "\n" +
        "goog.array.filter = function(arr, f, opt_obj) {\n" +
        "  var res = [];\n" +
        "  for (var i = 0; i < arr.length; i++) {\n" +
        "     if (f.call(opt_obj, arr[i], i, arr)) {\n" +
        "        res.push(val);\n" +
        "     }\n" +
        "  }\n" +
        "  return res;\n" +
        "}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "var arr = [];\n" +
        "var result = goog.array.filter(arr," +
        "  function(a,b,c) {var self=this;}, new Foo());");

    assertEquals("Foo", findNameType("self", lastLocalScope).toString());
    assertEquals("string", findNameType("a", lastLocalScope).toString());
    assertEquals("number", findNameType("b", lastLocalScope).toString());
    assertEquals("(Array.<string>|null)",
        findNameType("c", lastLocalScope).toString());
    assertEquals("Array.<string>",
        findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType9
  public void testTemplateType9() {
    testSame(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.method = function() {};\n" +
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "var g = new Bar().method();\n");
    assertEquals("Bar", findNameType("g", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType10
  public void testTemplateType10() {
    
    
    
    testSame(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "\n" +
        "Foo.prototype.method = function() {var g = this;};\n");
    assertEquals("T", findNameType("g", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType11
  public void testTemplateType11() {
    testSame(
        "\n" +
        "var method = function() {};\n" +
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "var g = method().call(new Bar());\n");
    
    assertEquals("?", findNameType("g", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType12
  public void testTemplateType12() {
    testSame(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "\n" +
        "Foo.prototype.method = function() {var g = this;};\n");
    assertEquals("(Array.<T>|{length: number})",
        findNameType("g", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType1
  public void testClassTemplateType1() {
    
    testSame(
        "\n" +
        "function C() {};\n" +
        "" +
        "\n" +
        "C.prototype.method = function() {}\n" +
        "" +
        " var x = new C();\n" +
        "var result = x.method();\n");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType2
  public void testClassTemplateType2() {
    
    
    testSame(
        " var ns = {};" +
        "\n" +
        "ns.C = function() {};\n" +
        "" +
        "\n" +
        "ns.C.prototype.method = function() {}\n" +
        "" +
        " var x = new ns.C();\n" +
        "var result = x.method();\n");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType3
  public void testClassTemplateType3() {
    
    testSame(
        "\n" +
        "function C() {\n" +
        "  \n" +
        "  this.foo;" +
        "};\n" +
        "" +
        " var x = new C();\n" +
        "var result = x.foo;\n");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType4
  public void testClassTemplateType4() {
    
    testSame(
        " var ns = {};" +
        "\n" +
        "ns.C = function() {\n" +
        "  \n" +
        "  this.foo;" +
        "};\n" +
        "" +
        " var x = new ns.C();\n" +
        "var result = x.foo;\n");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType5
  public void testClassTemplateType5() {
    
    
    testSame(
        "\n" +
        "function C() {\n" +
        "};\n" +
        "" +
        "" +
        "C.prototype.foo;\n" +
        "" +
        " var x = new C();\n" +
        "var result = x.foo;\n");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType6
  public void testClassTemplateType6() {
    
    
    testSame(
        "\n" +
        "function C() {\n" +
        "};\n" +
        "" +
        "" +
        "C.prototype.foo = 1;\n" +
        "" +
        " var x = new C();\n" +
        "var result = x.foo;\n");
    assertEquals("string", findNameType("result", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType7
  public void testClassTemplateType7() {
    
    testSame(
        "\n" +
        "function C() {};\n" +
        "" +
        "C.prototype.method = function() {\n" +
        "   var local;" +
        "}\n");
    assertEquals("T", findNameType("local", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClassTemplateType8
  public void testClassTemplateType8() {
    
    testSame(
        "\n" +
        "function C() {};\n" +
        "" +
        "C.prototype.method = function() {\n" +
        "  var local =  (x);" +
        "}\n");
    assertEquals("T", findNameType("local", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithoutJSDoc
  public void testClosureParameterTypesWithoutJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo(function(baz) { var f = baz; })\n");
    assertEquals("Object", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithJSDoc
  public void testClosureParameterTypesWithJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo(" +
        "  (function(baz) { var f = baz; }))\n");
    assertEquals("string", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDuplicateExternProperty1
  public void testDuplicateExternProperty1() {
    testSame(
        " function Foo() {}" +
        "Foo.prototype.bar;" +
        " Foo.prototype.bar; var x = (new Foo).bar;",
        null);
    assertEquals("number", findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDuplicateExternProperty2
  public void testDuplicateExternProperty2() {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.bar;" +
        "Foo.prototype.bar; var x = (new Foo).bar;", null);
    assertEquals("number", findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        " Foo.prototype.bar = abstractMethod;");
    assertEquals(
        "Function", findNameType("abstractMethod", globalScope).toString());

    FunctionType ctor = (FunctionType) findNameType("Foo", globalScope);
    ObjectType instance = ctor.getInstanceType();
    assertEquals("Foo", instance.toString());

    ObjectType proto = instance.getImplicitPrototype();
    assertEquals("Foo.prototype", proto.toString());

    assertEquals(
        "function (this:Foo, number): ?",
        proto.getPropertyType("bar").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod2
  public void testAbstractMethod2() {
    testSame(
        " var abstractMethod;" +
        " var y = abstractMethod;");
    assertEquals(
        "Function",
        findNameType("y", globalScope).toString());
    assertEquals(
        "function (number): ?",
        globalScope.getVar("y").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod3
  public void testAbstractMethod3() {
    testSame(
        " var abstractMethod;" +
        " var y = abstractMethod; y;");
    assertEquals(
        "function (number): ?",
        findNameType("y", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testAbstractMethod4
  public void testAbstractMethod4() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        "Foo.prototype = { bar: abstractMethod};");
    assertEquals(
        "Function", findNameType("abstractMethod", globalScope).toString());

    FunctionType ctor = (FunctionType) findNameType("Foo", globalScope);
    ObjectType instance = ctor.getInstanceType();
    assertEquals("Foo", instance.toString());

    ObjectType proto = instance.getImplicitPrototype();
    assertEquals("Foo.prototype", proto.toString());

    assertEquals(
        
        "function (this:Foo, number): ?",
        proto.getPropertyType("bar").toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testActiveXObject
  public void testActiveXObject() {
    testSame(
        CompilerTypeTestCase.ACTIVE_X_OBJECT_DEF,
        "var x = new ActiveXObject();", null);
    assertEquals(
        "?",
        findNameType("x", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference1
  public void testReturnTypeInference1() {
    testSame("function f() {}");
    assertEquals(
        "function (): undefined",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference2
  public void testReturnTypeInference2() {
    testSame(" function f() {}");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference3
  public void testReturnTypeInference3() {
    testSame("function f() {x: return 3;}");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference4
  public void testReturnTypeInference4() {
    testSame("function f() { throw Error(); }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference5
  public void testReturnTypeInference5() {
    testSame("function f() { if (true) { return 1; } }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testLiteralTypesInferred
  public void testLiteralTypesInferred() {
    testSame("null + true + false + 0 + '' + {}");
    assertEquals(
        "null", findTokenType(Token.NULL, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.TRUE, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.FALSE, globalScope).toString());
    assertEquals(
        "number", findTokenType(Token.NUMBER, globalScope).toString());
    assertEquals(
        "string", findTokenType(Token.STRING, globalScope).toString());
    assertEquals(
        "{}", findTokenType(Token.OBJECTLIT, globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalQualifiedNameInLocalScope
  public void testGlobalQualifiedNameInLocalScope() {
    testSame(
        "var ns = {}; " +
        "(function() { " +
        "     ns.foo = function(x) {}; })();" +
        "(function() { ns.foo(3); })();");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertEquals(
        "function (number): undefined",
        globalScope.getVar("ns.foo").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty1
  public void testDeclaredObjectLitProperty1() throws Exception {
    testSame("var x = { y: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty2
  public void testDeclaredObjectLitProperty2() throws Exception {
    testSame("var x = { y: function(z){}};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "function (number): undefined",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (number): undefined}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty3
  public void testDeclaredObjectLitProperty3() throws Exception {
    testSame("function f() {" +
        "  var x = { y: function(z){ return 3; }};" +
        "}");
    ObjectType xType = ObjectType.cast(lastLocalScope.getVar("x").getType());
    assertEquals(
        "function (?): number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (?): number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty4
  public void testDeclaredObjectLitProperty4() throws Exception {
    testSame("var x = {y: 5,  z: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number", xType.getPropertyType("y").toString());
    assertFalse(xType.isPropertyTypeDeclared("y"));
    assertTrue(xType.isPropertyTypeDeclared("z"));
    assertEquals(
        "{y: number, z: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty5
  public void testDeclaredObjectLitProperty5() throws Exception {
    testSame("var x = { prop: 3};" +
             "function f() { var y = x.prop; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty6
  public void testDeclaredObjectLitProperty6() throws Exception {
    testSame("var x = { prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertFalse(prop.isTypeInferred());
    assertFalse(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty1
  public void testInferredObjectLitProperty1() throws Exception {
    testSame("var x = {prop: 3};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("number", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty2
  public void testInferredObjectLitProperty2() throws Exception {
    testSame("var x = {prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType1
  public void testDeclaredConstType1() throws Exception {
    testSame(
        " var x = 3;" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType2
  public void testDeclaredConstType2() throws Exception {
    testSame(
        " var x = {};" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType3
  public void testDeclaredConstType3() throws Exception {
    testSame(
        " var x = {};" +
        " x.z = 'hi';" +
        "function f() { var y = x.z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType4
  public void testDeclaredConstType4() throws Exception {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.z = 'hi';" +
        "function f() { var y = (new Foo()).z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());

    ObjectType fooType =
        ((FunctionType) globalScope.getVar("Foo").getType()).getInstanceType();
    assertTrue(fooType.isPropertyTypeDeclared("z"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType5
  public void testDeclaredConstType5() throws Exception {
    testSame(
        " var goog = goog || {};" +
        " var foo = goog || {};" +
        "function f() { var y = goog; var z = foo; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());

    JSType zType = lastLocalScope.getVar("z").getType();
    assertEquals("?", zType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit1
  public void testBadCtorInit1() throws Exception {
    testSame(" var f;", CTOR_INITIALIZER);
  }
