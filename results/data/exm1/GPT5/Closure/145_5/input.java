// buggy code
  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
          // Either a empty statement or an block with more than one child,
          // way it isn't a FUNCTION or DO.
      return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }

// relevant test
// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossIncrement
  public void testDoNotCrossIncrement() {
    testSame("var a = b.c; b.c++; var d = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossConstructor
  public void testDoNotCrossConstructor() {
    testSame("var a = b; new Foo(); var c = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossVar
  public void testDoCrossVar() {
    
    test("var a = b; var b = 3; alert(a)", "alert(3);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOverlappingInlines
  public void testOverlappingInlines() {
    String source =
        "a = function(el, x, opt_y) { " +
        "  var cur = bar(el); " +
        "  opt_y = x.y; " +
        "  x = x.x; " +
        "  var dx = x - cur.x; " +
        "  var dy = opt_y - cur.y;" +
        "  foo(el, el.offsetLeft + dx, el.offsetTop + dy); " +
        "};";
    String expected =
      "a = function(el, x, opt_y) { " +
      "  var cur = bar(el); " +
      "  opt_y = x.y; " +
      "  x = x.x; " +
      "  foo(el, el.offsetLeft + (x - cur.x)," +
      "      el.offsetTop + (opt_y - cur.y)); " +
      "};";

    test(source, expected);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOverlappingInlineFunctions
  public void testOverlappingInlineFunctions() {
    String source =
        "a = function() { " +
        "  var b = function(args) {var n;}; " +
        "  var c = function(args) {}; " +
        "  d(b,c); " +
        "};";
    String expected =
      "a = function() { " +
      "  d(function(args){var n;}, function(args){}); " +
      "};";

    test(source, expected);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoLoops
  public void testInlineIntoLoops() {
    test("var x = true; while (true) alert(x);",
         "while (true) alert(true);");
    test("var x = true; while (true) for (var i in {}) alert(x);",
         "while (true) for (var i in {}) alert(true);");
    testSame("var x = [true]; while (true) alert(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoFunction
  public void testInlineIntoFunction() {
    test("var x = false; var f = function() { alert(x); };",
         "var f = function() { alert(false); };");
    testSame("var x = [false]; var f = function() { alert(x); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineIntoNamedFunction
  public void testNoInlineIntoNamedFunction() {
    testSame("f(); var x = false; function f() { alert(x); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoNestedNonHoistedNamedFunctions
  public void testInlineIntoNestedNonHoistedNamedFunctions() {
    test("f(); var x = false; if (false) function f() { alert(x); };",
         "f(); if (false) function f() { alert(false); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineIntoNestedNamedFunctions
  public void testNoInlineIntoNestedNamedFunctions() {
    testSame("f(); var x = false; function f() { if (false) { alert(x); } };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineMutatedVariable
  public void testNoInlineMutatedVariable() {
    testSame("var x = false; if (true) { var y = x; x = true; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineImmutableMultipleTimes
  public void testInlineImmutableMultipleTimes() {
    test("var x = null; var y = x, z = x;",
         "var y = null, z = null;");
    test("var x = 3; var y = x, z = x;",
         "var y = 3, z = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineStringMultipleTimesIfNotWorthwhile
  public void testNoInlineStringMultipleTimesIfNotWorthwhile() {
    testSame("var x = 'abcdefghijklmnopqrstuvwxyz'; var y = x, z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineStringMultipleTimesWhenAliasingAllStrings
  public void testInlineStringMultipleTimesWhenAliasingAllStrings() {
    inlineAllStrings = true;
    test("var x = 'abcdefghijklmnopqrstuvwxyz'; var y = x, z = x;",
         "var y = 'abcdefghijklmnopqrstuvwxyz', " +
         "    z = 'abcdefghijklmnopqrstuvwxyz';");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineBackwards
  public void testNoInlineBackwards() {
    testSame("var y = x; var x = null;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineOutOfBranch
  public void testNoInlineOutOfBranch() {
    testSame("if (true) var x = null; var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInterferingInlines
  public void testInterferingInlines() {
    test("var a = 3; var f = function() { var x = a; alert(x); };",
         "var f = function() { alert(3); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoTryCatch
  public void testInlineIntoTryCatch() {
    test("var a = true; " +
         "try { var b = a; } " +
         "catch (e) { var c = a + b; var d = true; } " +
         "finally { var f = a + b + c + d; }",
         "try { var b = true; } " +
         "catch (e) { var c = true + b; var d = true; } " +
         "finally { var f = true + b + c + d; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstants
  public void testInlineConstants() {
    test("function foo() { return XXX; } var XXX = true;",
         "function foo() { return true; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineStringWhenWorthwhile
  public void testInlineStringWhenWorthwhile() {
    test("var x = 'a'; foo(x, x, x);", "foo('a', 'a', 'a');");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAlias
  public void testInlineConstantAlias() {
    test("var XXX = new Foo(); q(XXX); var YYY = XXX; bar(YYY)",
         "var XXX = new Foo(); q(XXX); bar(XXX)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAliasWithAnnotation
  public void testInlineConstantAliasWithAnnotation() {
    test(" var xxx = new Foo(); q(xxx); var YYY = xxx; bar(YYY)",
         " var xxx = new Foo(); q(xxx); bar(xxx)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAliasWithNonConstant
  public void testInlineConstantAliasWithNonConstant() {
    test("var XXX = new Foo(); q(XXX); var y = XXX; bar(y); baz(y)",
         "var XXX = new Foo(); q(XXX); bar(XXX); baz(XXX)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testCascadingInlines
  public void testCascadingInlines() {
    test("var XXX = 4; " +
         "function f() { var YYY = XXX; bar(YYY); baz(YYY); }",
         "function f() { bar(4); baz(4); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineGetpropIntoCall
  public void testNoInlineGetpropIntoCall() {
    test("var a = b; a();", "b();");
    test("var a = b.c; f(a);", "f(b.c);");
    testSame("var a = b.c; a();");
  }

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

// com.google.javascript.jscomp.InlineVariablesTest::test2388531
  public void test2388531() {
    testSame("var f = function () {};" +
             "var g = function () {};" +
             "goog.inherits(f, g);");
    testSame("var f = function () {};" +
             "var g = function () {};" +
             "goog$inherits(f, g);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("var x = 0; (function x() { return x ? x() : 3; })();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    testSame("function y() { return y(); }");
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

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliasesInLoop
  public void testInlineAliasesInLoop() {
    test(
        "function f() { " +
        "  var x = extern();" +
        "  for (var i = 0; i < 5; i++) {" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "  }" +
        "}",
        "function f() { " +
        "  var x = extern();" +
        "  for (var i = 0; i < 5; i++) {" +
        "    (function() {" +
        "       window.setTimeout(function() { extern(x); }, 0);" +
        "     })();" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliasesInLoop
  public void testNoInlineAliasesInLoop() {
    testSame(
        "function f() { " +
        "  for (var i = 0; i < 5; i++) {" +
        "    var x = extern();" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "  }" +
        "}");
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

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1a
  public void testInlineFunctionAlias1a() {
    test(
      "function f(x) {}" +
      "var y = f;" +
      "g();" +
      "y();y();",
      "var y = function f(x) {};" +
      "g();" +
      "y();y();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1b
  public void testInlineFunctionAlias1b() {
    test(
      "function f(x) {};" +
      "f;var y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f;g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2a
  public void testInlineFunctionAlias2a() {
    test(
      "function f(x) {}" +
      "var y; y = f;" +
      "g();" +
      "y();y();",
      "var y; y = function f(x) {};" +
      "g();" +
      "y();y();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2b
  public void testInlineFunctionAlias2b() {
    test(
      "function f(x) {};" +
      "f; var y; y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f; f;" +
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

// com.google.javascript.jscomp.InlineVariablesTest::testLocalsOnly1
  public void testLocalsOnly1() {
    inlineLocalsOnly = true;
    test(
        "var x=1; x; function f() {var x = 1; x;}",
        "var x=1; x; function f() {1;}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testLocalsOnly2
  public void testLocalsOnly2() {
    inlineLocalsOnly = true;
    test(
        "\n" +
        "var X=1; X;\n" +
        "function f() {\n" +
        "  \n" +
        "  var X = 1; X;\n" +
        "}",
        "var X=1; X; function f() {1;}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined1
  public void testInlineUndefined1() {
    test("var x; x;",
         "void 0;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined2
  public void testInlineUndefined2() {
    testSame("var x; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined3
  public void testInlineUndefined3() {
    testSame("var x; var x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined4
  public void testInlineUndefined4() {
    test("var x; x; x;",
         "void 0; void 0;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined5
  public void testInlineUndefined5() {
    test("var x; for(x in a) {}",
         "var x; for(x in a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue90
  public void testIssue90() {
    test("var x; x && alert(1)",
         "void 0 && alert(1)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("var JSCompiler_renameProperty; " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testThisAlias
  public void testThisAlias() {
    test("function f() { var a = this; a.y(); a.z(); }",
         "function f() { this.y(); this.z(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testThisEscapedAlias
  public void testThisEscapedAlias() {
    testSame(
        "function f() { var a = this; var g = function() { a.y(); }; a.z(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineNamedFunction
  public void testInlineNamedFunction() {
    test("function f() {} f();", "(function f(){})()");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInstrument
  public void testInstrument() {
    final String kPreamble =
        "var $$toRemoveDefinition1, $$notToRemove;\n" +
        "var $$toRemoveDefinition2, $$toRemoveDefinition3;\n";

    
    
    List<String> initCodeList = ImmutableList.of(
        "var $$Table = [];",
        "function $$TestDefine(id) {",
        "  $$Table[id] = 0;",
        "};",
        "function $$TestInstrument(id) {",
        "  $$Table[id]++;",
        "};");
    StringBuilder initCodeBuilder = new StringBuilder();
    StringBuilder pbBuilder = new StringBuilder();
    for (String line : initCodeList) {
      initCodeBuilder.append(line).append("\n");
      pbBuilder.append("init: \"").append(line).append("\"\n");
    }

    pbBuilder.append("report_call: \"$$testInstrument\"")
        .append("report_defined: \"$$testDefine\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition1\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition2\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition3\"");

    final String initCode = initCodeBuilder.toString();
    this.instrumentationPb = pbBuilder.toString();

    
    test("function a(){b}",
         initCode + "$$testDefine(0);" +
         "function a(){$$testInstrument(0);b}");

    
    test(kPreamble + "function a(){b}",
         initCode +
         "$$testDefine(0);" +
         "var $$notToRemove;" +
         "function a(){$$testInstrument(0);b}");

    
    test(kPreamble + "var a = { b: function(){c} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "var a = { b: function(){$$testInstrument(0);c} }");

    
    test(kPreamble +
         "var a = { b: function(){c}, d: function(){e} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "$$testDefine(1);" +
         "var a={b:function(){$$testInstrument(0);c}," +
         "d:function(){$$testInstrument(1);e}}");

    
    test(kPreamble +
         "var a = { b: { f: function(){c} }, d: function(){e} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "$$testDefine(1);" +
         "var a={b:{f:function(){$$testInstrument(0);c}}," +
         "d:function(){$$testInstrument(1);e}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testEmpty
  public void testEmpty() {
    this.instrumentationPb = "";
    test("function a(){b}", "function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testAppNameSetter
  public void testAppNameSetter() {
    this.instrumentationPb = "app_name_setter: \"setAppName\"";
    test("function a(){b}", "setAppName(\"testfile.js\");function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInit
  public void testInit() {
    this.instrumentationPb = "init: \"var foo = 0;\"\n" +
        "init: \"function f(){g();}\"\n";
    test("function a(){b}",
         "var foo = 0;function f(){g()}function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testDeclare
  public void testDeclare() {
    this.instrumentationPb = "report_defined: \"$$testDefine\"";
    test("function a(){b}", "$$testDefine(0);function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testCall
  public void testCall() {
    this.instrumentationPb = "report_call: \"$$testCall\"";
    test("function a(){b}", "function a(){$$testCall(0);b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNested
  public void testNested() {
    this.instrumentationPb = "report_call: \"$$testCall\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ function b(){}}",
         "$$testDefine(1);$$testDefine(0);" +
         "function a(){$$testCall(1);function b(){$$testCall(0)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitPaths
  public void testExitPaths() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){return}",
         "function a(){return $$testExit(0)}");

    test("function b(){return 5}",
         "function b(){return $$testExit(0, 5)}");

    test("function a(){if(2 != 3){return}else{return 5}}",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}");

    test("function a(){if(2 != 3){return}else{return 5}}b()",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}b()");

    test("function a(){if(2 != 3){return}else{return 5}}",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitNoReturn
  public void testExitNoReturn() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){}",
         "function a(){$$testExit(0);}");

    test("function a(){b()}",
         "function a(){b();$$testExit(0);}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testPartialExitPaths
  public void testPartialExitPaths() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){if (2 != 3) {return}}",
         "function a(){if (2 != 3){return $$testExit(0)}$$testExit(0)}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitTry
  public void testExitTry() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){try{return}catch(err){}}",
         "function a(){try{return $$testExit(0)}catch(err){}$$testExit(0)}");

    test("function a(){try{}catch(err){return}}",
         "function a(){try{}catch(err){return $$testExit(0)}$$testExit(0)}");

    test("function a(){try{return}finally{}}",
         "function a(){try{return $$testExit(0)}finally{}$$testExit(0)}");

    test("function a(){try{return}catch(err){}finally{}}",
         "function a(){try{return $$testExit(0)}catch(err){}finally{}" +
         "$$testExit(0)}");

    test("function a(){try{return 1}catch(err){return 2}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}}");

    test("function a(){try{return 1}catch(err){return 2}finally{}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}" +
         "finally{}$$testExit(0)}");

    test("function a(){try{return 1}catch(err){return 2}finally{return}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}finally{return $$testExit(0)}}");

    test("function a(){try{}catch(err){}finally{return}}",
         "function a(){try{}catch(err){}finally{return $$testExit(0)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testProtobuffParseFail
  public void testProtobuffParseFail() {
    this.instrumentationPb = "not an ascii pb\n";
    test("function a(){b}", "", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInitJsParseFail
  public void testInitJsParseFail() {
    this.instrumentationPb = "init: \"= assignWithNoLhs();\"";
    test("function a(){b}", "", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testWithInversion
  public void testWithInversion(String original, String expected) {
    invert = false;
    test(original, expected);
    invert = true;
    test(expected, original);
    invert = false;
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testSameWithInversion
  public void testSameWithInversion(String externs, String original) {
    invert = false;
    testSame(externs, original, null);
    invert = true;
    testSame(externs, original, null);
    invert = false;
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testSameWithInversion
  public void testSameWithInversion(String original) {
    testSameWithInversion("", original);
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testInFunction
  public void testInFunction(String original, String expected) {
    test(wrapInFunction(original), wrapInFunction(expected));
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testSameInFunction
  public void testSameInFunction(String original) {
    testSame(wrapInFunction(original));
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext1
  public void testMakeLocalNamesUniqueWithContext1() {
    
    this.useDefaultRenamer = true;

    invert = true;
    test(
        "var a;function foo(){var a$$inline_1; a = 1}",
        "var a;function foo(){var a$$0; a = 1}");
    test(
        "var a;function foo(){var a$$inline_1;}",
        "var a;function foo(){var a;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext2
  public void testMakeLocalNamesUniqueWithContext2() {
    
    this.useDefaultRenamer = true;

    
    testSameWithInversion("var a;");

    
    testSameWithInversion("a;");

    
    testWithInversion(
        "var a;function foo(a){var b;a}",
        "var a;function foo(a$$1){var b;a$$1}");
    testWithInversion(
        "var a;function foo(){var b;a}function boo(){var b;a}",
         "var a;function foo(){var b;a}function boo(){var b$$1;a}");
    testWithInversion(
        "function foo(a){var b}" +
         "function boo(a){var b}",
         "function foo(a){var b}" +
         "function boo(a$$1){var b$$1}");

    
    testWithInversion(
        "var a = function foo(){foo()};var b = function foo(){foo()};",
        "var a = function foo(){foo()};var b = function foo$$1(){foo$$1()};");

    
    testWithInversion(
        "try { } catch(e) {e;}",
         "try { } catch(e) {e;}");

    
    test(
        "try { } catch(e) {e;}; try { } catch(e) {e;}",
        "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    test(
        "try { } catch(e) {e; try { } catch(e) {e;}};",
        "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext3
  public void testMakeLocalNamesUniqueWithContext3() {
    
    this.useDefaultRenamer = true;

    String externs = "var extern1 = {};";

    
    testSameWithInversion(externs, "var extern1 = extern1 || {};");

    
    testSame(externs, "var extern1 = extern1 || {};", null);
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext4
  public void testMakeLocalNamesUniqueWithContext4() {
    
    this.useDefaultRenamer = true;

    
    testInFunction(
        "var e; try { } catch(e) {e;}; try { } catch(e) {e;}",
        "var e; try { } catch(e$$1) {e$$1;}; try { } catch(e$$2) {e$$2;}");
    testInFunction(
        "var e; try { } catch(e) {e; try { } catch(e) {e;}}",
        "var e; try { } catch(e$$1) {e$$1; try { } catch(e$$2) {e$$2;} }");
    testInFunction(
        "try { } catch(e) {e;}; try { } catch(e) {e;} var e;",
        "try { } catch(e$$1) {e$$1;}; try { } catch(e$$2) {e$$2;} var e;");
    testInFunction(
        "try { } catch(e) {e; try { } catch(e) {e;}} var e;",
        "try { } catch(e$$1) {e$$1; try { } catch(e$$2) {e$$2;} } var e;");

    invert = true;

    testInFunction(
        "var e; try { } catch(e$$0) {e$$0;}; try { } catch(e$$1) {e$$1;}",
        "var e; try { } catch(e$$2) {e$$2;}; try { } catch(e$$0) {e$$0;}");
    testInFunction(
        "var e; try { } catch(e$$1) {e$$1; try { } catch(e$$2) {e$$2;} };",
        "var e; try { } catch(e$$0) {e$$0; try { } catch(e$$1) {e$$1;} };");
    testInFunction(
        "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;};var e$$2;",
        "try { } catch(e) {e;}; try { } catch(e$$0) {e$$0;};var e$$1;");
    testInFunction(
        "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} };var e$$2",
        "try { } catch(e) {e; try { } catch(e$$0) {e$$0;} };var e$$1");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testArguments
  public void testArguments() {
    
    this.useDefaultRenamer = true;

    
    testSameWithInversion(
        "function foo(){var arguments;function bar(){var arguments;}}");

    invert = true;
    
    
    test(
        "function foo(){var arguments$$1;}",
        "function foo(){var arguments$$0;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithoutContext
  public void testMakeLocalNamesUniqueWithoutContext() {
    
    this.useDefaultRenamer = false;

    test("var a;",
         "var a$$unique_0");

    
    testSame("a;");

    
    test("var a;" +
         "function foo(a){var b;a}",
         "var a$$unique_0;" +
         "function foo$$unique_1(a$$unique_2){var b$$unique_3;a$$unique_2}");
    test("var a;" +
         "function foo(){var b;a}" +
         "function boo(){var b;a}",
         "var a$$unique_0;" +
         "function foo$$unique_1(){var b$$unique_3;a$$unique_0}" +
         "function boo$$unique_2(){var b$$unique_4;a$$unique_0}");

    
    test("var a = function foo(){foo()};",
         "var a$$unique_0 = function foo$$unique_1(){foo$$unique_1()};");

    
    test("try { } catch(e) {e;}",
         "try { } catch(e$$unique_0) {e$$unique_0;}");
    test("try { } catch(e) {e;};" +
         "try { } catch(e) {e;}",
         "try { } catch(e$$unique_0) {e$$unique_0;};" +
         "try { } catch(e$$unique_1) {e$$unique_1;}");
    test("try { } catch(e) {e; " +
         "try { } catch(e) {e;}};",
         "try { } catch(e$$unique_0) {e$$unique_0; " +
            "try { } catch(e$$unique_1) {e$$unique_1;} }; ");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion
  public void testOnlyInversion() {
    invert = true;
    test("function f(a, a$$1) {}",
         "function f(a, a$$0) {}");
    test("function f(a$$1, b$$2) {}",
         "function f(a, b) {}");
    test("function f(a$$1, a$$2) {}",
         "function f(a, a$$0) {}");
    testSame("try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    testSame("try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");
    testSame("var a$$1;");
    testSame("function f() { var $$; }");
    test("var CONST = 3; var b = CONST;",
         "var CONST = 3; var b = CONST;");
    test("function() {var CONST = 3; var ACONST$$1 = 2;}",
         "function() {var CONST = 3; var ACONST = 2;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion2
  public void testOnlyInversion2() {
    invert = true;
    test("function () {try { } catch(e) {e;}; try { } catch(e$$0) {e$$0;}}",
        "function () {try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion3
  public void testOnlyInversion3() {
    invert = true;
    test(
        "function x1() {" +
        "  var a$$1;" +
        "  function x2() {" +
        "    var a$$2;" +
        "  }" +
        "  function x3() {" +
        "    var a$$3;" +
        "  }" +
        "}",
        "function x1() {" +
        "  var a$$0;" +
        "  function x2() {" +
        "    var a;" +
        "  }" +
        "  function x3() {" +
        "    var a;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion4
  public void testOnlyInversion4() {
    invert = true;
    test(
        "function x1() {" +
        "  var a$$0;" +
        "  function x2() {" +
        "    var a;a$$0++" +
        "  }" +
        "}",
        "function x1() {" +
        "  var a$$1;" +
        "  function x2() {" +
        "    var a;a$$1++" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testConstRemovingRename1
  public void testConstRemovingRename1() {
    removeConst = true;
    test("function() {var CONST = 3; var ACONST$$1 = 2;}",
         "function() {var CONST$$unique_0 = 3; var ACONST$$unique_1 = 2;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testConstRemovingRename2
  public void testConstRemovingRename2() {
    removeConst = true;
    test("var CONST = 3; var b = CONST;",
         "var CONST$$unique_0 = 3; var b$$unique_1 = CONST$$unique_0;");
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

// com.google.javascript.jscomp.MinimizeExitPointsTest::testBreakOptimization
  public void testBreakOptimization() throws Exception {
    fold("f:{if(true){a();break f;}else;b();}",
         "f:{if(true){a()}else{b()}}");
    fold("f:{if(false){a();break f;}else;b();break f;}",
         "f:{if(false){a()}else{b()}}");
    fold("f:{if(a()){b();break f;}else;c();}",
         "f:{if(a()){b();}else{c();}}");
    fold("f:{if(a()){b()}else{c();break f;}}",
         "f:{if(a()){b()}else{c();}}");
    fold("f:{if(a()){b();break f;}else;}",
         "f:{if(a()){b();}else;}");
    fold("f:{if(a()){break f;}else;}",
         "f:{if(a()){}else;}");

    fold("f:while(a())break f;",
         "f:while(a())break f");
    foldSame("f:for(x in a())break f");

    fold("f:{while(a())break;}",
         "f:{while(a())break;}");
    foldSame("f:{for(x in a())break}");

    fold("f:try{break f;}catch(e){break f;}",
         "f:try{}catch(e){}");
    fold("f:try{if(a()){break f;}else{break f;} break f;}catch(e){}",
         "f:try{if(a()){}else{}}catch(e){}");

    fold("f:g:break f",
         "");
    fold("f:g:{if(a()){break f;}else{break f;} break f;}",
         "f:g:{if(a()){}else{}}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testFunctionReturnOptimization
  public void testFunctionReturnOptimization() throws Exception {
    fold("function(){if(a()){b();if(c())return;}}",
         "function(){if(a()){b();if(c());}}");
    fold("function(){if(x)return; x=3; return; }",
         "function(){if(x); else x=3}");
    fold("function(){if(true){a();return;}else;b();}",
         "function(){if(true){a();}else{b();}}");
    fold("function(){if(false){a();return;}else;b();return;}",
         "function(){if(false){a();}else{b();}}");
    fold("function(){if(a()){b();return;}else;c();}",
         "function(){if(a()){b();}else{c();}}");
    fold("function(){if(a()){b()}else{c();return;}}",
         "function(){if(a()){b()}else{c();}}");
    fold("function(){if(a()){b();return;}else;}",
         "function(){if(a()){b();}else;}");
    fold("function(){if(a()){return;}else{return;} return;}",
         "function(){if(a()){}else{}}");
    fold("function(){if(a()){return;}else{return;} b();}",
         "function(){if(a()){}else{return;b()}}");

    fold("function(){while(a())return;}",
         "function(){while(a())return}");
    foldSame("function(){for(x in a())return}");

    fold("function(){while(a())break;}",
         "function(){while(a())break}");
    foldSame("function(){for(x in a())break}");

    fold("function(){try{return;}catch(e){return;}finally{return}}",
         "function(){try{}catch(e){}finally{}}");
    fold("function(){try{return;}catch(e){return;}}",
         "function(){try{}catch(e){}}");
    fold("function(){try{return;}finally{return;}}",
         "function(){try{}finally{}}");
    fold("function(){try{if(a()){return;}else{return;} return;}catch(e){}}",
         "function(){try{if(a()){}else{}}catch(e){}}");

    fold("function(){g:return}",
         "function(){}");
    fold("function(){g:if(a()){return;}else{return;} return;}",
         "function(){g:if(a()){}else{}}");
    fold("function(){try{g:if(a()){} return;}finally{return}}",
         "function(){try{g:if(a()){}}finally{}}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testWhileContinueOptimization
  public void testWhileContinueOptimization() throws Exception {
    fold("while(true){if(x)continue; x=3; continue; }",
         "while(true)if(x);else x=3");
    foldSame("while(true){a();continue;b();}");
    fold("while(true){if(true){a();continue;}else;b();}",
         "while(true){if(true){a();}else{b()}}");
    fold("while(true){if(false){a();continue;}else;b();continue;}",
         "while(true){if(false){a()}else{b();}}");
    fold("while(true){if(a()){b();continue;}else;c();}",
         "while(true){if(a()){b();}else{c();}}");
    fold("while(true){if(a()){b();}else{c();continue;}}",
         "while(true){if(a()){b();}else{c();}}");
    fold("while(true){if(a()){b();continue;}else;}",
         "while(true){if(a()){b();}else;}");
    fold("while(true){if(a()){continue;}else{continue;} continue;}",
         "while(true){if(a()){}else{}}");
    fold("while(true){if(a()){continue;}else{continue;} b();}",
         "while(true){if(a()){}else{continue;b();}}");

    fold("while(true)while(a())continue;",
         "while(true)while(a());");
    fold("while(true)for(x in a())continue",
         "while(true)for(x in a());");

    fold("while(true)while(a())break;",
         "while(true)while(a())break");
    fold("while(true)for(x in a())break",
         "while(true)for(x in a())break");

    fold("while(true){try{continue;}catch(e){continue;}}",
         "while(true){try{}catch(e){}}");
    fold("while(true){try{if(a()){continue;}else{continue;}" +
         "continue;}catch(e){}}",
         "while(true){try{if(a()){}else{}}catch(e){}}");

    fold("while(true){g:continue}",
         "while(true){}");
    
    fold("while(true){g:if(a()){continue;}else{continue;} continue;}",
         "while(true){g:if(a());else;}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testDoContinueOptimization
  public void testDoContinueOptimization() throws Exception {
    fold("do{if(x)continue; x=3; continue; }while(true)",
         "do if(x); else x=3; while(true)");
    foldSame("do{a();continue;b()}while(true)");
    fold("do{if(true){a();continue;}else;b();}while(true)",
         "do{if(true){a();}else{b();}}while(true)");
    fold("do{if(false){a();continue;}else;b();continue;}while(true)",
         "do{if(false){a();}else{b();}}while(true)");
    fold("do{if(a()){b();continue;}else;c();}while(true)",
         "do{if(a()){b();}else{c()}}while(true)");
    fold("do{if(a()){b();}else{c();continue;}}while(true)",
         "do{if(a()){b();}else{c();}}while(true)");
    fold("do{if(a()){b();continue;}else;}while(true)",
         "do{if(a()){b();}else;}while(true)");
    fold("do{if(a()){continue;}else{continue;} continue;}while(true)",
         "do{if(a()){}else{}}while(true)");
    fold("do{if(a()){continue;}else{continue;} b();}while(true)",
         "do{if(a()){}else{continue; b();}}while(true)");

    fold("do{while(a())continue;}while(true)",
         "do while(a());while(true)");
    fold("do{for(x in a())continue}while(true)",
         "do for(x in a());while(true)");

    fold("do{while(a())break;}while(true)",
         "do while(a())break;while(true)");
    fold("do for(x in a())break;while(true)",
         "do for(x in a())break;while(true)");

    fold("do{try{continue;}catch(e){continue;}}while(true)",
         "do{try{}catch(e){}}while(true)");
    fold("do{try{if(a()){continue;}else{continue;}" +
         "continue;}catch(e){}}while(true)",
         "do{try{if(a()){}else{}}catch(e){}}while(true)");

    fold("do{g:continue}while(true)",
         "do{}while(true)");
    
    fold("do{g:if(a()){continue;}else{continue;} continue;}while(true)",
         "do{g:if(a());else;}while(true)");

    fold("do { foo(); continue; } while(false)",
         "do { foo(); } while(false)");
    fold("do { foo(); break; } while(false)",
         "do { foo(); } while(false)");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testForContinueOptimization
  public void testForContinueOptimization() throws Exception {
    fold("for(x in y){if(x)continue; x=3; continue; }",
         "for(x in y)if(x);else x=3");
    foldSame("for(x in y){a();continue;b()}");
    fold("for(x in y){if(true){a();continue;}else;b();}",
         "for(x in y){if(true)a();else b();}");
    fold("for(x in y){if(false){a();continue;}else;b();continue;}",
         "for(x in y){if(false){a();}else{b()}}");
    fold("for(x in y){if(a()){b();continue;}else;c();}",
         "for(x in y){if(a()){b();}else{c();}}");
    fold("for(x in y){if(a()){b();}else{c();continue;}}",
         "for(x in y){if(a()){b();}else{c();}}");
    fold("for(x=0;x<y;x++){if(a()){b();continue;}else;}",
         "for(x=0;x<y;x++){if(a()){b();}else;}");
    fold("for(x=0;x<y;x++){if(a()){continue;}else{continue;} continue;}",
         "for(x=0;x<y;x++){if(a()){}else{}}");
    fold("for(x=0;x<y;x++){if(a()){continue;}else{continue;} b();}",
         "for(x=0;x<y;x++){if(a()){}else{continue; b();}}");

    fold("for(x=0;x<y;x++)while(a())continue;",
         "for(x=0;x<y;x++)while(a());");
    fold("for(x=0;x<y;x++)for(x in a())continue",
         "for(x=0;x<y;x++)for(x in a());");

    fold("for(x=0;x<y;x++)while(a())break;",
         "for(x=0;x<y;x++)while(a())break");
    foldSame("for(x=0;x<y;x++)for(x in a())break");

    fold("for(x=0;x<y;x++){try{continue;}catch(e){continue;}}",
         "for(x=0;x<y;x++){try{}catch(e){}}");
    fold("for(x=0;x<y;x++){try{if(a()){continue;}else{continue;}" +
         "continue;}catch(e){}}",
         "for(x=0;x<y;x++){try{if(a()){}else{}}catch(e){}}");

    fold("for(x=0;x<y;x++){g:continue}",
         "for(x=0;x<y;x++){}");
    
    fold("for(x=0;x<y;x++){g:if(a()){continue;}else{continue;} continue;}",
         "for(x=0;x<y;x++){g:if(a());else;}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testCodeMotionDoesntBreakFunctionHoisting
  public void testCodeMotionDoesntBreakFunctionHoisting() throws Exception {
    fold("function f() { if (x) return; foo(); function foo() {} }",
         "function f() { if (x); else { function foo() {} foo(); } }");
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testFunctionDeclarations
  public void testFunctionDeclarations() {
    test("a; function f(){} function g(){}", "function f(){} function g(){} a");
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testFunctionDeclarationsInModule
  public void testFunctionDeclarationsInModule() {
    test(createModules("a; function f(){} function g(){}"),
         new String[] { "function f(){} function g(){} a" });
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testFunctionsExpression
  public void testFunctionsExpression() {
    testSame("a; f = function(){}");
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testNoMoveDeepFunctionDeclarations
  public void testNoMoveDeepFunctionDeclarations() {
    testSame("a; if (a) function f(){};");
    testSame("a; if (a) { function f(){} }");
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
        " var window;" +
        "window['a'].prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testBug2099540
  public void testBug2099540() {
    testSame(
        " var document;\n" +
        " var window;\n" +
        "var klass;\n" +
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
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
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

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct9
  public void testSetterInForStruct9() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct10
  public void testSetterInForStruct10() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i = 2);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct11
  public void testSetterInForStruct11() {
    test("var Class = function() {}; " +
         "for (;Class.property_ = 0;);",
         "for (;0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct12
  public void testSetterInForStruct12() {
    test("var a = 1; var Class = function() {}; " +
         "for (;Class.property_ = a;);",
         "var a = 1; for (; a;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct13
  public void testSetterInForStruct13() {
    test("var a = 1; var Class = function() {}; " +
         "for (Class.property_ = a; 0 ;);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct14
  public void testSetterInForStruct14() {
    test("var a = 1; var Class = function() {}; " +
         "for (; 0; Class.property_ = a);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct15
  public void testSetterInForStruct15() {
    test("var Class = function() {}; " +
         "for (var i = 1; 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct16
  public void testSetterInForStruct16() {
    test("var Class = function() {}; " +
         "for (var i = 1; i = 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn1
  public void testSetterInForIn1() {
    test("var foo = {}; var bar; for(e in bar = foo.a);",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn2
  public void testSetterInForIn2() {
    testSame("var foo = {}; var bar; for(e in bar = foo.a); bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn3
  public void testSetterInForIn3() {
    
    
    test("var foo = {}; var bar; for(e in bar = foo.a); bar.b = 3",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn4
  public void testSetterInForIn4() {
    
    
    test("var foo = {}; var bar; for (e in bar = foo.a); bar.b = 3; foo.a",
         "var foo = {}; for (e in foo.a); foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn5
  public void testSetterInForIn5() {
    
    
    test("var foo = {}; var bar; for (e in foo.a) { bar = e } bar.b = 3; foo.a",
         "var foo={};for(e in foo.a);foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn6
  public void testSetterInForIn6() {
    testSame("var foo = {};for(e in foo);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInIfPredicate
  public void testSetterInIfPredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "if (Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInWhilePredicate
  public void testSetterInWhilePredicate() {
    test("var a = 1;" +
         "var Class = function() {}; " +
         "while (Class.property_ = a);",
         "var a = 1; for (;a;) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInDoWhilePredicate
  public void testSetterInDoWhilePredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "do {} while(Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInSwitchInput
  public void testSetterInSwitchInput() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "switch (Class.property_ = a) {" +
             "  default:" +
             "}");
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
