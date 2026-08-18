// buggy code
  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    return result;
  }

// relevant test
// com.google.javascript.jscomp.OptimizeParametersTest::testDifferentScopes
  public void testDifferentScopes() {
    test("function f(a, b) {} f(1, 2); f(1, 3); " +
        "function h() {function g(a) {} g(4); g(5);} f(1, 2);",
        "function f(b) {var a = 1} f(2); f(3); " +
        "function h() {function g(a) {} g(4); g(5);} f(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptimizeOnlyImmutableValues
  public void testOptimizeOnlyImmutableValues() {
    test("function foo(a) {}; foo(undefined);",
         "function foo() {var a = undefined}; foo()");
    test("function foo(a) {}; foo(null);",
        "function foo() {var a = null}; foo()");
    test("function foo(a) {}; foo(1);",
         "function foo() {var a = 1}; foo()");
    test("function foo(a) {}; foo('abc');",
        "function foo() {var a = 'abc'}; foo()");

    test("var foo = function(a) {}; foo(undefined);",
         "var foo = function() {var a = undefined}; foo()");
    test("var foo = function(a) {}; foo(null);",
         "var foo = function() {var a = null}; foo()");
    test("var foo = function(a) {}; foo(1);",
         "var foo = function() {var a = 1}; foo()");
    test("var foo = function(a) {}; foo('abc');",
         "var foo = function() {var a = 'abc'}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoOptimizeCall
  public void testDoOptimizeCall() {
    testSame("var foo = function () {}; foo(); foo.call();");
    
    testSame("var foo = function () {}; foo(); foo.call(this);");
    testSame("var foo = function (a, b) {}; foo(1); foo.call(this, 1);");
    testSame("var foo = function () {}; foo(); foo.call(null);");
    testSame("var foo = function (a, b) {}; foo(1); foo.call(null, 1);");

    testSame("var foo = function () {}; foo.call();");
    
    testSame("var foo = function () {}; foo.call(this);");
    testSame("var foo = function (a, b) {}; foo.call(this, 1);");
    testSame("var foo = function () {}; foo.call(null);");
    testSame("var foo = function (a, b) {}; foo.call(null, 1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoOptimizeApply
  public void testDoOptimizeApply() {
    testSame("var foo = function () {}; foo(); foo.apply();");
    testSame("var foo = function () {}; foo(); foo.apply(this);");
    testSame("var foo = function (a, b) {}; foo(1); foo.apply(this, 1);");
    testSame("var foo = function () {}; foo(); foo.apply(null);");
    testSame("var foo = function (a, b) {}; foo(1); foo.apply(null, []);");

    testSame("var foo = function () {}; foo.apply();");
    testSame("var foo = function () {}; foo.apply(this);");
    testSame("var foo = function (a, b) {}; foo.apply(this, 1);");
    testSame("var foo = function () {}; foo.apply(null);");
    testSame("var foo = function (a, b) {}; foo.apply(null, []);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    
    
    testSame("var foo; foo = function (p1) { }; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalOneRequired
  public void testRemoveOneOptionalOneRequired() {
    test("function foo(p1, p2) { } foo(1); foo(2)",
        "function foo(p1) {var p2} foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultipleCalls
  public void testRemoveOneOptionalMultipleCalls() {
    test( "function foo(p1, p2) { } foo(1); foo(2); foo()",
        "function foo(p1) {var p2} foo(1); foo(2); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultiplePossibleDefinition
  public void testRemoveOneOptionalMultiplePossibleDefinition() {
    
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2) { };" +
        "goog.foo = function (q1, q2) { };" +
        "goog.foo = function (r1, r2) { };" +
        "goog.foo(1); goog.foo(2); goog.foo()";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveTwoOptionalMultiplePossibleDefinition
  public void testRemoveTwoOptionalMultiplePossibleDefinition() {
    
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2, p3, p4) { };" +
        "goog.foo = function (q1, q2, q3, q4) { };" +
        "goog.foo = function (r1, r2, r3, r4) { };" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorOptArgsNotRemoved
  public void testConstructorOptArgsNotRemoved() {
    String src =
        "" +
        "var goog = function(){};" +
        "goog.prototype.foo = function(a,b) {};" +
        "goog.prototype.bar = function(a) {};" +
        "goog.bar.inherits(goog.foo);" +
        "new goog.foo(2,3);" +
        "new goog.foo(1,2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMultipleUnknown
  public void testMultipleUnknown() {
    
    String src = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testSingleUnknown
  public void testSingleUnknown() {
    String src =
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected =
        "var goog2 = {};" +
        "goog2.foo = function () { var p1 };" +
        "var x = getGoog();" +
        "x.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveVarArg
  public void testRemoveVarArg() {
    test("function foo(p1, var_args) { } foo(1); foo(2)",
        "function foo(p1) { var var_args } foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize
  public void testAliasMethodsDontGetOptimize() {
    String src =
        "var foo = function(a, b) {};" +
        "var goog = {};" +
        "goog.foo = foo;" +
        "goog.prototype.bar = goog.foo;" +
        "new goog().bar(1,2);" +
        "foo(2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize2
  public void testAliasMethodsDontGetOptimize2() {
    String src =
        "var foo = function(a, b) {};" +
        "var bar = foo;" +
        "foo(1);" +
        "bar(2,3);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize3
  public void testAliasMethodsDontGetOptimize3() {
    String src =
        "var array = {};" +
        "array[0] = function(a, b) {};" +
        "var foo = array[0];" + 
        "foo(1);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize4
  public void testAliasMethodsDontGetOptimize4() {
    

    test(
      "function foo(bar) {};" +
      "baz = function(a) {};" +
      "baz(1);" +
      "foo(baz);",
      "function foo() {var bar = baz};" +
      "baz = function(a) {};" +
      "baz(1);" +
      "foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMethodsDefinedInArraysDontGetOptimized
  public void testMethodsDefinedInArraysDontGetOptimized() {
    String src =
        "var array = [true, function (a) {}];" +
        "array[1](1)";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMethodsDefinedInObjectDontGetOptimized
  public void testMethodsDefinedInObjectDontGetOptimized() {
    String src =
      "var object = { foo: function bar() {} };" +
      "object.foo(1)";
    testSame(src);
    src =
      "var object = { foo: function bar() {} };" +
      "object['foo'](1)";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveConstantArgument
  public void testRemoveConstantArgument() {
    
    test("function foo(p1, p2) {}; foo(1,2); foo(2,2);",
         "function foo(p1) {var p2 = 2}; foo(1); foo(2)");

    
    testSame("function foo(p1, p2) {}; foo(1); foo(2,3);");

    
    test("function foo(a,b,c){}; foo(1, 2, 3); foo(1, 2, 4); foo(2, 2, 3)",
         "function foo(a,c){var b=2}; foo(1, 3); foo(1, 4); foo(2, 3)");

    
    test("function foo(a) {}; foo(1); foo(1.0);",
         "function foo() {var a = 1;}; foo(); foo();");

    
    String src =
        "" +
        "function Person(){}; Person.prototype.run = function(a, b) {};" +
        "Person.run(1, 'a'); Person.run(2, 'a')";
    String expected =
        "function Person(){}; Person.prototype.run = " +
        "function(a) {var b = 'a'};" +
        "Person.run(1); Person.run(2)";
    test(src, expected);

  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCanDeleteArgumentsAtAnyPosition
  public void testCanDeleteArgumentsAtAnyPosition() {
    
    String src =
        "function foo(a,b,c,d,e) {};" +
        "foo(1,2,3,4,5);" +
        "foo(2,2,4,4,5);";
    String expected =
        "function foo(a,c) {var b=2; var d=4; var e=5;};" +
        "foo(1,3);" +
        "foo(2,4);";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForExternsFunctions
  public void testNoOptimizationForExternsFunctions() {
    testSame("function _foo(x, y, z){}; _foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForGoogExportSymbol
  public void testNoOptimizationForGoogExportSymbol() {
    testSame("goog.exportSymbol('foo', foo);" +
             "function foo(x, y, z){}; foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoArgumentRemovalNonEqualNodes
  public void testNoArgumentRemovalNonEqualNodes() {
    testSame("function foo(a){}; foo('bar'); foo('baz');");
    testSame("function foo(a){}; foo(1.0); foo(2.0);");
    testSame("function foo(a){}; foo(true); foo(false);");
    testSame("var a = 1, b = 2; function foo(a){}; foo(a); foo(b);");
    testSame("function foo(a){}; foo(/&/g); foo(/</g);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionPassedAsParam
  public void testFunctionPassedAsParam() {
    String src =
        " function person(){}; " +
        "person.prototype.run = function(a, b) {};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk, 0.1)};" +
        "person.foo();";
    String expected =
        "function person(){}; person.prototype.run = function(a) {" +
        "  var b = 0.1;};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk)};" +
        "person.foo();";

    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCallIsIgnore
  public void testCallIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.call(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testApplyIsIgnore
  public void testApplyIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.apply(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithReferenceToArgumentsShouldNotBeOptimize
  public void testFunctionWithReferenceToArgumentsShouldNotBeOptimize() {
    testSame("function foo(a,b,c) { return arguments.size; };" +
             "foo(1);");
    testSame("var foo = function(a,b,c) { return arguments.size }; foo(1);");
    testSame("var foo = function bar(a,b,c) { return arguments.size }; " +
             "foo(2); bar(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithTwoNames
  public void testFunctionWithTwoNames() {
    testSame("var foo = function bar(a,b) {};");
    testSame("var foo = function bar(a,b) {}; foo(1)");
    testSame("var foo = function bar(a,b) {}; bar(1);");
    testSame("var foo = function bar(a,b) {}; foo(1); foo(2)");
    testSame("var foo = function bar(a,b) {}; foo(1); bar(1)");
    testSame("var foo = function bar(a,b) {}; foo(1); bar(2)");
    testSame("var foo = function bar(a,b) {}; foo(1,2); bar(2,1)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRecursion
  public void testRecursion() {
    test("var foo = function (a,b) {foo(1, b)}; foo(1, 2)",
         "var foo = function (b) {var a=1; foo(b)}; foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstantArgumentsToConstructorCanBeOptimized
  public void testConstantArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptionalArgumentsToConstructorCanBeOptimized
  public void testOptionalArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo();";
    String expected = "function foo() {var a;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRegexesCanBeInlined
  public void testRegexesCanBeInlined() {
    test("function foo(a) {}; foo(/abc/);",
         "function foo() {var a = /abc/}; foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorUsedAsFunctionCanBeOptimized
  public void testConstructorUsedAsFunctionCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);" +
        "foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();" +
        "foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeConstructorWhenArgumentsAreNotEqual
  public void testDoNotOptimizeConstructorWhenArgumentsAreNotEqual() {
    testSame("function Foo(a) {};" +
        "var bar = new Foo(1);" +
        "var baz = new Foo(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeArrayElements
  public void testDoNotOptimizeArrayElements() {
    testSame("var array = [function (a, b) {}];");
    testSame("var array = [function f(a, b) {}]");

    testSame("var array = [function (a, b) {}];" +
        "array[0](1, 2);" +
        "array[0](1);");

    testSame("var array = [];" +
        "function foo(a, b) {};" +
        "array[0] = foo;");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptimizeThis
  public void testOptimizeThis() {
    String src = "function foo() {" +
        "var bar = function (a, b) {};" +
        "this.bar = function (a, b) {};" +
        "this.bar(3);" +
        "bar(2);}";
    String expected = "function foo() {" +
        "var bar = function () {var b; var a = 2;};" +
        "this.bar = function () {var b; var a = 3;};" +
        "this.bar();" +
        "bar();}";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeWhenArgumentsPassedAsParameter
  public void testDoNotOptimizeWhenArgumentsPassedAsParameter() {
    testSame("function foo(a) {}; foo(arguments)");
    testSame("function foo(a) {}; foo(arguments[0])");

    test("function foo(a, b) {}; foo(arguments, 1)",
         "function foo(a) {var b = 1}; foo(arguments)");

    test("function foo(a, b) {}; foo(arguments)",
         "function foo(a) {var b}; foo(arguments)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeGoogExportFunctions
  public void testDoNotOptimizeGoogExportFunctions() {
    testSame("function foo(a, b) {}; foo(); goog.export_function(foo);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeJSCompiler_renameProperty
  public void testDoNotOptimizeJSCompiler_renameProperty() {
    testSame("function JSCompiler_renameProperty(a) {return a};" +
             "JSCompiler_renameProperty('a');");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeJSCompiler_ObjectPropertyString
  public void testDoNotOptimizeJSCompiler_ObjectPropertyString() {
    testSame("function JSCompiler_ObjectPropertyString(a, b) {return a[b]};" +
             "JSCompiler_renameProperty(window,'b');");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues1
  public void testMutableValues1() {
    test("function foo(p1) {} foo()",
         "function foo() {var p1} foo()");
    test("function foo(p1) {} foo(1)",
         "function foo() {var p1=1} foo()");
    test("function foo(p1) {} foo([])",
         "function foo() {var p1=[]} foo()");
    test("function foo(p1) {} foo({})",
         "function foo() {var p1={}} foo()");
    test("var x;function foo(p1) {} foo(x)",
         "var x;function foo() {var p1=x} foo()");
    test("var x;function foo(p1) {} foo(x())",
         "var x;function foo() {var p1=x()} foo()");
    test("var x;function foo(p1) {} foo(new x())",
         "var x;function foo() {var p1=new x()} foo()");
    test("var x;function foo(p1) {} foo('' + x)",
         "var x;function foo() {var p1='' + x} foo()");

    testSame("function foo(p1) {} foo(this)");
    testSame("function foo(p1) {} foo(arguments)");
    testSame("function foo(p1) {} foo(function(){})");
    testSame("function foo(p1) {} (function () {var x;foo(x)})()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues2
  public void testMutableValues2() {
    test("function foo(p1, p2) {} foo(1, 2)",
         "function foo() {var p1=1; var p2 = 2} foo()");
    test("var x; var y; function foo(p1, p2) {} foo(x(), y())",
         "var x; var y; function foo() {var p1=x(); var p2 = y()} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues3
  public void testMutableValues3() {
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "foo(x(), y()); foo(x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=x(); var p2=y()}" +
        "foo(); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues4
  public void testMutableValues4() {
    
    
    
    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo(x(), y(), z()); foo(x(),y(),3)");

    
    
    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo(x, y(), z()); foo(x,y(),3)");

    
    
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo([], y(), z()); foo([],y(),3)",
        "var x; var y; var z;" +
        "function foo(p2, p3) {var p1=[]}" +
        "foo(y(), z()); foo(y(),3)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues5
  public void testMutableValues5() {
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "new foo(new x(), y()); new foo(new x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=new x(); var p2=y()}" +
        "new foo(); new foo()");

    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "new foo(x(), y()); new foo(x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=x(); var p2=y()}" +
        "new foo(); new foo()");

    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo(x(), y(), z()); new foo(x(),y(),3)");

    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo(x, y(), z()); new foo(x,y(),3)");

    test(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo([], y(), z()); new foo([],y(),3)",
        "var x; var y; var z;" +
        "function foo(p2, p3) {var p1=[]}" +
        "new foo(y(), z()); new foo(y(),3)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testShadows
  public void testShadows() {
    testSame("function foo(a) {}" +
             "var x;" +
             "function f() {" +
             "  var x;" +
             "  function g() {" +
             "    foo(x());" +
             "  }" +
             "};" +
             "foo(x())");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCrash
  public void testCrash() {
    test(
        "function foo(a) {}" +
        "foo({o:1});" +
        "foo({o:1})",
        "function foo() {var a = {o:1}}" +
        "foo();" +
        "foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testGlobalCatch
  public void testGlobalCatch() {
    testSame("function foo(a) {} try {} catch (e) {foo(e)}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNamelessParameter1
  public void testNamelessParameter1() {
    test("f(g()); function f(){}",
         "f(); function f(){g()}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNamelessParameter2
  public void testNamelessParameter2() {
    test("f(g(),h()); function f(){}",
         "f(); function f(){g();h()}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldBlock
  public void testFoldBlock() {
    fold("{{foo()}}", "foo()");
    fold("{foo();{}}", "foo()");
    fold("{{foo()}{}}", "foo()");
    fold("{{foo()}{bar()}}", "foo();bar()");
    fold("{if(false)foo(); {bar()}}", "bar()");
    fold("{if(false)if(false)if(false)foo(); {bar()}}", "bar()");

    fold("{'hi'}", "");
    fold("{x==3}", "");
    fold("{ (function(){x++}) }", "");
    fold("function f(){return;}", "function f(){return;}");
    fold("function f(){return 3;}", "function f(){return 3}");
    fold("function f(){if(x)return; x=3; return; }",
         "function f(){if(x)return; x=3; return; }");
    fold("{x=3;;;y=2;;;}", "x=3;y=2");

    
    fold("while(x()){x}", "while(x());");
    fold("while(x()){x()}", "while(x())x()");
    fold("for(x=0;x<100;x++){x}", "for(x=0;x<100;x++);");
    fold("for(x in y){x}", "for(x in y);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldBlocksWithManyChildren
  public void testFoldBlocksWithManyChildren() {
    fold("function f() { if (false) {} }", "function f(){}");
    fold("function f() { { if (false) {} if (true) {} {} } }",
         "function f(){}");
    fold("{var x; var y; var z; function f() { { var a; { var b; } } } }",
         "var x;var y;var z;function f(){var a;var b}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testIf
  public void testIf() {
    fold("if (1){ x=1; } else { x = 2;}", "x=1");
    fold("if (false){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void 0){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void foo()){ x = 1; } else { x = 2; }",
         "foo();x=2");
    fold("if (false){ x = 1; } else if (true) { x = 3; } else { x = 2; }",
         "x=3");
    fold("if (x){ x = 1; } else if (false) { x = 3; }",
         "if(x)x=1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook
  public void testHook() {
    fold("true ? a() : b()", "a()");
    fold("false ? a() : b()", "b()");

    fold("a() ? b() : true", "a() && b()");
    fold("a() ? true : b()", "a() || b()");

    fold("(a = true) ? b() : c()", "a = true, b()");
    fold("(a = false) ? b() : c()", "a = false, c()");
    fold("do {f()} while((a = true) ? b() : c())",
         "do {f()} while((a = true) , b())");
    fold("do {f()} while((a = false) ? b() : c())",
         "do {f()} while((a = false) , c())");

    fold("var x = (true) ? 1 : 0", "var x=1");
    fold("var y = (true) ? ((false) ? 12 : (cond ? 1 : 2)) : 13",
         "var y=cond?1:2");

    foldSame("var z=x?void 0:y()");
    foldSame("z=x?void 0:y()");
    foldSame("z*=x?void 0:y()");

    foldSame("var z=x?y():void 0");
    foldSame("(w?x:void 0).y=z");
    foldSame("(w?x:void 0).y+=z");

    fold("y = (x ? void 0 : void 0)", "y = void 0");
    fold("y = (x ? f() : f())", "y = f()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testConstantConditionWithSideEffect1
  public void testConstantConditionWithSideEffect1() {
    fold("if (b=true) x=1;", "b=true;x=1");
    fold("if (b=/ab/) x=1;", "b=/ab/;x=1");
    fold("if (b=/ab/){ x=1; } else { x=2; }", "b=/ab/;x=1");
    fold("var b;b=/ab/;if(b)x=1;", "var b;b=/ab/;x=1");
    foldSame("var b;b=f();if(b)x=1;");
    fold("var b=/ab/;if(b)x=1;", "var b=/ab/;x=1");
    foldSame("var b=f();if(b)x=1;");
    foldSame("b=b++;if(b)x=b;");
    fold("(b=0,b=1);if(b)x=b;", "b=0,b=1;if(b)x=b;");
    fold("b=1;if(foo,b)x=b;","b=1;x=b;");
    foldSame("b=1;if(foo=1,b)x=b;");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testConstantConditionWithSideEffect2
  public void testConstantConditionWithSideEffect2() {
    fold("(b=true)?x=1:x=2;", "b=true,x=1");
    fold("(b=false)?x=1:x=2;", "b=false,x=2");
    fold("if (b=/ab/) x=1;", "b=/ab/;x=1");
    fold("var b;b=/ab/;(b)?x=1:x=2;", "var b;b=/ab/;x=1");
    foldSame("var b;b=f();(b)?x=1:x=2;");
    fold("var b=/ab/;(b)?x=1:x=2;", "var b=/ab/;x=1");
    foldSame("var b=f();(b)?x=1:x=2;");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testVarLifting
  public void testVarLifting() {
    fold("if(true)var a", "var a");
    fold("if(false)var a", "var a");

    
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldUselessWhile
  public void testFoldUselessWhile() {
    fold("while(false) { foo() }", "");

    fold("while(void 0) { foo() }", "");
    fold("while(undefined) { foo() }", "");

    foldSame("while(true) foo()");

    fold("while(false) { var a = 0; }", "var a");

    
    fold("while(false) { foo(); continue }", "");

    fold("while(0) { foo() }", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldUselessFor
  public void testFoldUselessFor() {
    fold("for(;false;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;true;) foo() ", "for(;;) foo() ");
    foldSame("for(;;) foo()");
    fold("for(;false;) { var a = 0; }", "var a");

    
    fold("for(;false;) { foo(); continue }", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldUselessDo
  public void testFoldUselessDo() {
    fold("do { foo() } while(false);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(true);", "do { foo() } while(true);");
    fold("do { var a = 0; } while(false);", "var a=0");

    fold("do { var a = 0; } while(!{a:foo()});", "var a=0;foo()");

    
    foldSame("do { foo(); continue; } while(0)");
    foldSame("do { foo(); break; } while(0)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testMinimizeWhileConstantCondition
  public void testMinimizeWhileConstantCondition() {
    fold("while(true) foo()", "while(true) foo()");
    fold("while(0) foo()", "");
    fold("while(0.0) foo()", "");
    fold("while(NaN) foo()", "");
    fold("while(null) foo()", "");
    fold("while(undefined) foo()", "");
    fold("while('') foo()", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldConstantCommaExpressions
  public void testFoldConstantCommaExpressions() {
    fold("if (true, false) {foo()}", "");
    fold("if (false, true) {foo()}", "foo()");
    fold("true, foo()", "foo()");
    fold("(1 + 2 + ''), foo()", "foo()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveUselessOps
  public void testRemoveUselessOps() {
    
    
    
    
    

    
    fold("Math.random()", "");
    fold("Math.random(f() + g())", "f(),g();");
    fold("Math.random(f(),g(),h())", "f(),g(),h();");

    
    foldSame("f();");
    foldSame("(function () { f(); })();");

    
    
    fold("(function () {})();", "");

    
    fold("(function () {});", "");
    fold("(function f() {});", "");
    
    fold("(function () {foo();});", "");

    
    fold("+f()", "f()");
    fold("a=(+f(),g())", "a=(f(),g())");
    fold("a=(true,g())", "a=g()");
    fold("f(),true", "f()");
    fold("f() + g()", "f(),g()");

    fold("for(;;+f()){}", "for(;;f()){}");
    fold("for(+f();;g()){}", "for(f();;g()){}");
    fold("for(;;Math.random(f(),g(),h())){}", "for(;;f(),g(),h()){}");

    
    fold("g() && +f()", "g() && f()");
    fold("g() || +f()", "g() || f()");
    fold("x ? g() : +f()", "x ? g() : f()");

    fold("+x()", "x()");
    fold("+x() * 2", "x()");
    fold("-(+x() * 2)", "x()");
    fold("2 -(+x() * 2)", "x()");
    fold("x().foo", "x()");
    foldSame("x().foo()");

    foldSame("x++");
    foldSame("++x");
    foldSame("x--");
    foldSame("--x");
    foldSame("x = 2");
    foldSame("x *= 2");

    
    foldSame("function f() {}");
    foldSame("var x;");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testOptimizeSwitch
  public void testOptimizeSwitch() {
    fold("switch(a){}", "");
    fold("switch(foo()){}", "foo()");
    fold("switch(a){default:}", "");
    fold("switch(a){default:break;}", "");
    fold("switch(a){default:var b;break;}", "var b");
    fold("switch(a){case 1: default:}", "");
    fold("switch(a){default: case 1:}", "");
    fold("switch(a){default: break; case 1:break;}", "");
    fold("switch(a){default: var b; break; case 1: var c; break;}",
        "var c; var b;");

    
    foldSame("function f() {switch(a){default: return; case 1: break;}}");
    foldSame("function f() {switch(a){case 1: foo();}}");
    foldSame("function f() {switch(a){case 3: case 2: case 1: foo();}}");

    fold("function f() {switch(a){case 2: case 1: default: foo();}}",
         "function f() {switch(a){default: foo();}}");
    fold("switch(a){case 1: default:break; case 2: foo()}",
         "switch(a){case 2: foo()}");
    foldSame("switch(a){case 1: goo(); default:break; case 2: foo()}");

    
    foldSame("switch(a){case 1: goo(); case 2:break; case 3: foo()}");

    
    fold("switch(1){case 2: var x=0;}", "var x;");
    fold("switch ('repeated') {\n" +
        "case 'repeated':\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 'repeated':\n" +
        "  var x=0;\n" +
        "  break;\n" +
        "}",
        "var x; {foo();}");

    
    foldSame("switch(a){case 1: var c =2; break;}");
    foldSame("function f() {switch(a){case 1: return;}}");
    foldSame("x:switch(a){case 1: break x;}");

    fold("switch ('foo') {\n" +
        "case 'foo':\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 'bar':\n" +
        "  bar();\n" +
        "  break;\n" +
        "}",
        "{foo();}");
    fold("switch ('noMatch') {\n" +
        "case 'foo':\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 'bar':\n" +
        "  bar();\n" +
        "  break;\n" +
        "}",
        "");
    foldSame("switch ('fallThru') {\n" +
        "case 'fallThru':\n" +
        "  if (foo(123) > 0) {\n" +
        "    foobar(1);\n" +
        "    break;\n" +
        "  }\n" +
        "  foobar(2);\n" +
        "case 'bar':\n" +
        "  bar();\n" +
        "}");
    foldSame("switch ('fallThru') {\n" +
        "case 'fallThru':\n" +
        "  foo();\n" +
        "case 'bar':\n" +
        "  bar();\n" +
        "}");
    foldSame("switch ('hasDefaultCase') {\n" +
        "case 'foo':\n" +
        "  foo();\n" +
        "  break;\n" +
        "default:\n" +
        "  bar();\n" +
        "  break;\n" +
        "}");
    fold("switch ('repeated') {\n" +
        "case 'repeated':\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 'repeated':\n" +
        "  bar();\n" +
        "  break;\n" +
        "}",
        "{foo();}");
    fold("switch ('foo') {\n" +
        "case 'bar':\n" +
        "  bar();\n" +
        "  break;\n" +
        "case notConstant:\n" +
        "  foobar();\n" +
        "  break;\n" +
        "case 'foo':\n" +
        "  foo();\n" +
        "  break;\n" +
        "}",
        "switch ('foo') {\n" +
        "case notConstant:\n" +
        "  foobar();\n" +
        "  break;\n" +
        "case 'foo':\n" +
        "  foo();\n" +
        "  break;\n" +
        "}");
    fold("switch (1) {\n" +
        "case 1:\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 2:\n" +
        "  bar();\n" +
        "  break;\n" +
        "}",
        "{foo();}");
    fold("switch (1) {\n" +
        "case 1.1:\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 2:\n" +
        "  bar();\n" +
        "  break;\n" +
        "}",
        "");
    foldSame("switch (0) {\n" +
        "case NaN:\n" +
        "  foobar();\n" +
        "  break;\n" +
        "case -0.0:\n" +
        "  foo();\n" +
        "  break;\n" +
        "case 2:\n" +
        "  bar();\n" +
        "  break;\n" +
        "}");
    foldSame("switch ('\\v') {\n" +
        "case '\\u000B':\n" +
        "  foo();\n" +
        "}");
    foldSame("switch ('empty') {\n" +
        "case 'empty':\n" +
        "case 'foo':\n" +
        "  foo();\n" +
        "}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveNumber
  public void testRemoveNumber() {
    test("3", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveVarGet1
  public void testRemoveVarGet1() {
    test("a", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveVarGet2
  public void testRemoveVarGet2() {
    test("var a = 1;a", "var a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveNamespaceGet1
  public void testRemoveNamespaceGet1() {
    test("var a = {};a.b", "var a = {}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveNamespaceGet2
  public void testRemoveNamespaceGet2() {
    test("var a = {};a.b=1;a.b", "var a = {};a.b=1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemovePrototypeGet1
  public void testRemovePrototypeGet1() {
    test("var a = {};a.prototype.b", "var a = {}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemovePrototypeGet2
  public void testRemovePrototypeGet2() {
    test("var a = {};a.prototype.b = 1;a.prototype.b",
         "var a = {};a.prototype.b = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveAdd1
  public void testRemoveAdd1() {
    test("1 + 2", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveVar1
  public void testNoRemoveVar1() {
    testSame("var a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveVar2
  public void testNoRemoveVar2() {
    testSame("var a = 1, b = 2");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign1
  public void testNoRemoveAssign1() {
    testSame("a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign2
  public void testNoRemoveAssign2() {
    testSame("a = b = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign3
  public void testNoRemoveAssign3() {
    test("1 + (a = 2)", "a = 2");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign4
  public void testNoRemoveAssign4() {
    testSame("x.a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign5
  public void testNoRemoveAssign5() {
    testSame("x.a = x.b = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign6
  public void testNoRemoveAssign6() {
    test("1 + (x.a = 2)", "x.a = 2");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall1
  public void testNoRemoveCall1() {
    testSame("a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall2
  public void testNoRemoveCall2() {
    test("a()+b()", "a(),b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall3
  public void testNoRemoveCall3() {
    testSame("a() && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall4
  public void testNoRemoveCall4() {
    testSame("a() || b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall5
  public void testNoRemoveCall5() {
    test("a() || 1", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall6
  public void testNoRemoveCall6() {
    testSame("1 || a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveThrow1
  public void testNoRemoveThrow1() {
    testSame("function f(){throw a()}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveThrow2
  public void testNoRemoveThrow2() {
    testSame("function f(){throw a}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveThrow3
  public void testNoRemoveThrow3() {
    testSame("function f(){throw 10}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveInControlStructure1
  public void testRemoveInControlStructure1() {
    test("if(x()) 1", "x()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveInControlStructure2
  public void testRemoveInControlStructure2() {
    test("while(2) 1", "while(2);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveInControlStructure3
  public void testRemoveInControlStructure3() {
    test("for(1;2;3) 4", "for(;;);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook1
  public void testHook1() {
    test("1 ? 2 : 3", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook2
  public void testHook2() {
    test("x ? a() : 3", "x && a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook3
  public void testHook3() {
    test("x ? 2 : a()", "x || a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook4
  public void testHook4() {
    testSame("x ? a() : b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook5
  public void testHook5() {
    test("a() ? 1 : 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook6
  public void testHook6() {
    test("a() ? b() : 2", "a() && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook7
  public void testHook7() {
    test("a() ? 1 : b()", "a() || b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook8
  public void testHook8() {
    testSame("a() ? b() : c()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit1
  public void testShortCircuit1() {
    testSame("1 && a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "1 && a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit4
  public void testShortCircuit4() {
    testSame("a() && 1 && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex1
  public void testComplex1() {
    test("1 && a() + b() + c()", "1 && (a(), b(), c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex2
  public void testComplex2() {
    test("1 && (a() ? b() : 1)", "1 && a() && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex3
  public void testComplex3() {
    test("1 && (a() ? b() : 1 + c())", "1 && (a() ? b() : c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex4
  public void testComplex4() {
    test("1 && (a() ? 1 : 1 + c())", "1 && (a() || c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex5
  public void testComplex5() {
    
    testSame("(a() ? 1 : 1 + c()) && foo()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveFunctionDeclaration1
  public void testNoRemoveFunctionDeclaration1() {
    testSame("function foo(){}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveFunctionDeclaration2
  public void testNoRemoveFunctionDeclaration2() {
    testSame("var foo = function (){}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoSimplifyFunctionArgs1
  public void testNoSimplifyFunctionArgs1() {
    testSame("f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoSimplifyFunctionArgs2
  public void testNoSimplifyFunctionArgs2() {
    testSame("1 && f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoSimplifyFunctionArgs3
  public void testNoSimplifyFunctionArgs3() {
    testSame("1 && foo(a() ? b() : 1 + c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits1
  public void testNoRemoveInherits1() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits2
  public void testNoRemoveInherits2() {
    test("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a) + 1",
         "var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits3
  public void testNoRemoveInherits3() {
    testSame("this.a = {}; var b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits4
  public void testNoRemoveInherits4() {
    test("this.a = {}; var b = {}; b.inherits(a) + 1;",
         "this.a = {}; var b = {}; b.inherits(a)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveFromLabel1
  public void testRemoveFromLabel1() {
    test("LBL: void 0", "LBL: {}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveFromLabel2
  public void testRemoveFromLabel2() {
    test("LBL: foo() + 1 + bar()", "LBL: foo(),bar()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testCall1
  public void testCall1() {
    test("Math.sin(0);", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testCall2
  public void testCall2() {
    test("1 + Math.sin(0);", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNew1
  public void testNew1() {
    test("new Date;", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNew2
  public void testNew2() {
    test("1 + new Date;", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldAssign
  public void testFoldAssign() {
    test("x=x", "");
    testSame("x=xy");
    testSame("x=x + 1");
    testSame("x.a=x.a");
    test("var y=(x=x)", "var y=x");
    test("y=1 + (x=x)", "y=1 + x");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testTryCatchFinally
  public void testTryCatchFinally() {
    testSame("try {foo()} catch (e) {bar()}");
    testSame("try { try {foo()} catch (e) {bar()}} catch (x) {bar()}");
    test("try {var x = 1} finally {}", "var x = 1;");
    testSame("try {var x = 1} finally {x()}");
    test("function f() { return; try{var x = 1}finally{} }",
        "function f() { return; var x = 1; }");
    test("try {} finally {x()}", "x()");
    test("try {} catch (e) { bar()} finally {x()}", "x()");
    test("try {} catch (e) { bar()}", "");
    test("try {} catch (e) { var a = 0; } finally {x()}", "var a; x()");
    test("try {} catch (e) {}", "");
    test("try {} finally {}", "");
    test("try {} catch (e) {} finally {}", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testObjectLiteral
  public void testObjectLiteral() {
    test("({})", "");
    test("({a:1})", "");
    test("({a:foo()})", "foo()");
    test("({'a':foo()})", "foo()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testArrayLiteral
  public void testArrayLiteral() {
    test("([])", "");
    test("([1])", "");
    test("([a])", "");
    test("([foo()])", "foo()");
  }

// com.google.javascript.jscomp.PrepareAstTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondVal = secondKey.getFirstChild();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
  }

// com.google.javascript.jscomp.PrepareAstTest::testFreeCall1
  public void testFreeCall1() throws Exception {
    Node root = parseExpectedJs("foo();");
    Node script = root.getFirstChild();
    Preconditions.checkState(script.isScript());
    Node firstExpr = script.getFirstChild();
    Node call = firstExpr.getFirstChild();
    Preconditions.checkState(call.isCall());

    assertTrue(call.getBooleanProp(Node.FREE_CALL));
  }

// com.google.javascript.jscomp.PrepareAstTest::testFreeCall2
  public void testFreeCall2() throws Exception {
    Node root = parseExpectedJs("x.foo();");
    Node script = root.getFirstChild();
    Preconditions.checkState(script.isScript());
    Node firstExpr = script.getFirstChild();
    Node call = firstExpr.getFirstChild();
    Preconditions.checkState(call.isCall());

    assertFalse(call.getBooleanProp(Node.FREE_CALL));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleProvides
  public void testSimpleProvides() {
    test("goog.provide('foo');",
         "var foo={};");
    test("goog.provide('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo.bar.baz.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.baz.boo={};");
    test("goog.provide('goog.bar');",
         "goog.bar={};");  
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleProvides
  public void testMultipleProvides() {
    test("goog.provide('foo.bar'); goog.provide('foo.baz');",
         "var foo={}; foo.bar={}; foo.baz={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.boo.foo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.boo={}; foo.boo.foo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.boo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('goog.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; goog.bar={}; " +
         "goog.bar.boo={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfProvidedObjLit
  public void testRemovalOfProvidedObjLit() {
    test("goog.provide('foo'); foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo'); var foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); var foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); var foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo.bar.Baz'); foo.bar.Baz=function(){};",
         "var foo={}; foo.bar={}; foo.bar.Baz=function(){};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1,S:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1,S:2};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1}; foo.bar.moo={E:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1}; foo.bar.moo={E:2};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvidedDeclaredFunctionError
  public void testProvidedDeclaredFunctionError() {
    test("goog.provide('foo'); function foo(){}",
         null, FUNCTION_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment1
  public void testRemovalMultipleAssignment1() {
    test("goog.provide('foo'); foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment2
  public void testRemovalMultipleAssignment2() {
    test("goog.provide('foo'); var foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment3
  public void testRemovalMultipleAssignment3() {
    test("goog.provide('foo'); foo = 0; var foo = 1",
         "foo = 0; var foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment4
  public void testRemovalMultipleAssignment4() {
    test("goog.provide('foo.bar'); foo.bar = 0; foo.bar = 1",
         "var foo = {}; foo.bar = 0; foo.bar = 1");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction1
  public void testNoRemovalFunction1() {
    test("goog.provide('foo'); function f(){foo = 0}",
         "var foo = {}; function f(){foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction2
  public void testNoRemovalFunction2() {
    test("goog.provide('foo'); function f(){var foo = 0}",
         "var foo = {}; function f(){var foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf1
  public void testRemovalMultipleAssignmentInIf1() {
    test("goog.provide('foo'); if (true) { var foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf2
  public void testRemovalMultipleAssignmentInIf2() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { var foo = 1 }",
         "if (true) { foo = 0 } else { var foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf3
  public void testRemovalMultipleAssignmentInIf3() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf4
  public void testRemovalMultipleAssignmentInIf4() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {}; if (true) { foo.bar = 0 } else { foo.bar = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError1
  public void testMultipleDeclarationError1() {
    String rest = "if (true) { foo.bar = 0 } else { foo.bar = 1 }";
    test("goog.provide('foo.bar');" + "var foo = {};" + rest,
         "var foo = {};" + "var foo = {};" + rest);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError2
  public void testMultipleDeclarationError2() {
    test("goog.provide('foo.bar');" +
         "if (true) { var foo = {}; foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  var foo = {}; foo.bar = 0" +
         "} else {" +
         "  foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError3
  public void testMultipleDeclarationError3() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { var foo = {}; foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  foo.bar = 0" +
         "} else {" +
         "  var foo = {}; foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideAfterDeclarationError
  public void testProvideAfterDeclarationError() {
    test("var x = 42; goog.provide('x');",
         "var x = 42; var x = {}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideErrorCases
  public void testProvideErrorCases() {
    test("goog.provide();", "", NULL_ARGUMENT_ERROR);
    test("goog.provide(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide({});", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide('foo', 'bar');", "", TOO_MANY_ARGUMENTS_ERROR);
    test("goog.provide('foo'); goog.provide('foo');", "",
        DUPLICATE_NAMESPACE_ERROR);
    test("goog.provide('foo.bar'); goog.provide('foo'); goog.provide('foo');",
        "", DUPLICATE_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfRequires
  public void testRemovalOfRequires() {
    test("goog.provide('foo'); goog.require('foo');",
         "var foo={};");
    test("goog.provide('foo.bar'); goog.require('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz'); goog.require('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo'); var x = 3; goog.require('foo'); something();",
         "var foo={}; var x = 3; something();");
    testSame("foo.require('foo.bar');");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireErrorCases
  public void testRequireErrorCases() {
    test("goog.require();", "", NULL_ARGUMENT_ERROR);
    test("goog.require(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require({});", "", INVALID_ARGUMENT_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateProvides
  public void testLateProvides() {
    test("goog.require('foo'); goog.provide('foo');",
         "var foo={};", LATE_PROVIDE_ERROR);
    test("goog.require('foo.bar'); goog.provide('foo.bar');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
    test("goog.provide('foo.bar'); goog.require('foo'); goog.provide('foo');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingProvides
  public void testMissingProvides() {
    test("goog.require('foo');",
         "", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('Foo');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('foo.bar');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); var EXPERIMENT_FOO = true; " +
             "if (EXPERIMENT_FOO) {goog.require('foo.bar');}",
         "var foo={}; var EXPERIMENT_FOO = true; if (EXPERIMENT_FOO) {}",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testAddDependency
  public void testAddDependency() {
    test("goog.addDependency('x.js', ['A', 'B'], []);", "0");

    Compiler compiler = getLastCompiler();
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("A"));
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("B"));
    assertFalse(compiler.getTypeRegistry().isForwardDeclaredType("C"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMapping
  public void testValidSetCssNameMapping() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'});", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMappingWithType
  public void testValidSetCssNameMappingWithType() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'}, 'BY_PART');", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));

    test("goog.setCssNameMapping({foo:'bar',biz:'baz','biz-foo':'baz-bar'}," +
        " 'BY_WHOLE');", "");
    map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
    assertEquals("baz-bar", map.get("biz-foo"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingNonStringValueReturnsError
  public void testSetCssNameMappingNonStringValueReturnsError() {
    
    test("var BAR = {foo:'bar'}; goog.setCssNameMapping(BAR);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping([]);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(false);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(null);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(undefined);", "",
        EXPECTED_OBJECTLIT_ERROR);

    
    test("var BAR = 'bar'; goog.setCssNameMapping({foo:BAR});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:6});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:false});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:null});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:undefined});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingValidity
  public void testSetCssNameMappingValidity() {
    
    test("goog.setCssNameMapping({'a': 'b', 'a-a': 'c'})", "", null,
        INVALID_CSS_RENAMING_MAP);

    
    test("goog.setCssNameMapping({'a': 'b', 'a-a': 'c'}, 'BY_WHOLE')", "", null,
        INVALID_CSS_RENAMING_MAP);

    
    test("goog.setCssNameMapping({foo:'bar'}, 'UNKNOWN');", "",
        INVALID_STYLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testBadCrossModuleRequire
  public void testBadCrossModuleRequire() {
    test(
        createModuleStar(
            "",
            "goog.provide('goog.ui');",
            "goog.require('goog.ui');"),
        new String[] {
          "",
          "goog.ui = {};",
          ""
        },
        null,
        XMODULE_REQUIRE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire1
  public void testGoodCrossModuleRequire1() {
    test(
        createModuleStar(
            "goog.provide('goog.ui');",
            "",
            "goog.require('goog.ui');"),
        new String[] {
            "goog.ui = {};",
            "",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire2
  public void testGoodCrossModuleRequire2() {
    test(
        createModuleStar(
            "",
            "",
            "goog.provide('goog.ui'); goog.require('goog.ui');"),
        new String[] {
            "",
            "",
            "goog.ui = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvide
  public void testSimpleAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvideAtEnd
  public void testSimpleAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};var b={};b.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleDottedAdditionalProvide
  public void testSimpleDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('c.d.D'); c.d.D = {};",
         "var a={};a.b={};a.b.B={};var c={};c.d={};c.d.D={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvide
  public void testOverlappingAdditionalProvide() {
    additionalCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvideAtEnd
  public void testOverlappingAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingDottedAdditionalProvide
  public void testOverlappingDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('a.b.C'); a.b.C = {};",
         "var a={};a.b={};a.b.B={};a.b.C={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfAdditionalProvide
  public void testRequireOfAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.B'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingRequireWithAdditionalProvide
  public void testMissingRequireWithAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.C'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateRequire
  public void testLateRequire() {
    additionalEndCode = "goog.require('a.A');";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides
  public void testReorderedProvides() {
    additionalCode = "a.B = {};";  
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides2
  public void testReorderedProvides2() {
    additionalEndCode = "a.B = {};";
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder1
  public void testProvideOrder1() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b.c;" +
         "a.b = function(x,y) {};",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b.c;" +
         "a.b = function(x,y) {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder2
  public void testProvideOrder2() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b = function(x,y) {};" +
         "a.b.c;",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3a
  public void testProvideOrder3a() {
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3b
  public void testProvideOrder3b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4a
  public void testProvideOrder4a() {
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4b
  public void testProvideOrder4b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidProvide
  public void testInvalidProvide() {
    test("goog.provide('a.class');", null, INVALID_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase1
  public void testInvalidBase1() {
    test("goog.base(this, 'method');", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase2
  public void testInvalidBase2() {
    test("function Foo() {}" +
         "Foo.method = function() {" +
         "  goog.base(this, 'method');" +
         "};", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase3
  public void testInvalidBase3() {
    test(String.format(METHOD_FORMAT, "goog.base();"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase4
  public void testInvalidBase4() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'bar');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase5
  public void testInvalidBase5() {
    test(String.format(METHOD_FORMAT, "goog.base('foo', 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase6
  public void testInvalidBase6() {
    test(String.format(METHOD_FORMAT, "goog.base.call(null, this, 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase7
  public void testInvalidBase7() {
    test("function Foo() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase8
  public void testInvalidBase8() {
    test("var Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase9
  public void testInvalidBase9() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase1
  public void testValidBase1() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method');"),
         String.format(METHOD_FORMAT, "Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase2
  public void testValidBase2() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method', 1, 2);"),
         String.format(METHOD_FORMAT,
             "Foo.superClass_.method.call(this, 1, 2)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase3
  public void testValidBase3() {
    test(String.format(METHOD_FORMAT, "return goog.base(this, 'method');"),
         String.format(METHOD_FORMAT,
             "return Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase4
  public void testValidBase4() {
    test("function Foo() { goog.base(this, 1, 2); }" + FOO_INHERITS,
         "function Foo() { BaseFoo.call(this, 1, 2); } " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase5
  public void testValidBase5() {
    test("var Foo = function() { goog.base(this, 1); };" + FOO_INHERITS,
         "var Foo = function() { BaseFoo.call(this, 1); }; " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase6
  public void testValidBase6() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);",
         "var goog = {}; goog.Foo = function() { goog.BaseFoo.call(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitAndExplicitProvide
  public void testImplicitAndExplicitProvide() {
    test("var goog = {}; " +
         "goog.provide('goog.foo.bar'); goog.provide('goog.foo');",
         "var goog = {}; goog.foo = {}; goog.foo.bar = {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules
  public void testImplicitProvideInIndependentModules() {
    test(
        createModuleStar(
            "",
            "goog.provide('apps.A');",
            "goog.provide('apps.B');"),
        new String[] {
            "var apps = {};",
            "apps.A = {};",
            "apps.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules2
  public void testImplicitProvideInIndependentModules2() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.A');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.A = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules3
  public void testImplicitProvideInIndependentModules3() {
    test(
        createModuleStar(
            "var goog = {};",
            "goog.provide('goog.foo.A');",
            "goog.provide('goog.foo.B');"),
        new String[] {
            "var goog = {}; goog.foo = {};",
            "goog.foo.A = {};",
            "goog.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules1
  public void testProvideInIndependentModules1() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2
  public void testProvideInIndependentModules2() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2b
  public void testProvideInIndependentModules2b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = function() {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = function() {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3
  public void testProvideInIndependentModules3() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); goog.require('apps.foo');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.B = {};",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3b
  public void testProvideInIndependentModules3b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); apps.foo = function() {}; " +
            "goog.require('apps.foo');"),
        new String[] {
            "var apps = {};",
            "apps.foo.B = {};",
            "apps.foo = function() {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules4
  public void testProvideInIndependentModules4() {
    
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.bar.B');",
            "goog.provide('apps.foo.bar.C');"),
        new String[] {
            "var apps = {};apps.foo = {};apps.foo.bar = {}",
            "apps.foo.bar.B = {};",
            "apps.foo.bar.C = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfBaseGoog
  public void testRequireOfBaseGoog() {
    test("goog.require('goog');",
         "", MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSourcePositionPreservation
  public void testSourcePositionPreservation() {
    test("goog.provide('foo.bar.baz');",
         "var foo = {};" +
         "foo.bar = {};" +
         "foo.bar.baz = {};");

    Node root = getLastCompiler().getRoot();

    Node fooDecl = findQualifiedNameNode("foo", root);
    Node fooBarDecl = findQualifiedNameNode("foo.bar", root);
    Node fooBarBazDecl = findQualifiedNameNode("foo.bar.baz", root);

    assertEquals(1, fooDecl.getLineno());
    assertEquals(14, fooDecl.getCharno());

    assertEquals(1, fooBarDecl.getLineno());
    assertEquals(18, fooBarDecl.getCharno());

    assertEquals(1, fooBarBazDecl.getLineno());
    assertEquals(22, fooBarBazDecl.getCharno());
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef
  public void testNoStubForProvidedTypedef() {
    test("goog.provide('x');  var x;", "var x;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef2
  public void testNoStubForProvidedTypedef2() {
    test("goog.provide('x.y');  x.y;",
         "var x = {}; x.y;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoStubForProvidedTypedef4
  public void testNoStubForProvidedTypedef4() {
    test("goog.provide('x.y.z');  x.y.z;",
         "var x = {}; x.y = {}; x.y.z;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideRequireSameFile
  public void testProvideRequireSameFile() {
    test("goog.provide('x');\ngoog.require('x');", "var x = {};");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine1
  public void testBasicDefine1() {
    test(" var DEF = true", "var DEF=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine2
  public void testBasicDefine2() {
    test(" var DEF = 'a'", "var DEF=\"a\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine3
  public void testBasicDefine3() {
    test(" var DEF = 0", "var DEF=0");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineBadType
  public void testDefineBadType() {
    test(" var DEF = {}",
        null, ProcessDefines.INVALID_DEFINE_TYPE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue1
  public void testDefineWithBadValue1() {
    test(" var DEF = new Boolean(true);", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue2
  public void testDefineWithBadValue2() {
    test(" var DEF = 'x' + y;", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithDependentValue
  public void testDefineWithDependentValue() {
    test(" var BASE = false;\n" +
         " var DEF = !BASE;",
         "var BASE=false;var DEF=!BASE");
    test("var a = {};\n" +
         " a.BASE = false;\n" +
         " a.DEF = !a.BASE;",
         "var a={};a.BASE=false;a.DEF=!a.BASE");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithInvalidDependentValue
  public void testDefineWithInvalidDependentValue() {
    test("var BASE = false;\n" +
         " var DEF = !BASE;",
         null,
          ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding1
  public void testOverriding1() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    overrides.put("DEF_OVERRIDE_TO_FALSE", new Node(Token.FALSE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = false;" +
        " var DEF_OVERRIDE_TO_FALSE = true",
        "var DEF_OVERRIDE_TO_TRUE=true;var DEF_OVERRIDE_TO_FALSE=false");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding2
  public void testOverriding2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    String normalConst = "var DEF_OVERRIDE_TO_FALSE=true;";
    testWithPrefix(
        normalConst,
        " var DEF_OVERRIDE_TO_TRUE = false",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding3
  public void testOverriding3() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = true;",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString0
  public void testOverridingString0() {
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"x\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString1
  public void testOverridingString1() {
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"x\" + \"y\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString2
  public void testOverridingString2() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString3
  public void testOverridingString3() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testMisspelledOverride
  public void testMisspelledOverride() {
    overrides.put("DEF_BAD_OVERIDE", new Node(Token.TRUE));
    test(" var DEF_BAD_OVERRIDE = true",
        "var DEF_BAD_OVERRIDE=true", null,
        ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testCompiledIsKnownDefine
  public void testCompiledIsKnownDefine() {
    overrides.put("COMPILED", new Node(Token.TRUE));
    testSame("");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign1
  public void testSimpleReassign1() {
    test(" var DEF = false; DEF = true;",
        "var DEF=true;true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign2
  public void testSimpleReassign2() {
    test(" var DEF=false;DEF=true;DEF=3",
        "var DEF=3;true;3");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(1, def.getRefs().size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.getDeclaration());
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign3
  public void testSimpleReassign3() {
    test(" var DEF = false;var x;x = DEF = true;",
        "var DEF=true;var x;x=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDuplicateVar
  public void testDuplicateVar() {
    test(" var DEF = false; var DEF = true;",
         null, VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration1
  public void testAssignBeforeDeclaration1() {
    test("DEF=false;var b=false,DEF=true,c=false",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration2
  public void testAssignBeforeDeclaration2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        "DEF_OVERRIDE_TO_TRUE = 3;" +
        " var DEF_OVERRIDE_TO_TRUE = false;",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testEmptyDeclaration
  public void testEmptyDeclaration() {
    test(" var DEF;",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterCall
  public void testReassignAfterCall() {
    test("var DEF=true;externMethod();DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRef
  public void testReassignAfterRef() {
    test("var DEF=true;var x = DEF;DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignWithExpr
  public void testReassignWithExpr() {
    test("var DEF=true;var x;DEF=x=false",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterNonGlobalRef
  public void testReassignAfterNonGlobalRef() {
    test(
        "var DEF=true;" +
        "var x=function(){var y=DEF}; DEF=false",
        "var DEF=false;var x=function(){var y=DEF};false");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(2, def.getRefs().size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.getDeclaration());
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRefInConditional
  public void testReassignAfterRefInConditional() {
    test(
        "var DEF=true;" +
        "if (false) {var x=DEF} DEF=false;",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignInNonGlobalScope
  public void testAssignInNonGlobalScope() {
    test("var DEF=true;function foo() {DEF=false};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDeclareInNonGlobalScope
  public void testDeclareInNonGlobalScope() {
    test("function foo() {var DEF=true;};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineAssignmentInLoop
  public void testDefineAssignmentInLoop() {
    test("var DEF=true;var x=0;while (x) {DEF=false;}",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testWithNoDefines
  public void testWithNoDefines() {
    testSame("var DEF=true;var x={};x.foo={}");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine1
  public void testNamespacedDefine1() {
    test("var a = {};  a.B = false; a.B = true;",
         "var a = {}; a.B = true; true;");

    Name aDotB = namespace.getNameIndex().get("a.B");
    assertEquals(1, aDotB.getRefs().size());
    assertEquals(1, aDotB.globalSets);
    assertNotNull(aDotB.getDeclaration());
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2a
  public void testNamespacedDefine2a() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2b
  public void testNamespacedDefine2b() {
    
    
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {  B : false };",
         "var a = {B : false};",
         null, ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2c
  public void testNamespacedDefine2c() {
    
    
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {  get B() { return false } };",
      "var a = {get B() { return false } };",
      null, ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine3
  public void testNamespacedDefine3() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};", "var a = {};", null,
         ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine4
  public void testNamespacedDefine4() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverrideAfterAlias
  public void testOverrideAfterAlias() {
    test("var x; var DEF=true; x=DEF; DEF=false;",
         null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303
  public void testIssue303() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "(new F()).setLocation('http://www.google.com/');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303b
  public void testIssue303b() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "function x() {" +
        "  (new F()).setLocation('http://www.google.com/');" +
        "} window['x'] = x;",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new1
  public void testAnnotationInExterns_new1() throws Exception {
    checkMarkedCalls("externSENone()",
        ImmutableList.<String>of("externSENone"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new2
  public void testAnnotationInExterns_new2() throws Exception {
    checkMarkedCalls("externSEThis()",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new3
  public void testAnnotationInExterns_new3() throws Exception {
    checkMarkedCalls("new externObjSEThis()",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new4
  public void testAnnotationInExterns_new4() throws Exception {
    
    

    checkMarkedCalls("new externObjSEThis().externObjSEThisMethod('')",
        ImmutableList.<String>of(
           "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new5
  public void testAnnotationInExterns_new5() throws Exception {
    checkMarkedCalls(
        "function f() { new externObjSEThis() };" +
        "f();",
        ImmutableList.<String>of("externObjSEThis", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new6
  public void testAnnotationInExterns_new6() throws Exception {
    
    
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod('') " +
        "};" +
        "f();",
         ImmutableList.<String>of(
             "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new7
  public void testAnnotationInExterns_new7() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  var x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new8
  public void testAnnotationInExterns_new8() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(new externObjSEThis());",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new9
  public void testAnnotationInExterns_new9() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(g);",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new10
  public void testAnnotationInExterns_new10() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod2('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1
  public void testAnnotationInExternStubs1() throws Exception {
    checkMarkedCalls("o.propWithStubBefore('a');",
        ImmutableList.<String>of("o.propWithStubBefore"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1b
  public void testAnnotationInExternStubs1b() throws Exception {
    checkMarkedCalls("o.propWithStubBeforeWithJSDoc('a');",
        ImmutableList.<String>of("o.propWithStubBeforeWithJSDoc"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2
  public void testAnnotationInExternStubs2() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2b
  public void testAnnotationInExternStubs2b() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs3
  public void testAnnotationInExternStubs3() throws Exception {
    checkMarkedCalls("propWithAnnotatedStubAfter('a');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs4
  public void testAnnotationInExternStubs4() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs5
  public void testAnnotationInExternStubs5() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testResultLocalitySimple
  public void testResultLocalitySimple() throws Exception {
    String prefix = "var g; function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");
    List<String> notExpected = ImmutableList.of();

    
    checkLocalityOfMarkedCalls(
        prefix + "" + suffix, expected);
    
    checkLocalityOfMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return 2" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; return a" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return {foo : 1}.foo" + suffix,
        notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix,
        notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return externObj" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        "function inner(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, notExpected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }
