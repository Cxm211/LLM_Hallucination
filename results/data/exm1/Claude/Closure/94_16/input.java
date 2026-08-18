// buggy code
  static boolean isValidDefineValue(Node val, Set<String> defines) {
    switch (val.getType()) {
      case Token.STRING:
      case Token.NUMBER:
      case Token.TRUE:
      case Token.FALSE:
        return true;

      // Binary operators are only valid if both children are valid.
      case Token.BITAND:
      case Token.BITNOT:
      case Token.BITOR:
      case Token.BITXOR:

      // Uniary operators are valid if the child is valid.
      case Token.NOT:
      case Token.NEG:
        return isValidDefineValue(val.getFirstChild(), defines);

      // Names are valid if and only if they are defines themselves.
      case Token.NAME:
      case Token.GETPROP:
        if (val.isQualifiedName()) {
          return defines.contains(val.getQualifiedName());
        }
    }
    return false;
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface2
  public void testAnnotatedPropertyOnInterface2() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface3
  public void testAnnotatedPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        " T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface4
  public void testAnnotatedPropertyOnInterface4() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function T() {};\n" +
        " T.prototype.f = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface5
  public void testWarnUnannotatedPropertyOnInterface5() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface6
  public void testWarnUnannotatedPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface2
  public void testDataPropertyOnInterface2() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface3
  public void testWarnDataPropertyOnInterface3() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface4
  public void testWarnDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface4
  public void testErrorMismatchingPropertyOnInterface4() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x =\n" +
        "function() {};",
        "parameter foo does not appear in u.T.prototype.x's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface5
  public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface6
  public void testErrorMismatchingPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceNonEmptyFunction
  public void testInterfaceNonEmptyFunction() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { return 'foo'; }",
        "interface member functions must have an empty body"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testDoubleNestedInterface
  public void testDoubleNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.I3 = function() {};\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticDataPropertyOnNestedInterface
  public void testStaticDataPropertyOnNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.x = 1;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() throws Exception {
    testTypes("var f; new f",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypeLoop
  public void testPrototypeLoop() throws Exception {
    testTypes(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        "Parse error. Cycle detected in inheritance chain of type T");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()",
        "assignment to property prototype of Bar\n" +
        "found   : Foo\n" +
        "required: (Array|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry1
  public void testResolutionViaRegistry1() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry2
  public void testResolutionViaRegistry2() throws Exception {
    testTypes(
        " u.T = function() {" +
        "  this.a = 0; };\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry3
  public void testResolutionViaRegistry3() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a = 0;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry4
  public void testResolutionViaRegistry4() throws Exception {
    testTypes(" u.A = function() {};\n" +
        "\nu.A.A = function() {}\n;" +
        "\nu.A.B = function() {};\n" +
        "var ab = new u.A.B();\n" +
        " var a = ab;\n" +
        " var aa = ab;\n",
        "initializing variable\n" +
        "found   : u.A.B\n" +
        "required: u.A.A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry5
  public void testResolutionViaRegistry5() throws Exception {
    Node n = parseAndTypeCheck(" u.T = function() {}; u.T");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof FunctionType);
    assertEquals("u.T",
        ((FunctionType) type).getInstanceType().getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation1
  public void testGatherProperyWithoutAnnotation1() throws Exception {
    Node n = parseAndTypeCheck(" var T = function() {};" +
        "var t; t.x; t;");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Sets.newHashSet(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation2
  public void testGatherProperyWithoutAnnotation2() throws Exception {
    TypeCheckResult ns =
        parseAndTypeCheckWithScope("var t; t.x; t;");
    Node n = ns.root;
    Scope s = ns.scope;
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertEquals(type, OBJECT_TYPE);
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Sets.newHashSet(OBJECT_TYPE),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionMasksVariableBug
  public void testFunctionMasksVariableBug() throws Exception {
    testTypes("var x = 4; var f = function x(b) { return b ? 1 : x(true); };",
        "function x masks variable (IE bug)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa1
  public void testDfa1() throws Exception {
    testTypes("var x = null;\n x = 1;\n  var y = x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa2
  public void testDfa2() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\nvar x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa3
  public void testDfa3() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\n" +
        " var x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa4
  public void testDfa4() throws Exception {
    testTypes(" function f(d) {\n" +
        "if (!d) { return; }\n" +
        " var e = d;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa5
  public void testDfa5() throws Exception {
    testTypes(" function u() {return 'a';}\n" +
        " function f(x) {\n" +
        "while (!x) { x = u(); }\nreturn x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa6
  public void testDfa6() throws Exception {
    testTypes(" function u() {return {};}\n" +
        " function f(x) {\n" +
        "while (x) { x = u(); if (!x) { x = u(); } }\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa7
  public void testDfa7() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = null;\n" +
        " function f(t) {\n" +
        "if (!t.x) { return; }\n" +
        " var e = t.x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa8
  public void testDfa8() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = '';\n" +
        "function u() {}\n" +
        " function f(t) {\n" +
        "if (u()) { t.x = 1; } else { t.x = 2; } return t.x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa9
  public void testDfa9() throws Exception {
    testTypes("function f() {\nvar x;\nx = null;\n" +
        "if (x == null) { return 0; } else { return 1; } }",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa10
  public void testDfa10() throws Exception {
    testTypes(" function g(x) {}" +
        "function f(x) {\n" +
        "if (!x) { x = ''; }\n" +
        "if (g(x)) { return 0; } else { return 1; } }",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa11
  public void testDfa11() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { if (!opt_x) { " +
        "throw new Error('x cannot be empty'); } return opt_x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa12
  public void testDfa12() throws Exception {
    testTypes("" +
        "var Bar = function(x) {};" +
        " function g(x) { return true; }" +
        " " +
        "function f(opt_x) { " +
        "  if (opt_x) { new Bar(g(opt_x) && 'x'); }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa13
  public void testDfa13() throws Exception {
    testTypes(
        "" +
        "function g(x, y, z) {}" +
        "function f() { " +
        "  var x = 'a'; g(x, x = 3, x);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast1
  public void testTypeInferenceWithCast1() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast2
  public void testTypeInferenceWithCast2() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y; y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast3
  public void testTypeInferenceWithCast3() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x));}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast4
  public void testTypeInferenceWithCast4() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x)) && 1;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast5
  public void testTypeInferenceWithCast5() throws Exception {
    testTypes(
        " function foo(x) {}" +
        " function bar(y) {" +
        "   y.length;" +
        "  foo(y.length);" +
        "}",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithClosure1
  public void testTypeInferenceWithClosure1() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithClosure2
  public void testTypeInferenceWithClosure2() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x === 3;" +
        "}",
        "condition always evaluates to the same value\n" +
        "left : (null|string)\n" +
        "right: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardPropertyReference
  public void testForwardPropertyReference() throws Exception {
    testTypes(" var Foo = function() { this.init(); };" +
        "" +
        "Foo.prototype.getString = function() {" +
        "  return this.number_;" +
        "};" +
        "Foo.prototype.init = function() {" +
        "  " +
        "  this.number_ = 3;" +
        "};",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoForwardTypeDeclaration
  public void testNoForwardTypeDeclaration() throws Exception {
    testTypes(
        " function f(x) {}",
        "Parse error. Unknown type MyType");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoForwardTypeDeclarationAndNoBraces
  public void testNoForwardTypeDeclarationAndNoBraces() throws Exception {
    
    
    testTypes(" function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration1
  public void testForwardTypeDeclaration1() throws Exception {
    testClosureTypes(
        
        "goog.addDependency();" +
        "goog.addDependency('y', [goog]);" +

        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return x; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration2
  public void testForwardTypeDeclaration2() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }" +
        "f(3);", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration3
  public void testForwardTypeDeclaration3() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMalformedOldTypeDef
  public void testMalformedOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        "goog.Bar = goog.typedef",
        "Typedef for goog.Bar does not have any type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMalformedOldTypeDef2
  public void testMalformedOldTypeDef2() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef",
        "Typedef for goog.Bar does not have any type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateOldTypeDef
  public void testDuplicateOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = function() {};" +
        " goog.Bar = goog.typedef",
        "variable goog.Bar redefined with type number, " +
        "original definition at [testcode]:1 " +
        "with type function (this:goog.Bar): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOldTypeDef1
  public void testOldTypeDef1() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOldTypeDef2
  public void testOldTypeDef2() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOldTypeDef3
  public void testOldTypeDef3() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " var Bar = goog.typedef;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCircularOldTypeDef
  public void testCircularOldTypeDef() throws Exception {
    testTypes(
        "var goog = {}; goog.typedef = true;" +
        " goog.Bar = goog.typedef;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateTypeDef
  public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar = function() {};" +
        " goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (this:goog.Bar): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef1
  public void testTypeDef1() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef2
  public void testTypeDef2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef3
  public void testTypeDef3() throws Exception {
    testTypes(
        "var goog = {};" +
        " var Bar;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCircularTypeDef
  public void testCircularTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent1
  public void testGetTypedPercent1() throws Exception {
    String js = "var id = function(x) { return x; }\n" +
                "var id2 = function(x) { return id(x); }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent2
  public void testGetTypedPercent2() throws Exception {
    String js = "var x = {}; x.y = 1;";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent3
  public void testGetTypedPercent3() throws Exception {
    String js = "var f = function(x) { x.a = x.b; }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent4
  public void testGetTypedPercent4() throws Exception {
    String js = "var n = {};\n  n.T = function() {};\n" +
        " var x = new n.T();";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypePropertyReference
  public void testPrototypePropertyReference() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(""
        + "\n"
        + "function Foo() {}\n"
        + "\n"
        + "Foo.prototype.bar = function(a){};\n"
        + "\n"
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

// com.google.javascript.jscomp.TypeCheckTest::testResolvingNamedTypes
  public void testResolvingNamedTypes() throws Exception {
    String js = ""
        + "\n"
        + "var Foo = function() {}\n"
        + "\n"
        + "Foo.prototype.foo = function(a) {\n"
        + "  return this.baz().toString();\n"
        + "};\n"
        + "\n"
        + "Foo.prototype.baz = function() { return new Baz(); };\n"
        + "\n"
        + "var Bar = function() {};"
        + "\n"
        + "var Baz = function() {};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty1
  public void testMissingProperty1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty2
  public void testMissingProperty2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty3
  public void testMissingProperty3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).a = 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty4
  public void testMissingProperty4() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).b = 3;",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty5
  public void testMissingProperty5() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " function Bar() { this.a = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty6
  public void testMissingProperty6() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " " +
        "function Bar() { this.a = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty7
  public void testMissingProperty7() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return obj.impossible; }",
        "Property impossible never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty8
  public void testMissingProperty8() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return typeof obj.impossible; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty9
  public void testMissingProperty9() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { if (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty10
  public void testMissingProperty10() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { while (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty11
  public void testMissingProperty11() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { for (;obj.impossible;) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty12
  public void testMissingProperty12() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { do { } while (obj.impossible); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty13
  public void testMissingProperty13() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isDef(obj.impossible); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty14
  public void testMissingProperty14() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isNull(obj.impossible); }",
        "Property isNull never defined on goog");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty15
  public void testMissingProperty15() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo) { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty16
  public void testMissingProperty16() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo(); if (x.foo) {} }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty17
  public void testMissingProperty17() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (typeof x.foo == 'function') { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty18
  public void testMissingProperty18() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo instanceof Function) { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty19
  public void testMissingProperty19() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.bar) { if (x.foo) {} } else { x.foo(); } }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty20
  public void testMissingProperty20() throws Exception {
    
    
    
    
    
    
    
    
    testTypes(
        "" +
        "function f(x) { if (x.foo) { } else { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty21
  public void testMissingProperty21() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo && x.foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty22
  public void testMissingProperty22() throws Exception {
    testTypes(
        "" +
        "function f(x) { return x.foo ? x.foo() : true; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty23
  public void testMissingProperty23() throws Exception {
    testTypes(
        "function f(x) { x.impossible(); }",
        "Property impossible never defined on x");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty24
  public void testMissingProperty24() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty25
  public void testMissingProperty25() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "Foo.prototype.bar = function() {};" +
        " var FooAlias = Foo;" +
        "(new FooAlias()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty26
  public void testMissingProperty26() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        "FooAlias.prototype.bar = function() {};" +
        "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty27
  public void testMissingProperty27() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) {" +
        "  for (var parent = x; parent; parent = parent.getParent()) {}" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty28
  public void testMissingProperty28() throws Exception {
    testTypes(
        "function f(obj) {" +
        "   obj.foo;" +
        "  return obj.foo;" +
        "}");
    testTypes(
        "function f(obj) {" +
        "   obj.foo;" +
        "  return obj.foox;" +
        "}",
        "Property foox never defined on obj");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty29
  public void testMissingProperty29() throws Exception {
    
    testTypes(
        
        " var Foo;" +
        "Foo.prototype.opera;" +
        "Foo.prototype.opera.postError;",
        "",
        null,
        false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty30
  public void testMissingProperty30() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        " return {};" +
        "}" +
        "f().a = 3;" +
        " function g(y) { return y.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty31
  public void testMissingProperty31() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        " return [];" +
        "}" +
        "f().a = 3;" +
        " function g(y) { return y.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty32
  public void testMissingProperty32() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        " return [];" +
        "}" +
        "f().a = 3;" +
        " function g(y) { return y.a; }",
        "Property a never defined on Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty33
  public void testMissingProperty33() throws Exception {
    testTypes(
      "" +
      "function f(x) { !x.foo || x.foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty34
  public void testMissingProperty34() throws Exception {
    testTypes(
        "" +
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug1
  public void testFlowScopeBug1() throws Exception {
    Node n = parseAndTypeCheck("\n"
        + "function f(a, b) {\n"
        + ""
        + "var i = 0;"
        + "for (; (i + a) < b; ++i) {}}");

    
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
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

    
    assertEquals(registry.createNullableType(registry.getType("Foo")),
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
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
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

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType1
  public void testBadTemplateType1() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y, z) {}\n" +
        "f(this, this, function() {});",
        FunctionTypeBuilder.TEMPLATE_TYPE_DUPLICATED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType2
  public void testBadTemplateType2() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType3
  public void testBadTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {}\n" +
        "f(this);",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType4
  public void testBadTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType5
  public void testBadTemplateType5() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUndefinedThisArgument
  public void testFunctionLiteralUndefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; });",
        "Function literal argument refers to undefined this argument");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument
  public void testFunctionLiteralDefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; }, {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testActiveXObject
  public void testActiveXObject() throws Exception {
    testTypes(
        " var x = new ActiveXObject();" +
        " var y = new ActiveXObject();");
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

// com.google.javascript.jscomp.TypeInferenceTest::testAssert1
  public void testAssert1() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assert(x); out2 = x;");
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

// com.google.javascript.jscomp.TypeInferenceTest::testAssertArray
  public void testAssertArray() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertArray(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof
  public void testAssertInstanceof() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertInstanceof(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", OBJECT_TYPE);
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
    verify("y", JSTypeNative.NO_OBJECT_TYPE);
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
            new TypeMismatch(firstFunction, secondFunction),
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
            new TypeMismatch(firstFunction, secondFunction),
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
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        foo.getPropertyType("bar"));
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorProperty
  public void testConstructorProperty() {
    testSame("var foo = {};  foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (this:foo.Bar): undefined", fooBar.toString());
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("Bar"));
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
    assertEquals(Sets.newHashSet(foo), registry.getTypesWithProperty("Bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredProperty
  public void testInferredProperty() {
    testSame("var foo = {}; foo.Bar = 3;");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.toString(), foo.hasProperty("Bar"));
    assertEquals("number", foo.getPropertyType("Bar").toString());
    assertTrue(foo.isPropertyTypeInferred("Bar"));
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredPrototypeProperty
  public void testInferredPrototypeProperty() {
    testSame(" var Foo = function() {};" +
        "Foo.prototype.bar = 1; var x = new Foo();");

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
    assertEquals(registry.getType("FooAlias"),
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
    assertEquals(registry.getType("goog.Foo"),
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

    assertEquals(globalScope.getVar("goog.foo").getType(),
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

    assertEquals(lastLocalScope.getVar("goog.foo").getType(),
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertyOnUnknownSuperClass
  public void testPropertyOnUnknownSuperClass() {
    testSame(
        "var goog = this.foo();" +
        "" +
        "function Foo() {}" +
        "Foo.prototype.bar = 1;" +
        "var x = new Foo();",
        RhinoErrorReporter.PARSE_ERROR);
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertEquals("?", x.getPropertyType("bar").toString());
    assertTrue(x.isPropertyTypeInferred("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction
  public void testMethodBeforeFunction() throws Exception {
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
    assertEquals(getNativeType(NUMBER_TYPE),
        instanceType.getPropertyType("m1"));
    assertEquals(getNativeType(BOOLEAN_TYPE),
        instanceType.getPropertyType("m2"));
    assertEquals(getNativeType(STRING_TYPE),
        instanceType.getPropertyType("m3"));

    
    
    
    
    
    assertFalse(instanceType.hasOwnProperty("m1"));
    assertFalse(instanceType.hasOwnProperty("m2"));
    assertFalse(instanceType.hasOwnProperty("m3"));

    ObjectType proto1 = instanceType.getImplicitPrototype();
    assertFalse(proto1.hasOwnProperty("m1"));
    assertFalse(proto1.hasOwnProperty("m2"));
    assertTrue(proto1.hasOwnProperty("m3"));

    ObjectType proto2 = proto1.getImplicitPrototype();
    assertTrue(proto2.hasOwnProperty("m1"));
    assertTrue(proto2.hasOwnProperty("m2"));
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

    assertEquals(iPrototype, globalScope.getVar("I.prototype").getType());
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
    assertEquals("function (this:Extern): ?", e.toString());

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
    assertTypeEquals("function (): number",
        externInstance.getPropertyType("one"));

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
    assertTypeEquals("number", obj.getPropertyType("one"));
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
        "function ((Object|null|undefined), (Object|null|undefined)): ?",
        f.getPropertyType("apply").toString());

    
    
    FunctionType func = (FunctionType) globalScope.getVar("Function").getType();
    assertEquals("Function",
        func.getPrototype().getPropertyType("apply").toString());
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
    assertEquals("{ foo : number }", a.toString());
    assertFalse(a.hasProperty("name"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalThis
  public void testGlobalThis() {
    testSame(
        " function Window() {}" +
        "Window.prototype.alert = function() {};" +
        "var x = this;");

    ObjectType x = (ObjectType) (globalScope.getVar("x").getType());
    FunctionType windowCtor =
        (FunctionType) (globalScope.getVar("Window").getType());
    assertEquals("global this", x.toString());
    assertTrue(x.isSubtype(windowCtor.getInstanceType()));
    assertFalse(x.equals(windowCtor.getInstanceType()));
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
    assertEquals("function (this:goog.Foo): undefined", ctor.toString());
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
    assertEquals(registry.getType("Foo"), registry.getType("FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testNamespacedConstructorAlias
  public void testNamespacedConstructorAlias() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;");
    assertEquals("goog.Foo", registry.getType("goog.FooAlias").toString());
    assertEquals(registry.getType("goog.Foo"),
        registry.getType("goog.FooAlias"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType
  public void testTemplateType() {
    testSame(
        "\n" +
        "function bind(fn, thisObj) {}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.baz = function() {};\n" +
        "bind(function() { var f = this.baz(); }, new Foo());");
    assertEquals("number", findNameType("f", lastLocalScope).toString());
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
        "foo((" +
        "function(baz) { var f = baz; }))\n");
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

// com.google.javascript.jscomp.TypedScopeCreatorTest::testActiveXObject
  public void testActiveXObject() {
    testSame(
        CompilerTypeTestCase.ACTIVE_X_OBJECT_DEF,
        "var x = new ActiveXObject();", null);
    assertEquals(
        "NoObject",
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

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testIncrement
  public void testIncrement() {
    test("x++;", "x = +x + 1;");
    test("var x = 0; ++x;", "var x = 0; x = +x + 1;");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testDecrement
  public void testDecrement() {
    test("x--;", "x = x - 1;");
    test("var x = 0; --x;", "var x = 0; x = x - 1;");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testCompoundAssignment
  public void testCompoundAssignment() {
    test("x <<= y;", "x = x << y;");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPostfixInForLoop0
  public void testPostfixInForLoop0() {
    test("for (x++;;) {}", "for (x = +x + 1;;) {}");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPostfixInForLoop1
  public void testPostfixInForLoop1() {
    try {
      testSame("for (;x++;) {}");
      fail("Should raise an exception");
    } catch (RuntimeException e) {
    }
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPostfixInForLoop2
  public void testPostfixInForLoop2() {
    test("for (;;x++) {}", "for (;;x = +x + 1) {}");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPrefixWithinLargerExpression
  public void testPrefixWithinLargerExpression() {
    test("--x + 7;", "(x = x - 1) + 7;");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPostfixInComma
  public void testPostfixInComma() {
    test("z++, z==8;", "z = +z + 1, z==8;");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPostfixUsedValue0
  public void testPostfixUsedValue0() {
    try {
      testSame("z==8, z++;");
      fail("Should raise an exception");
    } catch (RuntimeException e) {
    }
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testPostfixUsedValue1
  public void testPostfixUsedValue1() {
    try {
      testSame("x-- + 7;");
      fail("Should raise an Exception");
    } catch (RuntimeException e) {
    }
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testMultiple
  public void testMultiple() {
    test("x++, 5; for (a.x++;0;x++) {}; x++;",
        "x = +x + 1, 5; for (a.x = +a.x + 1; 0; x = +x + 1) {}; x = +x + 1;");
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testIncrementSideEffects
  public void testIncrementSideEffects() {
    try {
      
      testSame("++a[f()];");
      fail("Should raise an exception");
    } catch (RuntimeException e) {
    }
  }

// com.google.javascript.jscomp.UnfoldCompoundAssignmentsTest::testCompoundAssignmentSideEffects
  public void testCompoundAssignmentSideEffects() {
    try {
      
      testSame("a[f()] *= 2;");
      fail("Should raise an exception");
    } catch (RuntimeException e) {
    }
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    
    test("function foo(){switch(foo){case 1:x=1;return;break;" +
         "case 2:{x=2;return;break}default:}}",
         "function foo(){switch(foo){case 1:x=1;return;" +
         "case 2:{x=2}default:}}");

    
    test("function bar(){if(foo)x=1;else if(bar){return;x=2}" +
         "else{x=3;return;x=4}return 5;x=5}",
         "function bar(){if(foo)x=1;else if(bar){return}" +
         "else{x=3;return}return 5}");

    
    test("function foo(){if(x==3)return;x=4;y++;while(y==4){return;x=3}}",
         "function foo(){if(x==3)return;x=4;y++;while(y==4){return}}");

    
    test("function baz(){for(i=0;i<n;i++){x=3;break;x=4}" +
         "do{x=2;break;x=4}while(x==4);" +
         "while(i<4){x=3;return;x=6}}",
         "function baz(){for(i=0;i<n;){x=3;break}" +
         "do{x=2;break}while(x==4);" +
         "while(i<4){x=3;return}}");

    
    test("function foo(){if(x==3){return}return 5;while(y==4){x++;return;x=4}}",
         "function foo(){if(x==3){return}return 5}");

    
    test("function foo(){return 3;for(;y==4;){x++;return;x=4}}",
         "function foo(){return 3}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=4;return 5;x=5}}",
         "function foo(){try{x=3;return x+1}catch(e){x=4;return 5}}");

    
    test("function foo(){try{x=3;return x+1;x=5}finally{x=4;return 5;x=5}}",
         "function foo(){try{x=3;return x+1}finally{x=4;return 5}}");

    
    test("function foo(){try{x=3;return x+1;x=5}catch(e){x=3;return;x=2}" +
         "finally{x=4;return 5;x=5}}",

         "function foo(){try{x=3;return x+1}catch(e){x=3;return}" +
         "finally{x=4;return 5}}");

    
    test("function foo(){x=3;if(x==4){x=5;return;x=6}else{x=7}return 5;x=3}",
         "function foo(){x=3;if(x==4){x=5;return}else{x=7}return 5}");

    
    test("function foo() { return 1; var x = 2; var y = 10; return 2;}",
         "function foo() { var y; var x; return 1}");

    test("function foo() { return 1; x = 2; y = 10; return 2;}",
         "function foo(){ return 1}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessNameStatements
  public void testRemoveUselessNameStatements() {
    test("a;", "");
    test("a.b;", "");
    test("a.b.MyClass.prototype.memberName;", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessStrings
  public void testRemoveUselessStrings() {
    test("'a';", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testNoRemoveUseStrict
  public void testNoRemoveUseStrict() {
    test("'use strict';", "'use strict'");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testNoRemoveUselessNameStatements
  public void testNoRemoveUselessNameStatements() {
    removeNoOpStatements = false;
    testSame("a;");
    testSame("a.b;");
    testSame("a.b.MyClass.prototype.memberName;");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveDo
  public void testRemoveDo() {
    test("do { print(1); break } while(1)", "do { print(1); break } while(1)");
    test("while(1) { break; do { print(1); break } while(1) }",
         "while(1) { break; do {} while(1) }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemoveUselessLiteralValueStatements
  public void testRemoveUselessLiteralValueStatements() {
    test("true;", "");
    test("'hi';", "");
    test("if (x) 1;", "");
    test("while (x) 1;", "while (x);");
    test("do 1; while (x);", "do ; while (x);");
    test("for (;;) 1;", "for (;;);");
    test("switch(x){case 1:true;case 2:'hi';default:true}",
         "switch(x){case 1:case 2:default:}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testConditionalDeadCode
  public void testConditionalDeadCode() {
    test("function f() { if (1) return 5; else return 5; x = 1}",
        "function f() { if (1) return 5; else return 5; }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testSwitchCase
  public void testSwitchCase() {
    test("function f() { switch(x) { default: return 5; foo()}}",
         "function f() { switch(x) { default: return 5;}}");
    test("function f() { switch(x) { default: return; case 1: foo(); bar()}}",
         "function f() { switch(x) { default: return; case 1: foo(); bar()}}");
    test("function f() { switch(x) { default: return; case 1: return 5;bar()}}",
         "function f() { switch(x) { default: return; case 1: return 5;}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testTryCatchFinally
  public void testTryCatchFinally() {
    testSame("try {foo()} catch (e) {bar()}");
    testSame("try { try {foo()} catch (e) {bar()}} catch (x) {bar()}");
    test("try {var x = 1} catch (e) {e()}", "{var x = 1}");
    test("try {var x = 1} catch (e) {e()} finally {x()}",
        " try {var x = 1}                 finally {x()}");
    test("try {var x = 1} catch (e) {e()} finally {}",
        "     {var x = 1}");
    testSame("try {var x = 1} finally {x()}");
    test("try {var x = 1} finally {}", "{var x = 1}");
    test("function f() { return; try{var x = 1}catch(e){} }",
        "function f() { var x; return; {}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testRemovalRequiresRedeclaration
  public void testRemovalRequiresRedeclaration() {
    test("while(1) { break; var x = 1}", "var x; while(1) { break } ");
    test("while(1) { break; var x=1; var y=1}",
        "var y; var x; while(1) { break } ");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testAssignPropertyOnCreatedObject
  public void testAssignPropertyOnCreatedObject() {
    testSame("this.foo = 3;");
    testSame("a.foo = 3;");
    testSame("bar().foo = 3;");
    testSame("({}).foo = bar();");
    testSame("(new X()).foo = 3;");

    test("({}).foo = 3;", "");
    test("(function() {}).prototype.toString = function(){};", "");
    test("(function() {}).prototype['toString'] = function(){};", "");
    test("(function() {}).prototype[f] = function(){};", "");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testUnlessUnconditionalReturn
  public void testUnlessUnconditionalReturn() {
    test("function foo() { return }", " function foo() { }");
    test("function foo() { return; return; x=1 }", "function foo() { }");
    test("function foo() { return; return; var x=1}", "function foo() {var x}");
    test("function foo() { return; function bar() {} }",
         "function foo() {         function bar() {} }" );
    testSame("function foo() { return 5 }");

    test("function() {switch (a) { case 'a': return}}",
         "function() {switch (a) { case 'a': }}");
    testSame("function() {switch (a) { case 'a': case foo(): }}");
    testSame("function() {switch (a) { default: return; case 'a': alert(1)}}");
    testSame("function() {switch (a) { case 'a': return; default: alert(1)}}");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testUnlessUnconditionalContinue
  public void testUnlessUnconditionalContinue() {
    test("for(;1;) {continue}", " for(;1;) {}");
    test("for(;0;) {continue}", " for(;0;) {}");

    testSame("X: for(;1;) { for(;1;) { if (x()) {continue X} x = 1}}");
    test("for(;1;) { X: for(;1;) { if (x()) {continue X} }}",
         "for(;1;) { X: for(;1;) { if (x()) {}}}");

    test("do { continue } while(1);", "do {  } while(1);");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testUnlessUnconditonalBreak
  public void testUnlessUnconditonalBreak() {
    test("switch (a) { case 'a': break }", "switch (a) { case 'a': }");
    test("switch (a) { case 'a': break; case foo(): }",
         "switch (a) { case 'a':        case foo(): }");
    test("switch (a) { default: break; case 'a': }",
         "switch (a) { default:        case 'a': }");

    testSame("switch (a) { case 'a': alert(a); break; default: alert(a); }");
    testSame("switch (a) { default: alert(a); break; case 'a': alert(a); }");

    test("X: {switch (a) { case 'a': break X}}",
         "X: {switch (a) { case 'a': }}");

    testSame("X: {switch (a) { case 'a': if (a()) {break X}  a = 1}}");
    test("X: {switch (a) { case 'a': if (a()) {break X}}}",
         "X: {switch (a) { case 'a': if (a()) {}}}");

    test("X: {switch (a) { case 'a': if (a()) {break X}}}",
         "X: {switch (a) { case 'a': if (a()) {}}}");

    testSame("do { break } while(1);");
    testSame("for(;1;) { break }");
  }

// com.google.javascript.jscomp.UnreachableCodeEliminationTest::testCascadedRemovalOfUnlessUnconditonalJumps
  public void testCascadedRemovalOfUnlessUnconditonalJumps() {
    test("switch (a) { case 'a': break; case 'b': break; case 'c': break }",
         "switch (a) { case 'a': case 'b': case 'c': }");
    test("function foo() {" +
         "  switch (a) { case 'a':return; case 'b':return; case 'c':return }}",
         "function foo() { switch (a) { case 'a': case 'b': case 'c': }}");
    testSame("function foo() {" +
             "switch (a) { case 'a':return 2; case 'b':return 1}}");
  }

// com.google.javascript.jscomp.VarCheckTest::testBreak
  public void testBreak() {
    testSame("a: while(1) break a;");
  }

// com.google.javascript.jscomp.VarCheckTest::testContinue
  public void testContinue() {
    testSame("a: while(1) continue a;");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarNotDefined
  public void testReferencedVarNotDefined() {
    test("x = 0;", null, VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarDefined1
  public void testReferencedVarDefined1() {
    testSame("var x, y; x=1;");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarDefined2
  public void testReferencedVarDefined2() {
    testSame("var x; function y() {x=1;}");
  }

// com.google.javascript.jscomp.VarCheckTest::testReferencedVarsExternallyDefined
  public void testReferencedVarsExternallyDefined() {
    testSame("var x = window; alert(x);");
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars1
  public void testMultiplyDeclaredVars1() {
    test("var x = 1; var x = 2;", null,
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars2
  public void testMultiplyDeclaredVars2() {
    test("var y; try { y=1 } catch (x) {}" +
         "try { y=1 } catch (x) {}",
         "var y;try{y=1}catch(x){}try{y=1}catch(x){}");
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars3
  public void testMultiplyDeclaredVars3() {
    test("try { var x = 1; x *=2; } catch (x) {}", null,
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testMultiplyDeclaredVars4
  public void testMultiplyDeclaredVars4() {
    testSame("x;", "var x = 1; var x = 2;",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR, true);
  }

// com.google.javascript.jscomp.VarCheckTest::testVarReferenceInExterns
  public void testVarReferenceInExterns() {
    testSame("asdf;", "var asdf;",
        VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testCallInExterns
  public void testCallInExterns() {
    testSame("yz();", "function yz() {}",
        VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns1
  public void testPropReferenceInExterns1() {
    testSame("asdf.foo;", "var asdf;",
        VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testPropReferenceInExterns2
  public void testPropReferenceInExterns2() {
    testSame("asdf.foo;", "",
        VarCheck.UNDEFINED_VAR_ERROR, true);
  }
