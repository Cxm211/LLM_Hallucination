// buggy code
  protected void declareNameInScope(FlowScope scope, Node node, JSType type) {
    switch (node.getType()) {
      case Token.NAME:
        scope.inferSlotType(node.getString(), type);
        break;

      case Token.GETPROP:
        String qualifiedName = node.getQualifiedName();
        Preconditions.checkNotNull(qualifiedName);

        JSType origType = node.getJSType();
        origType = origType == null ? getNativeType(UNKNOWN_TYPE) : origType;
        scope.inferQualifiedSlot(node, qualifiedName, origType, type);
        break;

        // "this" references aren't currently modeled in the CFG.

      default:
        throw new IllegalArgumentException("Node cannot be refined. \n" +
            node.toStringTree());
    }
  }

// relevant test
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
        "condition always evaluates to false\n" +
        "left : (null|string)\n" +
        "right: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithNoEntry1
  public void testTypeInferenceWithNoEntry1() throws Exception {
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        "Foo.prototype.init = function() {" +
        "   this.bar = {baz: 3};" +
        "};" +
        "" +
        "function SubFoo() {}" +
        "" +
        "SubFoo.prototype.method = function() {" +
        "  for (var i = 0; i < 10; i++) {" +
        "    f(this.bar);" +
        "    f(this.bar.baz);" +
        "  }" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (null|{baz: number})\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithNoEntry2
  public void testTypeInferenceWithNoEntry2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " function f(x) {}" +
        " function g(x) {}" +
        " function Foo() {}" +
        "Foo.prototype.init = function() {" +
        "   this.bar = {baz: 3};" +
        "};" +
        "" +
        "function SubFoo() {}" +
        "" +
        "SubFoo.prototype.method = function() {" +
        "  for (var i = 0; i < 10; i++) {" +
        "    f(this.bar);" +
        "    goog.asserts.assert(this.bar);" +
        "    g(this.bar);" +
        "  }" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (null|{baz: number})\n" +
        "required: number");
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
        "Bad type annotation. Unknown type MyType");
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
        "function f(x) { return 3; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration2
  public void testForwardTypeDeclaration2() throws Exception {
    String f = "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }";
    testClosureTypes(f, null);
    testClosureTypes(f + "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null)");
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

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration4
  public void testForwardTypeDeclaration4() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(new MyType());",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration5
  public void testForwardTypeDeclaration5() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " var YourType = function() {};" +
        " YourType.prototype.method = function() {};",
        "Could not resolve type in @extends tag of YourType");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration6
  public void testForwardTypeDeclaration6() throws Exception {
    testClosureTypesMultipleWarnings(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " var YourType = function() {};" +
        " YourType.prototype.method = function() {};",
        Lists.newArrayList(
            "Could not resolve type in @implements tag of YourType",
            "property method not defined on any superclass of YourType"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration7
  public void testForwardTypeDeclaration7() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return x == undefined; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration8
  public void testForwardTypeDeclaration8() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return x.name == undefined; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration9
  public void testForwardTypeDeclaration9() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { x.name = 'Bob'; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration10
  public void testForwardTypeDeclaration10() throws Exception {
    String f = "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }";
    testClosureTypes(f, null);
    testClosureTypes(f + "f(3);", null);
    testClosureTypes(f + "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: (MyType|null|number)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateTypeDef
  public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar = function() {};" +
        " goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (new:goog.Bar): undefined");
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

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef4
  public void testTypeDef4() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " var AB;" +
        " function f(x) {}" +
        "f(new A()); f(new B()); f(1);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (A|B|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef5
  public void testTypeDef5() throws Exception {
    
    
    
    
    testTypes(
        " function f(x) {}" +
        " function A() {}" +
        " function B() {}" +
        " var AB;" +
        "f(new A()); f(new B()); f(1);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (A|B|null)");
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

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent5
  public void testGetTypedPercent5() throws Exception {
    String js = " keys = {A: 1,B: 2,C: 3};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent6
  public void testGetTypedPercent6() throws Exception {
    String js = "a = {TRUE: 1, FALSE: 0};";
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

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty35
  public void testMissingProperty35() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " function Baz() {}" +
        " function f(x) { x.specialProp = 1; }" +
        " function g(x) { return x.specialProp; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty36
  public void testMissingProperty36() throws Exception {
    
    
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.baz = 0;" +
        " function SubFoo() {}" +
        "SubFoo.prototype.bar = 0;" +
        " function f(x) { return x.baz; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty37
  public void testMissingProperty37() throws Exception {
    
    
    testTypes(
        " function f(x){" +
        "  x.isVisible = false;" +
        "}" +
        " function Foo() {}" +
        " function SubFoo() {}" +
        " SubFoo.prototype.isVisible = true;" +
        "\n" +
        "function g(x) { return x.isVisible; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty38
  public void testMissingProperty38() throws Exception {
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " function f() { return new Foo(); }" +
        "f().missing;",
        "Property missing never defined on (Bar|Foo|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty39
  public void testMissingProperty39() throws Exception {
    testTypes(
        " function f() { return 3; }" +
        "f().length;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty40
  public void testMissingProperty40() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty41
  public void testMissingProperty41() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.impossible) x.impossible(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReflectObject1
  public void testReflectObject1() throws Exception {
    testClosureTypes(
        "var goog = {}; goog.reflect = {}; " +
        "goog.reflect.object = function(x, y){};" +
        " function A() {}" +
        "goog.reflect.object(A, {x: 3});",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testReflectObject2
  public void testReflectObject2() throws Exception {
    testClosureTypes(
        "var goog = {}; goog.reflect = {}; " +
        "goog.reflect.object = function(x, y){};" +
        " function f(x) {}" +
        " function A() {}" +
        "goog.reflect.object(A, {x: f(1 + 1)});",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends1
  public void testLends1() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        "Bad type annotation. missing object name in @lends tag");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends2
  public void testLends2() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        "Variable Foob not declared before @lends annotation.");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends3
  public void testLends3() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo, {bar: 1});" +
        "alert(Foo.bar);",
        "Property bar never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends4
  public void testLends4() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));" +
        "alert(Foo.bar);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends5
  public void testLends5() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo, {bar: 1});" +
        "alert((new Foo()).bar);",
        "Property bar never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends6
  public void testLends6() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));" +
        "alert((new Foo()).bar);");
  }

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
        "found   : Object\n" +
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
        "Int2 cannot extend this type; a constructor can only extend " +
        "objects and an interface can only extend interfaces");
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
    String FN_DECL = " \n" +
        "function f(x,y) { return y(x); }\n";

    testTypes(
        FN_DECL +
        "" +
        "var out;" +
        "" +
        "var result = f('hi', function(x){ out = x; return x; });");

    testTypes(
        FN_DECL +
        "" +
        "var out;" +
        "var result = f(0, function(x){ out = x; return x; });",
        "assignment\n" +
        "found   : number\n" +
        "required: string");

    testTypes(
        FN_DECL +
        "var out;" +
        "" +
        "var result = f(0, function(x){ out = x; return x; });",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
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
    ObjectType thisType = registry.createAnonymousObjectType();
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; if (this.foo) { y = this.foo; }");
    verify("y", createUnionType(NUMBER_TYPE, STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference2
  public void testPropertyInference2() {
    ObjectType thisType = registry.createAnonymousObjectType();
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; this.foo = 'x'; y = this.foo;");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference3
  public void testPropertyInference3() {
    ObjectType thisType = registry.createAnonymousObjectType();
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

// com.google.javascript.jscomp.TypeInferenceTest::testAssertWithIsDef
  public void testAssertWithIsDef() {
    JSType startType = createNullableType(NUMBER_TYPE);
    assuming("x", startType);
    inFunction(
        "out1 = x;" +
        "goog.asserts.assert(goog.isDefAndNotNull(x));" +
        "out2 = x;");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
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
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        foo.getPropertyType("bar"));
    assertEquals(Lists.newArrayList(foo), registry.getTypesWithProperty("bar"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testConstructorProperty
  public void testConstructorProperty() {
    testSame("var foo = {};  foo.Bar = function() {};");
    ObjectType foo = (ObjectType) findNameType("foo", globalScope);
    assertTrue(foo.hasProperty("Bar"));
    assertFalse(foo.isPropertyTypeInferred("Bar"));

    JSType fooBar = foo.getPropertyType("Bar");
    assertEquals("function (new:foo.Bar): undefined", fooBar.toString());
    assertEquals(Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
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
    assertEquals(Lists.newArrayList(foo), registry.getTypesWithProperty("Bar"));
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

    assertEquals(iPrototype, globalScope.getVar("I.prototype").getType());
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
