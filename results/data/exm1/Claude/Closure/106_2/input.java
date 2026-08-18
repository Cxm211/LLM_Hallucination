// buggy code
    boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      // Don't try to collapse if the one global set is a twin reference.
      // We could theoretically handle this case in CollapseProperties, but
      // it's probably not worth the effort.

      if (isClassOrEnum) {
        return true;
      }
      return (type == Type.FUNCTION || aliasingGets == 0) &&
          (parent == null || parent.canCollapseUnannotatedChildNames());
    }

  public boolean recordBlockDescription(String description) {
    if (parseDocumentation) {
    populated = true;
    }
    return currentInfo.documentBlock(description);
  }

// relevant test
// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionDeclaration
  public void testInlineFunctionDeclaration() {
    test("var f = function () {}; var a = f;", 
         "var a = function () {};");
    test("var f = function () {}; foo(); var a = f;", 
         "foo(); var a = function () {};");
    test("var f = function () {}; foo(f);", 
         "foo(function () {});");
    
    testSame("var f = function () {}; function g() {var a = f;}"); 
    testSame("var f = function () {}; function g() {h(f);}"); 
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("var x = 0; (function x() { return x ? x() : 3; })();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    testSame("var x = function y() {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    testSame("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1
  public void testInlineAliases1() {
    test("var x = this.foo(); this.bar(); var y = x; this.baz(y);",
         "var x = this.foo(); this.bar(); this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1b
  public void testInlineAliases1b() {
    test("var x = this.foo(); this.bar(); var y; y = x; this.baz(y);",
         "var x = this.foo(); this.bar(); x; this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1c
  public void testInlineAliases1c() {
    test("var x; x = this.foo(); this.bar(); var y = x; this.baz(y);",
         "var x; x = this.foo(); this.bar(); this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1d
  public void testInlineAliases1d() {
    test("var x; x = this.foo(); this.bar(); var y; y = x; this.baz(y);",
         "var x; x = this.foo(); this.bar(); x; this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2
  public void testInlineAliases2() {
    test("var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); }",
         "var x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2b
  public void testInlineAliases2b() {
    test("var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); }",
         "var x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2c
  public void testInlineAliases2c() {
    test("var x; x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); }",
         "var x; x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2d
  public void testInlineAliases2d() {
    test("var x; x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); }",
         "var x; x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases1
  public void testNoInlineAliases1() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; x = 3; this.baz(y);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases1b
  public void testNoInlineAliases1b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; x = 3; this.baz(y);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases2
  public void testNoInlineAliases2() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; y = 3; this.baz(y); ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases2b
  public void testNoInlineAliases2b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; y = 3; this.baz(y); ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases3
  public void testNoInlineAliases3() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; g(); this.baz(y); } " +
         "function g() { x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases3b
  public void testNoInlineAliases3b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; g(); this.baz(y); } " +
         "function g() { x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases4
  public void testNoInlineAliases4() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; y = 3; this.baz(y); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases4b
  public void testNoInlineAliases4b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; y = 3; this.baz(y); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases5
  public void testNoInlineAliases5() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; this.bing();" +
        "this.baz(y); x = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases5b
  public void testNoInlineAliases5b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; this.bing();" +
        "this.baz(y); x = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases6
  public void testNoInlineAliases6() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; this.bing();" +
        "this.baz(y); y = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases6b
  public void testNoInlineAliases6b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; this.bing();" +
        "this.baz(y); y = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases7
  public void testNoInlineAliases7() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.bing(); this.baz(y); x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases7b
  public void testNoInlineAliases7b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.bing(); this.baz(y); x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases8
  public void testNoInlineAliases8() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); y = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases8b
  public void testNoInlineAliases8b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); y = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSideEffectOrder
  public void testSideEffectOrder() {
    
    String EXTERNS = "var z; function f(){}";
    test(EXTERNS,
         "var x = f(y.a, y); z = x;",
         "z = f(y.a, y);", null, null);
    
    testSame(EXTERNS, "var x = f(y.a, y); z.b = x;", null, null);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineParameterAlias1
  public void testInlineParameterAlias1() {
    test(
      "function f(x) {" +
      "  var y = x;" +
      "  g();" +
      "  y;y;" +
      "}",
      "function f(x) {" +
      "  g();" +
      "  x;x;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineParameterAlias2
  public void testInlineParameterAlias2() {
    test(
      "function f(x) {" +
      "  var y; y = x;" +
      "  g();" +
      "  y;y;" +
      "}",
      "function f(x) {" +
      "  x;" +
      "  g();" +
      "  x;x;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1
  public void testInlineFunctionAlias1() {
    test(
      "function f(x) {};" +
      "var y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2
  public void testInlineFunctionAlias2() {
    test(
      "function f(x) {};" +
      "var y; y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f;" +
      "g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineCatchAlias1
  public void testInlineCatchAlias1() {
    test(
      "try {" +
      "} catch (e) {" +
      "  var y = e;" +
      "  g();" +
      "  y;y;" +
      "}",
      "try {" +
      "} catch (e) {" +
      "  g();" +
      "  e;e;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineCatchAlias2
  public void testInlineCatchAlias2() {
    test(
      "try {" +
      "} catch (e) {" +
      "  var y; y = e;" +
      "  g();" +
      "  y;y;" +
      "}",
      "try {" +
      "} catch (e) {" +
      "  e;" +
      "  g();" +
      "  e;e;" +
      "}"
      );
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testFunctionAnnotation
  public void testFunctionAnnotation() throws Exception {
    testMarkCalls("function f(){}", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f; f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f;  f = function(){};", "f()",
                  ImmutableList.of("f"));

    
    testMarkCalls("function f(){}", Collections.<String>emptyList());
    testMarkCalls("function f(){} f()", Collections.<String>emptyList());

    
    testMarkCalls("var f = " +
                  "function(){};",
                  "f()",
                  ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testNamespaceAnnotation
  public void testNamespaceAnnotation() throws Exception {
    testMarkCalls("var o = {}; o.f = function(){};",
        "o.f()", ImmutableList.of("o.f"));
    testMarkCalls("var o = {}; o.f = function(){};",
        "o.f()", ImmutableList.of("o.f"));
    testMarkCalls("var o = {}; o.f = function(){}; o.f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testConstructorAnnotation
  public void testConstructorAnnotation() throws Exception {
    testMarkCalls("function c(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("var c = function(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("var c = function(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("function c(){}; new c", Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    testMarkCalls("function f(){}" +
                  "f = function(){};",
                  "f()",
                  ImmutableList.of("f"));
    testMarkCalls("function f(){}" +
                  "f = function(){};",
                  "f()",
                  Collections.<String>emptyList());
    testMarkCalls("function f(){}",
                  "f = function(){};" +
                  "f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testAssignNoFunction
  public void testAssignNoFunction() throws Exception {
    testMarkCalls("function f(){}", "f = 1; f()",
                  ImmutableList.of("f"));
    testMarkCalls("function f(){}", "f = 1 || 2; f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testPrototype
  public void testPrototype() throws Exception {
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "var o = new c; o.g()",
                  ImmutableList.of("o.g"));
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "function f(){}" +
                  "var o = new c; o.g(); f()",
                  ImmutableList.of("o.g"));

    
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "var o = new c;" +
                  "o.g = function(){};" +
                  "o.g()",
                  ImmutableList.<String>of());
    
    testMarkCalls("function c1(){};" +
                  "c1.prototype.f = function(){};" +
                  "function c2(){};" +
                  "c2.prototype.f = function(){};",
                  "var o = new c1;" +
                  "o.f()",
                  ImmutableList.of("o.f"));

    
    testMarkCalls("function c1(){};" +
                  "c1.prototype.f = function(){};",
                  "function c2(){};" +
                  "c2.prototype.f = function(){};" +
                  "var o = new c1;" +
                  "o.f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testAnnotationInExterns
  public void testAnnotationInExterns() throws Exception {
    testMarkCalls("externSef1()", Collections.<String>emptyList());
    testMarkCalls("externSef2()", Collections.<String>emptyList());
    testMarkCalls("externNsef1()", ImmutableList.of("externNsef1"));
    testMarkCalls("externNsef2()", ImmutableList.of("externNsef2"));
    testMarkCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testNamespaceAnnotationInExterns
  public void testNamespaceAnnotationInExterns() throws Exception {
    testMarkCalls("externObj.sef1()", Collections.<String>emptyList());
    testMarkCalls("externObj.sef2()", Collections.<String>emptyList());
    testMarkCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
    testMarkCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));

    testMarkCalls("externObj.nsef3()", ImmutableList.of("externObj.nsef3"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testOverrideDefinitionInSource
  public void testOverrideDefinitionInSource() throws Exception {
    
    testMarkCalls("var obj = {}; obj.sef1 = function(){}; obj.sef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {};" +
                  "obj.sef1 = function(){};",
                  "obj.sef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {}; obj.nsef1 = function(){}; obj.nsef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {};" +
                  "obj.nsef1 = function(){};",
                  "obj.nsef1()",
                  ImmutableList.of("obj.nsef1"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testApply1
  public void testApply1() throws Exception {
    testMarkCalls(" var f = function() {}",
                  "f.apply()",
                  ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testApply2
  public void testApply2() throws Exception {
    testMarkCalls("var f = function() {}",
                  "f.apply()",
                  ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testCall1
  public void testCall1() throws Exception {
    testMarkCalls(" var f = function() {}",
                  "f.call()",
                  ImmutableList.of("f.call"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testCall2
  public void testCall2() throws Exception {
    testMarkCalls("var f = function() {}",
                  "f.call()",
                  ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion1
  public void testRemoveVarDeclartion1() {
    test("var foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion2
  public void testRemoveVarDeclartion2() {
    test("var foo = 3, bar = 4; externfoo = foo;",
         "var foo = 3; externfoo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion3
  public void testRemoveVarDeclartion3() {
    test("var a = f(), b = 1, c = 2; b; c", "f();var b = 1, c = 2; b; c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion4
  public void testRemoveVarDeclartion4() {
    test("var a = 0, b = f(), c = 2; a; c", "var a = 0;f();var c = 2; a; c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion5
  public void testRemoveVarDeclartion5() {
    test("var a = 0, b = 1, c = f(); a; b", "var a = 0, b = 1; f(); a; b");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion6
  public void testRemoveVarDeclartion6() {
    test("var a = 0, b = a = 1; a", "var a = 0; a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion7
  public void testRemoveVarDeclartion7() {
    test("var a = 0, b = a = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion8
  public void testRemoveVarDeclartion8() {
    test("var a;var b = 0, c = a = b = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveFunction
  public void testRemoveFunction() {
    test("var foo = {}; foo.bar = function() {};", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testReferredToByWindow
  public void testReferredToByWindow() {
    testSame("var foo = {}; foo.bar = function() {}; window['fooz'] = foo.bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExtern
  public void testExtern() {
    testSame("externfoo = 5");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveNamedFunction
  public void testRemoveNamedFunction() {
    test("function foo(){}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction1
  public void testRemoveRecursiveFunction1() {
    test("function f(){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction2
  public void testRemoveRecursiveFunction2() {
    test("var f = function (){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction3
  public void testRemoveRecursiveFunction3() {
    test("var f;f = function (){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction4
  public void testRemoveRecursiveFunction4() {
    
    testSame("f = function (){f()}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction5
  public void testRemoveRecursiveFunction5() {
    test("function g(){f()}function f(){g()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction6
  public void testRemoveRecursiveFunction6() {
    test("var f=function(){g()};function g(){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction7
  public void testRemoveRecursiveFunction7() {
    test("var g = function(){f()};var f = function(){g()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction8
  public void testRemoveRecursiveFunction8() {
    test("var o = {};o.f = function(){o.f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction9
  public void testRemoveRecursiveFunction9() {
    testSame("var o = {};o.f = function(){o.f()};o.f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification1
  public void testSideEffectClassification1() {
    test("foo();", "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification2
  public void testSideEffectClassification2() {
    test("var a = foo();", "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification3
  public void testSideEffectClassification3() {
    testSame("var a = foo();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification4
  public void testSideEffectClassification4() {
    testSame("function sef(){} sef();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification5
  public void testSideEffectClassification5() {
    testSame("function nsef(){} var a = nsef();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification6
  public void testSideEffectClassification6() {
    test("function sef(){} sef();", "function sef(){} sef();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification7
  public void testSideEffectClassification7() {
    testSame("function sef(){} var a = sef();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation1
  public void testNoSideEffectAnnotation1() {
    test("function f(){} var a = f();",
         "function f(){} f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation2
  public void testNoSideEffectAnnotation2() {
    test("function f(){}", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation3
  public void testNoSideEffectAnnotation3() {
    test("var f = function(){}; var a = f();",
         "var f = function(){}; f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation4
  public void testNoSideEffectAnnotation4() {
    test("var f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation5
  public void testNoSideEffectAnnotation5() {
    test("var f; f = function(){}; var a = f();",
         "var f; f = function(){}; f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation6
  public void testNoSideEffectAnnotation6() {
    test("var f; f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation7
  public void testNoSideEffectAnnotation7() {
    test("var f;" +
         "f = function(){};",
         "f = function(){};" +
         "var a = f();",
         "f = function(){}; f();", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation8
  public void testNoSideEffectAnnotation8() {
    test("var f;" +
         "f = function(){};" +
         "f = function(){};",
         "var a = f();",
         "f();", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation9
  public void testNoSideEffectAnnotation9() {
    test("var f;" +
         "f = function(){};" +
         "f = function(){};",
         "var a = f();",
         "", null, null);

    test("var f; f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation10
  public void testNoSideEffectAnnotation10() {
    test("var o = {}; o.f = function(){}; var a = o.f();",
         "var o = {}; o.f = function(){}; o.f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation11
  public void testNoSideEffectAnnotation11() {
    test("var o = {}; o.f = function(){};",
         "var a = o.f();", "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation12
  public void testNoSideEffectAnnotation12() {
    test("function c(){} var a = new c",
         "function c(){} new c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation13
  public void testNoSideEffectAnnotation13() {
    test("function c(){}", "var a = new c",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation14
  public void testNoSideEffectAnnotation14() {
    String common = "function c(){};" +
        "c.prototype.f = function(){};";
    test(common, "var o = new c; var a = o.f()", "new c", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation15
  public void testNoSideEffectAnnotation15() {
    test("function c(){}; c.prototype.f = function(){}; var a = (new c).f()",
         "function c(){}; c.prototype.f = function(){}; (new c).f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation16
  public void testNoSideEffectAnnotation16() {
    test("function c(){}" +
         "c.prototype.f = function(){};",
         "var a = (new c).f()",
         "",
         null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctionPrototype
  public void testFunctionPrototype() {
    testSame("var a = 5; Function.prototype.foo = function() {return a;}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass1
  public void testTopLevelClass1() {
    test("var Point = function() {}; Point.prototype.foo = function() {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass2
  public void testTopLevelClass2() {
    testSame("var Point = {}; Point.prototype.foo = function() {};" +
             "externfoo = new Point()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass3
  public void testTopLevelClass3() {
    test("function Point() {this.me_ = Point}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass4
  public void testTopLevelClass4() {
    test("function f(){} function A(){} A.prototype = {x: function() {}}; f();",
         "function f(){} f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass5
  public void testTopLevelClass5() {
    testSame("function f(){} function A(){}" +
             "A.prototype = {x: function() { f(); }}; new A();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass6
  public void testTopLevelClass6() {
    testSame("function f(){} function A(){}" +
             "A.prototype = {x: function() { f(); }}; new A().x();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass7
  public void testTopLevelClass7() {
    test("A.prototype.foo = function(){}; function A() {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass1
  public void testNamespacedClass1() {
    test("var foo = {};foo.bar = {};foo.bar.prototype.baz = {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass2
  public void testNamespacedClass2() {
    testSame("var foo = {};foo.bar = {};foo.bar.prototype.baz = {};" +
             "window.z = new foo.bar()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass3
  public void testNamespacedClass3() {
    test("var a = {}; a.b = function() {}; a.b.prototype = {x: function() {}};",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass4
  public void testNamespacedClass4() {
    testSame("function f(){} var a = {}; a.b = function() {};" +
             "a.b.prototype = {x: function() { f(); }}; new a.b();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass5
  public void testNamespacedClass5() {
    testSame("function f(){} var a = {}; a.b = function() {};" +
             "a.b.prototype = {x: function() { f(); }}; new a.b().x();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToThisPrototype
  public void testAssignmentToThisPrototype() {
    testSame("Function.prototype.inherits = function(parentCtor) {" +
             "  function tempCtor() {};" +
             "  tempCtor.prototype = parentCtor.prototype;" +
             "  this.superClass_ = parentCtor.prototype;" +
             "  this.prototype = new tempCtor();" +
             "  this.prototype.constructor = this;" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToCallResultPrototype
  public void testAssignmentToCallResultPrototype() {
    testSame("function f() { return function(){}; } f().prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToExternPrototype
  public void testAssignmentToExternPrototype() {
    testSame("externfoo.prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToUnknownPrototype
  public void testAssignmentToUnknownPrototype() {
    testSame(
        "var window;" +
        "window['a'].prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testBug2099540
  public void testBug2099540() {
    testSame(
        "var document,window,klass;" +
        "window[klass].prototype = " +
            "document.createElement(tagName)['__proto__'];");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testOtherGlobal
  public void testOtherGlobal() {
    testSame("goog.global.foo = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternName1
  public void testExternName1() {
    testSame("top.z = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternName2
  public void testExternName2() {
    testSame("top['z'] = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits1
  public void testInherits1() {
    test("var a = {}; var b = {}; b.inherits(a)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits2
  public void testInherits2() {
    test("var a = {}; var b = {}; var goog = {}; goog.inherits(b, a)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits3
  public void testInherits3() {
    testSame("var a = {}; this.b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits4
  public void testInherits4() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits5
  public void testInherits5() {
    test("this.a = {}; var b = {}; b.inherits(a);",
         "this.a = {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits6
  public void testInherits6() {
    test("this.a = {}; var b = {}; var goog = {}; goog.inherits(b, a);",
         "this.a = {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits7
  public void testInherits7() {
    testSame("var a = {}; this.b = {}; var goog = {};" +
        " goog.inherits = function() {}; goog.inherits(b, a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits8
  public void testInherits8() {
    
    
    test("this.a = {}; var b = {}; var c = b.inherits(a);", "this.a = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin1
  public void testMixin1() {
    testSame("var goog = {}; goog.mixin = function() {};" +
             "Function.prototype.mixin = function(base) {" +
             "  goog.mixin(this.prototype, base); " +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin2
  public void testMixin2() {
    testSame("var a = {}; this.b = {}; var goog = {};" +
        " goog.mixin = function() {}; goog.mixin(b.prototype, a.prototype);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin3
  public void testMixin3() {
    test("this.a = {}; var b = {}; var goog = {};" +
         " goog.mixin = function() {}; goog.mixin(b.prototype, a.prototype);",
         "this.a = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin4
  public void testMixin4() {
    testSame("this.a = {}; var b = {}; var goog = {};" +
             "goog.mixin = function() {};" +
             "goog.mixin(b.prototype, a.prototype);" +
             "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin5
  public void testMixin5() {
    test("this.a = {}; var b = {}; var c = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "goog.mixin(c.prototype, a.prototype);" +
         "new b()",
         "this.a = {}; var b = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin6
  public void testMixin6() {
    testSame("this.a = {}; var b = {}; var c = {}; var goog = {};" +
             "goog.mixin = function() {};" +
             "goog.mixin(c.prototype, a.prototype) + " +
             "goog.mixin(b.prototype, a.prototype);" +
             "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin7
  public void testMixin7() {
    test("this.a = {}; var b = {}; var c = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "var d = goog.mixin(c.prototype, a.prototype) + " +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()",
         "this.a = {}; var b = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConstants1
  public void testConstants1() {
    testSame("var bar = function(){}; var EXP_FOO = true; if (EXP_FOO) bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConstants2
  public void testConstants2() {
    test("var bar = function(){}; var EXP_FOO = true; var EXP_BAR = true;" +
         "if (EXP_FOO) bar();",
         "var bar = function(){}; var EXP_FOO = true; if (EXP_FOO) bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions1
  public void testExpressions1() {
    test("var foo={}; foo.A='A'; foo.AB=foo.A+'B'; foo.ABC=foo.AB+'C'",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions2
  public void testExpressions2() {
    testSame("var foo={}; foo.A='A'; foo.AB=foo.A+'B'; this.ABC=foo.AB+'C'");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions3
  public void testExpressions3() {
    testSame("var foo = 2; window.bar(foo + 3)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetCreatingReference
  public void testSetCreatingReference() {
    testSame("var foo; var bar = function(){foo=6;}; bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous1
  public void testAnonymous1() {
    testSame("function foo() {}; function bar() {}; foo(function() {bar()})");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous2
  public void testAnonymous2() {
    test("var foo;(function(){foo=6;})()", "(function(){})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous3
  public void testAnonymous3() {
    testSame("var foo; (function(){ if(!foo)foo=6; })()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous4
  public void testAnonymous4() {
    testSame("var foo; (function(){ foo=6; })(); externfoo=foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous5
  public void testAnonymous5() {
    testSame("var foo;" +
             "(function(){ foo=function(){ bar() }; function bar(){} })();" +
             "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous6
  public void testAnonymous6() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){externfoo = bar});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous7
  public void testAnonymous7() {
    testSame("var foo;" +
             "(function (){ function bar(){ externfoo = foo; } bar(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous8
  public void testAnonymous8() {
    testSame("var foo;" +
             "(function (){ var g=function(){ externfoo = foo; }; g(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous9
  public void testAnonymous9() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){ function baz(){ externfoo = bar; } baz(); });");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions1
  public void testFunctions1() {
    testSame("var foo = null; function baz() {}" +
             "function bar() {foo=baz();} bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions2
  public void testFunctions2() {
    testSame("var foo; foo = function() {var a = bar()};" +
             "var bar = function(){}; foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem1
  public void testGetElem1() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {window[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem2
  public void testGetElem2() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {this[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem3
  public void testGetElem3() {
    testSame("var foo = {'i': 0, 'j': 1}; foo['k'] = 2; top.foo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf1
  public void testIf1() {
    test("var foo = {};if(e)foo.bar=function(){};", "if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf2
  public void testIf2() {
    test("var e = false;var foo = {};if(e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf3
  public void testIf3() {
    test("var e = false;var foo = {};if(e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf4
  public void testIf4() {
    test("var e = false, f;var foo = {};if(f=e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf5
  public void testIf5() {
    test("var e = false, f;var foo = {};if(f = e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIfElse
  public void testIfElse() {
    test("var foo = {};if(e)foo.bar=function(){};else foo.bar=function(){};",
         "if(e);else;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWhile
  public void testWhile() {
    test("var foo = {};while(e)foo.bar=function(){};", "while(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFor
  public void testFor() {
    test("var foo = {};for(e in x)foo.bar=function(){};", "for(e in x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDo
  public void testDo() {
    test("var cond = false;do {var a = 1} while (cond)", "var cond = false;do {} while (cond)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct1
  public void testSetterInForStruct1() {
    test("var j = 0; for (var i = 1; i = 0; j++);",
         "var j = 0; for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct2
  public void testSetterInForStruct2() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.prototype.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct3
  public void testSetterInForStruct3() {
    test("var j = 0; for (var i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; for (f(), g(), h(); 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; for (f(), g(), h(); 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct5
  public void testSetterInForStruct5() {
    test("var i = 0, j = 0; for (i = f(), j = g(); 0;);",
         "for (f(), g(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct6
  public void testSetterInForStruct6() {
    test("var i = 0, j = 0, k = 0; for (i = f(), j = g(), k = h(); i = 0;);",
         "for (f(), g(), h(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct7
  public void testSetterInForStruct7() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = 2, k = 3; i = 0;);",
         "for (1, 2, 3; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct8
  public void testSetterInForStruct8() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = i, k = 2; i = 0;);",
         "var i = 0; for(i = 1, i , 2; i = 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexAssigns
  public void testComplexAssigns() {
    
    testSame("var x = 0; x += 3; x *= 5;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssigns
  public void testNestedAssigns() {
    
    testSame("var x = 0; var y = x = 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns1
  public void testComplexNestedAssigns1() {
    
    testSame("var x = 0; var y = 2; y += x = 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns2
  public void testComplexNestedAssigns2() {
    test("var x = 0; var y = 2; y += x = 3; window.alert(y);",
         "var y = 2; y += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns3
  public void testComplexNestedAssigns3() {
    test("var x = 0; var y = x += 3; window.alert(x);",
         "var x = 0; x += 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns4
  public void testComplexNestedAssigns4() {
    testSame("var x = 0; var y = x += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope1
  public void testUnintendedUseOfInheritsInLocalScope1() {
    testSame("goog.mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope2
  public void testUnintendedUseOfInheritsInLocalScope2() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope3
  public void testUnintendedUseOfInheritsInLocalScope3() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })(); " +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope4
  public void testUnintendedUseOfInheritsInLocalScope4() {
    
    
    testSame("var goog$mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog$mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope1
  public void testPrototypePropertySetInLocalScope1() {
    testSame("(function() { var x = function(){}; x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope2
  public void testPrototypePropertySetInLocalScope2() {
    testSame("var x = function(){}; (function() { x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope3
  public void testPrototypePropertySetInLocalScope3() {
    test("var x = function(){ x.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope4
  public void testPrototypePropertySetInLocalScope4() {
    test("var x = {}; x.foo = function(){ x.foo.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope5
  public void testPrototypePropertySetInLocalScope5() {
    test("var x = {}; x.prototype.foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope6
  public void testPrototypePropertySetInLocalScope6() {
    testSame("var x = {}; x.prototype.foo = 3; bar(x.prototype.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope7
  public void testPrototypePropertySetInLocalScope7() {
    testSame("var x = {}; x.foo = 3; bar(x.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference1
  public void testRValueReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference2
  public void testRValueReference2() {
    testSame("var a = 1; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference3
  public void testRValueReference3() {
    testSame("var x = {}; x.prototype.foo = 3; var a = x.prototype.foo; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference4
  public void testRValueReference4() {
    testSame("var x = {}; x.prototype.foo = 3; x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference5
  public void testRValueReference5() {
    testSame("var x = {}; x.prototype.foo = 3; 1+x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference6
  public void testRValueReference6() {
    testSame("var x = {}; var idx = 2; x[idx]");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnhandledTopNode
  public void testUnhandledTopNode() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "function Bar() {}; Bar.prototype.isFoo = function() {};" +
             "var foo = new Foo(); var bar = new Bar();" +
             
             
             "var cond = foo.isBar() && bar.isFoo();" +
             "if (cond) {window.alert('hello');}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPropertyDefinedInGlobalScope
  public void testPropertyDefinedInGlobalScope() {
    testSame("function Foo() {}; var x = new Foo(); x.cssClass = 'bar';" +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction1
  public void testConditionallyDefinedFunction1() {
    testSame("var g; externfoo.x || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction2
  public void testConditionallyDefinedFunction2() {
    testSame("var g; 1 || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElemOnThis
  public void testGetElemOnThis() {
    testSame("var a = 3; this['foo'] = a;");
    testSame("this['foo'] = 3;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfOnly
  public void testRemoveInstanceOfOnly() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
         "var x; if (x instanceof Foo) { window.alert(x); }",
         ";var x; if (false) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLocalScopedInstanceOfOnly
  public void testRemoveLocalScopedInstanceOfOnly() {
    test("function Foo() {}; function Bar(x) { this.z = x instanceof Foo; };" +
        "externfoo.x = new Bar({});",
        ";function Bar(x) { this.z = false }; externfoo.x = new Bar({});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfWithReferencedMethod
  public void testRemoveInstanceOfWithReferencedMethod() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "var x; if (x instanceof Foo) { window.alert(x.isBar()); }",
        ";var x; if (false) { window.alert(x.isBar()); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedInstanceOf
  public void testDoNotChangeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "var x = new Foo(); if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedLocalScopedInstanceOf
  public void testDoNotChangeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; externfoo.x = new Foo();" +
        "function Bar() { if (x instanceof Foo) { window.alert(x); } };" +
        "externfoo.y = new Bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedInstanceOf
  public void testDoNotChangeLocalScopeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); }; externfoo.x = new Bar();" +
        "if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf
  public void testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); };" +
        "Bar.prototype.func = function(x) {" +
          "if (x instanceof Foo) { window.alert(x); }" +
        "}; new Bar().func();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWeirdnessOnLeftSideOfPrototype
  public void testWeirdnessOnLeftSideOfPrototype() {
    
    
    testSame("var x = 3; " +
        "(function() { this.bar = 3; }).z = function() {" +
        "  return x;" +
        "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit1
  public void testShortCircuit1() {
    test("var a = b() || 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit2
  public void testShortCircuit2() {
    test("var a = 1 || c()", "1 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit3
  public void testShortCircuit3() {
    test("var a = b() || c()", "b() || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit4
  public void testShortCircuit4() {
    test("var a = b() || 3 || c()", "b() || 3 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit5
  public void testShortCircuit5() {
    test("var a = b() && 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit6
  public void testShortCircuit6() {
    test("var a = 1 && c()", "1 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit7
  public void testShortCircuit7() {
    test("var a = b() && c()", "b() && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit8
  public void testShortCircuit8() {
    test("var a = b() && 3 && c()", "b() && 3 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference1
  public void testRhsReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference2
  public void testRhsReference2() {
    testSame("var a = 1; a || b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference3
  public void testRhsReference3() {
    testSame("var a = 1; 1 || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference4
  public void testRhsReference4() {
    test("var a = 1; var b = a || foo()", "var a = 1; a || foo()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference5
  public void testRhsReference5() {
    test("var a = 1, b = 5; a; foo(b)", "var a = 1, b = 5; a; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign1
  public void testRhsAssign1() {
    test("var foo, bar; foo || (bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign2
  public void testRhsAssign2() {
    test("var foo, bar, baz; foo || (baz = bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign3
  public void testRhsAssign3() {
    testSame("var foo = null; foo || (foo = 1)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign4
  public void testRhsAssign4() {
    test("var foo = null; foo = (foo || 1)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign5
  public void testRhsAssign5() {
    test("var a = 3, foo, bar; foo || (bar = a)", "var a = 3, foo; foo || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign6
  public void testRhsAssign6() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {foo || (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign7
  public void testRhsAssign7() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {foo || (foo = new Foo())}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign8
  public void testRhsAssign8() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {(foo = new Foo()) || g()}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign9
  public void testRhsAssign9() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {1 + (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var a, b = a = 1, c = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign2
  public void testNestedAssign2() {
    testSame("var a, b = a = 1; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign3
  public void testNestedAssign3() {
    testSame("var a, b = a = 1; a = b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign4
  public void testNestedAssign4() {
    testSame("var a, b = a = 1; b = a = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign5
  public void testNestedAssign5() {
    test("var a, b = a = 1; b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign15
  public void testNestedAssign15() {
    test("var a, b, c; c = b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign6
  public void testNestedAssign6() {
    testSame("var a, b, c; a = b = c = 1; foo(a, b, c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign7
  public void testNestedAssign7() {
    testSame("var a = 0; a = i[j] = 1; b(a, i[j])");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign8
  public void testNestedAssign8() {
    testSame("function f(){" +
             "this.lockedToken_ = this.lastToken_ = " +
             "SETPROP_value(this.hiddenInput_, a)}f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain1
  public void testRefChain1() {
    test("var a = 1; var b = a; var c = b; var d = c", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain2
  public void testRefChain2() {
    test("var a = 1; var b = a; var c = b; var d = c || f()",
         "var a = 1; var b = a; var c = b; c || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain3
  public void testRefChain3() {
    test("var a = 1; var b = a; var c = b; var d = c + f()", "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain4
  public void testRefChain4() {
    test("var a = 1; var b = a; var c = b; var d = f() || c",
         "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain5
  public void testRefChain5() {
    test("var a = 1; var b = a; var c = b; var d = f() ? g() : c",
         "f() && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain6
  public void testRefChain6() {
    test("var a = 1; var b = a; var c = b; var d = c ? f() : g()",
         "var a = 1; var b = a; var c = b; c ? f() : g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain7
  public void testRefChain7() {
    test("var a = 1; var b = a; var c = b; var d = (b + f()) ? g() : c",
         "var a = 1; var b = a; (b+f()) && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain8
  public void testRefChain8() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain9
  public void testRefChain9() {
    test("var a = 1; var b = a; var c = 5; var d = f()[b+c] ? g() : 0",
         "var a = 1; var b = a; var c = 5; f()[b+c] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain10
  public void testRefChain10() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain11
  public void testRefChain11() {
    test("var a = 1; var b = a; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain12
  public void testRefChain12() {
    testSame("var a = 1; var b = a; f()[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain13
  public void testRefChain13() {
    test("function f(){}var a = 1; var b = a; var d = f()[b] ? g() : 0",
         "function f(){}var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain14
  public void testRefChain14() {
    testSame("function f(){}var a = 1; var b = a; f()[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain15
  public void testRefChain15() {
    test("function f(){}var a = 1, b = a; var c = f(); var d = c[b] ? g() : 0",
         "function f(){}var a = 1, b = a; var c = f(); c[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain16
  public void testRefChain16() {
    testSame("function f(){}var a = 1; var b = a; var c = f(); c[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain17
  public void testRefChain17() {
    test("function f(){}var a = 1; var b = a; var c = f(); var d = c[b]",
         "function f(){} f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain18
  public void testRefChain18() {
    testSame("var a = 1; f()[a] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain19
  public void testRefChain19() {
    test("var a = 1; var b = [a]; var c = b; b[f()] ? g() : 0",
         "var a=1; var b=[a]; b[f()] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain20
  public void testRefChain20() {
    test("var a = 1; var b = [a]; var c = b; var d = b[f()] ? g() : 0",
         "var a=1; var b=[a]; b[f()]&&g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain21
  public void testRefChain21() {
    testSame("var a = 1; var b = 2; var c = a + b; f(c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain22
  public void testRefChain22() {
    test("var a = 2; var b = a = 4; f(a)", "var a = 2; a = 4; f(a)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain23
  public void testRefChain23() {
    test("var a = {}; var b = a[1] || f()", "var a = {}; a[1] || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs
  public void testAssignmentWithComplexLhs() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().x_ = o['key'];");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs2
  public void testAssignmentWithComplexLhs2() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().foo = function() {" +
             "  o" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs3
  public void testAssignmentWithComplexLhs3() {
    String source =
        "var o = {'key': 'val'};" +
        "function init_() {" +
        "  this.x = o['key']" +
        "}";

    test(source, "");
    testSame(source + ";init_()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs4
  public void testAssignmentWithComplexLhs4() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().foo = function() {" +
             "  this.x = o['key']" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope1
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope1() {
    testSame("function f(arg){}" +
             "" +
             "(function(){" +
             "  var O = {};" +
             "  O.prototype = 'foo';" +
             "  f(O);" +
             "})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope2
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope2() {
    testSame("function f(arg){}" +
             "(function h(){" +
             "  var L = {};" +
             "  L.prototype = 'foo';" +
             "  f(L);" +
             "})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope4
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope4() {
    testSame("function f(arg){}" +
             "function g(){" +
             "  var N = {};" +
             "  N.prototype = 'foo';" +
             "  f(N);" +
             "}" +
             "g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope5
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope5() {
    
    testSame("function g(){ var R = {}; R.prototype = 'foo' } g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeDefinitionsInGlobalScope1
  public void testRemovePrototypeDefinitionsInGlobalScope1() {
    testSame("function f(arg){}" +
             "var M = {};" +
             "M.prototype = 'foo';" +
             "f(M);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeDefinitionsInGlobalScope2
  public void testRemovePrototypeDefinitionsInGlobalScope2() {
    test("var Q = {}; Q.prototype = 'foo'", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment
  public void testRemoveLabeledStatment() {
    test("LBL: var x = 1;", "LBL: {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment2
  public void testRemoveLabeledStatment2() {
    test("var x; LBL: x = f() + g()", "LBL: { f() ; g()}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment3
  public void testRemoveLabeledStatment3() {
    test("var x; LBL: x = 1;", "LBL: {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment4
  public void testRemoveLabeledStatment4() {
    test("var a; LBL: a = f()", "LBL: f()");
  }

// com.google.javascript.jscomp.NodeTypeNormalizerTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getNext();

    Node secondKey = firstVal.getNext();
    Node secondVal = secondKey.getNext();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
  }

// com.google.javascript.jscomp.NormalizeTest::testSplitVar
  public void testSplitVar() {
    testSame("var a");
    test("var a, b",
         "var a; var b");
    test("var a, b, c",
         "var a; var b; var c");
    testSame("var a = 0 ");
    test("var a = 0 , b = foo()",
         "var a = 0; var b = foo()");
    test("var a = 0, b = 1, c = 2",
         "var a = 0; var b = 1; var c = 2");
    test("var a = foo(1), b = foo(2), c = foo(3)",
         "var a = foo(1); var b = foo(2); var c = foo(3)");

    
    test("for(var a = 0, b = foo(1), c = 1; c < b; c++) foo(2)",
         "var a = 0; var b = foo(1); var c = 1; for(; c < b; c++) foo(2)");

    
    test("for(;;) var b = foo(1), c = foo(2);",
        "for(;;){var b = foo(1); var c = foo(2)}");
    test("for(;;){var b = foo(1), c = foo(2);}",
         "for(;;){var b = foo(1); var c = foo(2)}");

    test("try{var b = foo(1), c = foo(2);} finally foo(3);",
         "try{var b = foo(1); var c = foo(2)} finally foo(3);");
    test("try{var b = foo(1),c = foo(2);} finally;",
         "try{var b = foo(1); var c = foo(2)} finally;");
    test("try{foo(0);} finally var b = foo(1), c = foo(2);",
         "try{foo(0);} finally {var b = foo(1); var c = foo(2)}");

    test("switch(a) {default: var b = foo(1), c = foo(2); break;}",
         "switch(a) {default: var b = foo(1); var c = foo(2); break;}");

    test("do var a = foo(1), b; while(false);",
         "do{var a = foo(1); var b} while(false);");
  }

// com.google.javascript.jscomp.NormalizeTest::testUnhandled
  public void testUnhandled() {
    testSame("var x = y = 1");
  }

// com.google.javascript.jscomp.NormalizeTest::testFor
  public void testFor() {
    
    test("for(a = 0; a < 2 ; a++) foo();",
         "a = 0; for(; a < 2 ; a++) foo()");
    
    test("for(var a = 0; c < b ; c++) foo()",
         "var a = 0; for(; c < b ; c++) foo()");

    
    test("a:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:for(; c < b ; c++) foo()");
    
    test("a:b:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:b:for(; c < b ; c++) foo()");

    
    test("if(x) for(var a = 0; c < b ; c++) foo()",
         "if(x){var a = 0; for(; c < b ; c++) foo()}");

    
    test("for(init(); a < 2 ; a++) foo();",
         "init(); for(; a < 2 ; a++) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testWhile
  public void testWhile() {
    
    test("while(c < b) foo()",
         "for(; c < b;) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions1
  public void testMoveFunctions1() throws Exception {
    test("function f() { if (x) return; foo(); function foo() {} }",
         "function f() {function foo() {} if (x) return; foo(); }");
    test("function f() { " +
            "function foo() {} " +
            "if (x) return;" +
            "foo(); " +
            "function bar() {} " +
         "}",
         "function f() {" +
           "function foo() {}" +
           "function bar() {}" +
           "if (x) return;" +
           "foo();" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions2
  public void testMoveFunctions2() throws Exception {
    testSame("function f() { function foo() {} }");
    testSame("function f() { f(); a:function bar() {} }");
    testSame("function f() { f(); {function bar() {}}}");
    testSame("function f() { f(); if (true) {function bar() {}}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMakeLocalNamesUnique
  public void testMakeLocalNamesUnique() {
    if (!Normalize.MAKE_LOCAL_NAMES_UNIQUE) {
      return;
    }

    
    testSame("var a;");

    
    testSame("a;");

    
    test("var a;function foo(a){var b;a}",
         "var a;function foo(a$$1){var b;a$$1}");
    test("var a;function foo(){var b;a}function boo(){var b;a}",
         "var a;function foo(){var b;a}function boo(){var b$$1;a}");
    test("function foo(a){var b}" +
         "function boo(a){var b}",
         "function foo(a){var b}" +
         "function boo(a$$1){var b$$1}");

    
    test("var a = function foo(){foo()};var b = function foo(){foo()};",
         "var a = function foo(){foo()};var b = function foo$$1(){foo$$1()};");

    
    test("try { } catch(e) {e;}",
         "try { } catch(e) {e;}");
    test("try { } catch(e) {e;}; try { } catch(e) {e;}",
         "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    test("try { } catch(e) {e; try { } catch(e) {e;}};",
         "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations
  public void testRemoveDuplicateVarDeclarations() {
    test("function f() { var a; var a }",
         "function f() { var a; }");
    test("function f() { var a = 1; var a = 2 }",
         "function f() { var a = 1; a = 2 }");
    test("var a = 1; function f(){ var a = 2 }",
         "var a = 1; function f(){ var a$$1 = 2 }");
    test("function f() { var a = 1; lable1:var a = 2 }",
         "function f() { var a = 1; lable1:a = 2 }");
    test("function f() { var a = 1; lable1:var a }",
         "function f() { var a = 1; lable1:; }");
    test("function f() { var a = 1; for(var a in b); }",
         "function f() { var a = 1; for(a in b); }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstants
  public void testRenamingConstants() {
    test("var ACONST = 4;var b = ACONST;",
         "var ACONST$$constant = 4; var b = ACONST$$constant;");

    test("var a, ACONST = 4;var b = ACONST;",
         "var a; var ACONST$$constant = 4; var b = ACONST$$constant;");

    test("var ACONST; ACONST = 4; var b = ACONST;",
         "var ACONST$$constant; ACONST$$constant = 4;" +
         "var b = ACONST$$constant;");

    test("var ACONST = new Foo(); var b = ACONST;",
         "var ACONST$$constant = new Foo(); var b = ACONST$$constant;");

    test("var aa; aa=1;", "var aa$$constant;aa$$constant=1");
  }

// com.google.javascript.jscomp.NormalizeTest::testSkipRenamingExterns
  public void testSkipRenamingExterns() {
    test("var EXTERN; var ext; ext.FOO;", "var b = EXTERN; var c = ext.FOO",
         "var b = EXTERN; var c = ext.FOO", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoRemoval
  public void testNoRemoval() {
    testSame("function foo(p1) { } foo(1); foo(2)");
    testSame("function foo(p1) { } foo(1,2); foo(3,4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNotAFunction
  public void testNotAFunction() {
    testSame("var x = 1; x; x = 2");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalNamedFunction
  public void testRemoveOneOptionalNamedFunction() {
    test("function foo(p1) { } foo()", "function foo() {var p1} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    test("var foo; foo = function (p1) { }; foo()",
        "var foo; foo = function () {var p1}; foo()");
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

    String expected = "var goog = {};" +
        "goog.foo = function (p1) { var p2 };" +
        "goog.foo = function (q1) { var q2 };" +
        "goog.foo = function (r1) { var r2 };" +
        "goog.foo(1); goog.foo(2); goog.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveTwoOptionalMultiplePossibleDefinition
  public void testRemoveTwoOptionalMultiplePossibleDefinition() {
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2, p3, p4) { };" +
        "goog.foo = function (q1, q2, q3, q4) { };" +
        "goog.foo = function (r1, r2, r3, r4) { };" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";

    String expected = "var goog = {};" +
        "goog.foo = function(p1, p2) { var p4; var p3};" +
        "goog.foo = function(q1, q2) { var q4; var q3};" +
        "goog.foo = function(r1, r2) { var r4; var r3};" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";
    test(src, expected);
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

// com.google.javascript.jscomp.OptimizeParametersTest::testUnknown
  public void testUnknown() {
    String src = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected = "var goog1 = {};" +
        "goog1.foo = function () { };" +
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
