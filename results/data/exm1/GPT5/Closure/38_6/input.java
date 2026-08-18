// buggy code
  void addNumber(double x) {
    // This is not pretty printing. This is to prevent misparsing of x- -4 as
    // x--4 (which is a syntax error).
    char prev = getLastChar();
    boolean negativeZero = isNegativeZero(x);
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x && !negativeZero) {
      long value = (long) x;
      long mantissa = value;
      int exp = 0;
      if (Math.abs(x) >= 100) {
        while (mantissa / 10 * Math.pow(10, exp + 1) == value) {
          mantissa /= 10;
          exp++;
        }
      }
      if (exp > 2) {
        add(Long.toString(mantissa) + "E" + Integer.toString(exp));
      } else {
        add(Long.toString(value));
      }
    } else {
      add(String.valueOf(x));
    }
  }

// relevant test
// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConstantInline
  public void testConstantInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return 3};",
        "var f=new Foo;var x=f.bar()",
        "var f=new Foo;var x=3");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConstantArrayInline
  public void testConstantArrayInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return[3,4]};",
        "var f=new Foo;var x=f.bar()",
        "var f=new Foo;var x=[3,4]");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConstantInlineWithSideEffects
  public void testConstantInlineWithSideEffects() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return 3};",
        "var x=(new Foo).bar()",
        "var x=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInline
  public void testEmptyMethodInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(a){};",
        "var x=new Foo; x.bar();",
        "var x=new Foo");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInlineWithSideEffects
  public void testEmptyMethodInlineWithSideEffects() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){};",
        "(new Foo).bar();var y=new Foo;y.bar(new Foo)",
        "(new Foo).bar();var y=new Foo;y.bar(new Foo)");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInlineInAssign1
  public void testEmptyMethodInlineInAssign1() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){};",
        "var x=new Foo;var y=x.bar()",
        "var x=new Foo;var y=void 0");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInlineInAssign2
  public void testEmptyMethodInlineInAssign2() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){};",
        "var x=new Foo;var y=x.bar().toString()",
        "var x=new Foo;var y=(void 0).toString()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNormalMethod
  public void testNormalMethod() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){var x=1};",
        "var x=new Foo;x.bar()",
        "var x=new Foo;x.bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfExternMethods1
  public void testNoInlineOfExternMethods1() {
    testSame("var external={};external.charAt;",
        "external.charAt()", (DiagnosticType) null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfExternMethods2
  public void testNoInlineOfExternMethods2() {
    testSame("var external={};external.charAt=function(){};",
        "external.charAt()", (DiagnosticType) null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfExternMethods3
  public void testNoInlineOfExternMethods3() {
    testSame("var external={};external.bar=function(){};",
        "function Foo(){}Foo.prototype.bar=function(){};(new Foo).bar()",
             (DiagnosticType) null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfDangerousProperty
  public void testNoInlineOfDangerousProperty() {
    testSame("function Foo(){this.bar=3}" +
        "Foo.prototype.bar=function(){};" +
        "var x=new Foo;var y=x.bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoWarn
  public void testNoWarn() {
    testSame("function Foo(){}" +
        "Foo.prototype.bar=function(opt_a,b){var x=1};" +
        "var x=new Foo;x.bar()");

    testSame("function Foo(){}" +
        "Foo.prototype.bar=function(var_args,b){var x=1};" +
        "var x=new Foo;x.bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testObjectLit
  public void testObjectLit() {
    testSame("Foo.prototype.bar=function(){return this.baz_};" +
             "var blah={bar:function(){}};" +
             "(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testObjectLit2
  public void testObjectLit2() {
    testSame("var blah={bar:function(){}};" +
             "(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testObjectLitExtern
  public void testObjectLitExtern() {
    String externs = "window.bridge={_sip:function(){}};";
    testSame(externs, "window.bridge._sip()", null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testExternFunction
  public void testExternFunction() {
    String externs = "function emptyFunction() {}";
    testSame(externs,
        "function Foo(){this.empty=emptyFunction}" +
        "(new Foo).empty()", null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testIssue2508576_1
  public void testIssue2508576_1() {
    
    String externs = "function alert(a) {}";
    testSame(externs, "({a:alert,b:alert}).a(\"a\")", null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testIssue2508576_2
  public void testIssue2508576_2() {
    
    testSame("({a:function(){},b:x()}).a(\"a\")");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testIssue2508576_3
  public void testIssue2508576_3() {
    
    test("({a:function(){},b:alert}).a(\"a\")", "");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testAnonymousGet
  public void testAnonymousGet() {
    
    testSame("({get a(){return function(){}},b:alert}).a(\"a\")");
    testSame("({get a(){},b:alert}).a(\"a\")");
    testSame("({get a(){},b:alert}).a");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testAnonymousSet
  public void testAnonymousSet() {
    
    testSame("({set a(b){return function(){}},b:alert}).a(\"a\")");
    testSame("({set a(b){},b:alert}).a(\"a\")");
    testSame("({set a(b){},b:alert}).a");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstants
  public void testInlineVariablesConstants() {
    test("var ABC=2; var x = ABC;", "var x=2");
    test("var AA = 'aa'; AA;", "'aa'");
    test("var A_A=10; A_A + A_A;", "10+10");
    test("var AA=1", "");
    test("var AA; AA=1", "1");
    test("var AA; if (false) AA=1; AA;", "if (false) 1; 1;");
    testSame("var AA; if (false) AA=1; else AA=2; AA;");

    test("var AA;(function () {AA=1})()",
         "(function () {1})()");

    
    testSame("var x = AA;");

    
    testSame("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);");

    test("var AA = '123456789012345';AA;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineArraysOrRegexps
  public void testNoInlineArraysOrRegexps() {
    testSame("var AA = [10,20]; AA[0]");
    testSame("var AA = [10,20]; AA.push(1); AA[0]");
    testSame("var AA = /x/; AA.test('1')");
    testSame(" var aa = /x/; aa.test('1')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsJsDocStyle
  public void testInlineVariablesConstantsJsDocStyle() {
    test("var abc=2; var x = abc;", "var x=2");
    test("var aa = 'aa'; aa;", "'aa'");
    test("var a_a=10; a_a + a_a;", "10+10");
    test("var aa=1;", "");
    test("var aa; aa=1;", "1");
    test("var aa;(function () {aa=1})()", "(function () {1})()");
    test("var aa;(function () {aa=1})(); var z=aa",
         "(function () {1})(); var z=1");
    testSame("var aa;(function () {var y; aa=y})(); var z=aa");

    
    testSame("var aa = '1234567890'; foo(aa); foo(aa); foo(aa);");

    test("var aa = '123456789012345';aa;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant1
  public void testInlineConditionallyDefinedConstant1() {
    
    
    
    
    test("if (x) var ABC = 2; if (y) f(ABC);",
         "if (x); if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant2
  public void testInlineConditionallyDefinedConstant2() {
    test("if (x); else var ABC = 2; if (y) f(ABC);",
         "if (x); else; if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant3
  public void testInlineConditionallyDefinedConstant3() {
    test("if (x) { var ABC = 2; } if (y) { f(ABC); }",
         "if (x) {} if (y) { f(2); }");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineDefinedConstant
  public void testInlineDefinedConstant() {
    test(
        "\n" +
        "var aa = '1234567890';\n" +
        "foo(aa); foo(aa); foo(aa);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");

    test(
        "\n" +
        "var ABC = '1234567890';\n" +
        "foo(ABC); foo(ABC); foo(ABC);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsWithInlineAllStringsOn
  public void testInlineVariablesConstantsWithInlineAllStringsOn() {
    inlineAllStrings = true;
    test("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);",
         "foo('1234567890'); foo('1234567890'); foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineWithoutConstDeclaration
  public void testNoInlineWithoutConstDeclaration() {
    testSame("var abc = 2; var x = abc;");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineAliases
  public void testNoInlineAliases() {
    testSame("var XXX = new Foo(); var yyy = XXX; bar(yyy)");
    testSame("var xxx = new Foo(); var YYY = xxx; bar(YYY)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineGlobal
  public void testInlineGlobal() {
    test("var x = 1; var z = x;", "var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName
  public void testNoInlineExportedName() {
    testSame("var _x = 1; var z = _x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName2
  public void testNoInlineExportedName2() {
    testSame("var f = function() {}; var _x = f;" +
             "var y = function() { _x(); }; var _y = f;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    testSame("var x = 1; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineDecrement
  public void testDoNotInlineDecrement() {
    testSame("var x = 1; x--;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    testSame("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoRhsOfAssign
  public void testInlineIntoRhsOfAssign() {
    test("var x = 1; var y = x;", "var y = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction
  public void testInlineInFunction() {
    test("function baz() { var x = 1; var z = x; }",
        "function baz() { var z = 1; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction2
  public void testInlineInFunction2() {
    test("function baz() { " +
            "var a = new obj();"+
            "result = a;" +
         "}",
         "function baz() { " +
            "result = new obj()" +
         "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction3
  public void testInlineInFunction3() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "(function(){a;})();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction4
  public void testInlineInFunction4() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "foo.result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction5
  public void testInlineInFunction5() {
    testSame(
        "function baz() { " +
           "var a = (foo = new obj());" +
           "foo.x();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAcrossModules
  public void testInlineAcrossModules() {
    
    test(createModules("var a = 2;", "var b = a;"),
        new String[] { "", "var b = 2;" });
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional1
  public void testDoNotExitConditional1() {
    testSame("if (true) { var x = 1; } var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional2
  public void testDoNotExitConditional2() {
    testSame("if (true) var x = 1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional3
  public void testDoNotExitConditional3() {
    testSame("var x; if (true) x=1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    testSame("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitForLoop
  public void testDoNotExitForLoop() {
    test("for (var i = 1; false; false) var z = i;",
         "for (;false;false) var z = 1;");
    testSame("for (; false; false) var i = 1; var z = i;");
    testSame("for (var i in {}); var z = i;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterSubscope
  public void testDoNotEnterSubscope() {
    testSame(
        "var x = function() {" +
        "  var self = this; " +
        "  return function() { var y = self; };" +
        "}");
    testSame(
        "var x = function() {" +
        "  var y = [1]; " +
        "  return function() { var z = y; };" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitTry
  public void testDoNotExitTry() {
    testSame("try { var x = y; } catch (e) {} var z = y; ");
    testSame("try { throw e; var x = 1; } catch (e) {} var z = x; ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterCatch
  public void testDoNotEnterCatch() {
    testSame("try { } catch (e) { var z = e; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterFinally
  public void testDoNotEnterFinally() {
    testSame("try { throw e; var x = 1; } catch (e) {} " +
             "finally  { var z = x; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfConditional
  public void testInsideIfConditional() {
    test("var a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOnlyReadAtInitialization
  public void testOnlyReadAtInitialization() {
    test("var a; a = foo();", "foo();");
    test("var a; if (a = foo()) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; switch (a = foo()) {}", "switch(foo()) {}");
    test("var a; function f(){ return a = foo(); }",
         "function f(){ return foo(); }");
    test("function f(){ var a; return a = foo(); }",
         "function f(){ return foo(); }");
    test("var a; with (a = foo()) { alert(3); }", "with (foo()) { alert(3); }");

    test("var a; b = (a = foo());", "b = foo();");
    test("var a; while(a = foo()) { alert(3); }",
         "while(foo()) { alert(3); }");
    test("var a; for(;a = foo();) { alert(3); }",
         "for(;foo();) { alert(3); }");
    test("var a; do {} while(a = foo()) { alert(3); }",
         "do {} while(foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testImmutableWithSingleReferenceAfterInitialzation
  public void testImmutableWithSingleReferenceAfterInitialzation() {
    test("var a; a = 1;", "1;");
    test("var a; if (a = 1) { alert(3); }", "if (1) { alert(3); }");
    test("var a; switch (a = 1) {}", "switch(1) {}");
    test("var a; function f(){ return a = 1; }",
         "function f(){ return 1; }");
    test("function f(){ var a; return a = 1; }",
         "function f(){ return 1; }");
    test("var a; with (a = 1) { alert(3); }", "with (1) { alert(3); }");

    test("var a; b = (a = 1);", "b = 1;");
    test("var a; while(a = 1) { alert(3); }",
         "while(1) { alert(3); }");
    test("var a; for(;a = 1;) { alert(3); }",
         "for(;1;) { alert(3); }");
    test("var a; do {} while(a = 1) { alert(3); }",
         "do {} while(1) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSingleReferenceAfterInitialzation
  public void testSingleReferenceAfterInitialzation() {
    test("var a; a = foo();a;", "foo();");
    testSame("var a; if (a = foo()) { alert(3); } a;");
    testSame("var a; switch (a = foo()) {} a;");
    testSame("var a; function f(){ return a = foo(); } a;");
    testSame("function f(){ var a; return a = foo(); a;}");
    testSame("var a; with (a = foo()) { alert(3); } a;");
    testSame("var a; b = (a = foo()); a;");
    testSame("var a; while(a = foo()) { alert(3); } a;");
    testSame("var a; for(;a = foo();) { alert(3); } a;");
    testSame("var a; do {} while(a = foo()) { alert(3); } a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfBranch
  public void testInsideIfBranch() {
    testSame("var a = foo(); if (1) { alert(a); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideAndConditional
  public void testInsideAndConditional() {
    test("var a = foo(); a && alert(3);", "foo() && alert(3);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideAndBranch
  public void testInsideAndBranch() {
    testSame("var a = foo(); 1 && alert(a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranch
  public void testInsideOrBranch() {
    testSame("var a = foo(); 1 || alert(a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideHookBranch
  public void testInsideHookBranch() {
    testSame("var a = foo(); 1 ? alert(a) : alert(3)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideHookConditional
  public void testInsideHookConditional() {
    test("var a = foo(); a ? alert(1) : alert(3)",
         "foo() ? alert(1) : alert(3)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranchInsideIfConditional
  public void testInsideOrBranchInsideIfConditional() {
    testSame("var a = foo(); if (x || a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranchInsideIfConditionalWithConstant
  public void testInsideOrBranchInsideIfConditionalWithConstant() {
    
    testSame("var a = [false]; if (x || a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testCrossFunctionsAsLeftLeaves
  public void testCrossFunctionsAsLeftLeaves() {
    
    test(
        new String[] { "var x = function() {};", "",
            "function cow() {} var z = x;"},
        new String[] { "", "", "function cow() {} var z = function() {};" });
    test(
        new String[] { "var x = function() {};", "",
            "var cow = function() {}; var z = x;"},
        new String[] { "", "",
            "var cow = function() {}; var z = function() {};" });
    testSame(
        new String[] { "var x = a;", "",
            "(function() { a++; })(); var z = x;"});
    test(
        new String[] { "var x = a;", "",
            "function cow() { a++; }; cow(); var z = x;"},
        new String[] { "var x = a;", "",
            ";(function cow(){ a++; })(); var z = x;"});
    testSame(
        new String[] { "var x = a;", "",
            "cow(); var z = x; function cow() { a++; };"});
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossFunction
  public void testDoCrossFunction() {
    
    
    test("var x = 1; foo(); var z = x;", "foo(); var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossReferencingFunction
  public void testDoNotCrossReferencingFunction() {
    testSame(
        "var f = function() { var z = x; };" +
        "var x = 1;" +
        "f();" +
        "var z = x;" +
        "f();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testChainedAssignment
  public void testChainedAssignment() {
    test("var a = 2, b = 2; var c = b;", "var a = 2; var c = 2;");
    test("var a = 2, b = 2; var c = a;", "var b = 2; var c = 2;");
    test("var a = b = 2; var f = 3; var c = a;", "var f = 3; var c = b = 2;");
    testSame("var a = b = 2; var c = b;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testForIn
  public void testForIn() {
    testSame("for (var i in j) { var c = i; }");
    testSame("var i = 0; for (i in j) ;");
    testSame("var i = 0; for (i in j) { var c = i; }");
    testSame("i = 0; for (var i in j) { var c = i; }");
    testSame("var j = {'key':'value'}; for (var i in j) {print(i)};");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossNewVariables
  public void testDoCrossNewVariables() {
    test("var x = foo(); var z = x;", "var z = foo();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossFunctionCalls
  public void testDoNotCrossFunctionCalls() {
    testSame("var x = foo(); bar(); var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossAssignment
  public void testDoNotCrossAssignment() {
    testSame("var x = {}; var y = x.a; x.a = 1; var z = y;");
    testSame("var a = this.id; foo(this.id = 3, a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossDelete
  public void testDoNotCrossDelete() {
    testSame("var x = {}; var y = x.a; delete x.a; var z = y;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossAssignmentPlus
  public void testDoNotCrossAssignmentPlus() {
    testSame("var a = b; b += 2; var c = a;");
  }

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

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ModifiedArguments1
  public void testIssue378ModifiedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ModifiedArguments2
  public void testIssue378ModifiedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  \n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments1
  public void testIssue378EscapedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments,this);\n" +
        "  f.apply(this, arguments);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments2
  public void testIssue378EscapedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  \n" +
        "  var f = callback;\n" +
        "  h(arguments,this);\n" +
        "  f.apply(this);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments3
  public void testIssue378EscapedArguments3() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  f.apply(this, arguments);\n" +
        "}\n",
        "function g(callback) {\n" +
        "  callback.apply(this, arguments);\n" +
        "}\n");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments4
  public void testIssue378EscapedArguments4() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments[0],this);\n" +
        "  f.apply(this, arguments);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ArgumentsRead1
  public void testIssue378ArgumentsRead1() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  var g = arguments[0];\n" +
        "  f.apply(this, arguments);\n" +
        "}",
        "function g(callback) {\n" +
        "  var g = arguments[0];\n" +
        "  callback.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ArgumentsRead2
  public void testIssue378ArgumentsRead2() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments[0],this);\n" +
        "  f.apply(this, arguments[0]);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}",
        "function g(callback) {\n" +
        "  h(arguments[0],this);\n" +
        "  callback.apply(this, arguments[0]);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testArgumentsModifiedInOuterFunction
  public void testArgumentsModifiedInOuterFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  arguments[0] = this;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  arguments[0] = this;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    callback.apply(this);\n" +
      "  }" +
      "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testArgumentsModifiedInInnerFunction
  public void testArgumentsModifiedInInnerFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    arguments[0] = this;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  callback.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    arguments[0] = this;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineRedeclaredExterns
  public void testNoInlineRedeclaredExterns() {
    String externs = "var test = 1;";
    String code = " var test = 2;alert(test);";
    test(externs, code, code, null, null);
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

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNestedExit
  public void testNestedExit() {
    this.instrumentationPb = "report_exit: \"$$testExit\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ return function(){ return c;}}",
         "$$testDefine(1);function a(){$$testDefine(0);" +
         "return $$testExit(1, function(){return $$testExit(0, c);});}");
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

// com.google.javascript.jscomp.IntegrationTest::testBug1949424
  public void testBug1949424() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.closurePass = true;
    test(options, CLOSURE_BOILERPLATE + "goog.provide('FOO'); FOO.bar = 3;",
         CLOSURE_COMPILED + "var FOO$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1949424_v2
  public void testBug1949424_v2() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.closurePass = true;
    test(options, CLOSURE_BOILERPLATE + "goog.provide('FOO.BAR'); FOO.BAR = 3;",
         CLOSURE_COMPILED + "var FOO$BAR = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1956277
  public void testBug1956277() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.inlineVariables = true;
    test(options, "var CONST = {}; CONST.bar = null;" +
         "function f(url) { CONST.bar = url; }",
         "var CONST$bar = null; function f(url) { CONST$bar = url; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1962380
  public void testBug1962380() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.inlineVariables = true;
    options.generateExports = true;
    test(options,
         CLOSURE_BOILERPLATE + " goog.CONSTANT = 1;" +
         "var x = goog.CONSTANT;",
         "(function() {})('goog.CONSTANT', 1);" +
         "var x = 1;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug2410122
  public void testBug2410122() {
    CompilerOptions options = createCompilerOptions();
    options.generateExports = true;
    options.closurePass = true;
    test(options,
         "var goog = {};" +
         "function F() {}" +
         " function G() { goog.base(this); } " +
         "goog.inherits(G, F);",
         "var goog = {};" +
         "function F() {}" +
         "function G() { F.call(this); } " +
         "goog.inherits(G, F); goog.exportSymbol('G', G);");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue90
  public void testIssue90() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    options.inlineVariables = true;
    options.removeDeadCode = true;
    test(options,
         "var x; x && alert(1);",
         "");
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassOff
  public void testClosurePassOff() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = false;
    testSame(
        options,
        "var goog = {}; goog.require = function(x) {}; goog.require('foo');");
    testSame(
        options,
        "var goog = {}; goog.getCssName = function(x) {};" +
        "goog.getCssName('foo');");
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassOn
  public void testClosurePassOn() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    test(
        options,
        "var goog = {}; goog.require = function(x) {}; goog.require('foo');",
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
    test(
        options,
        " var COMPILED = false;" +
        "var goog = {}; goog.getCssName = function(x) {};" +
        "goog.getCssName('foo');",
        "var COMPILED = true;" +
        "var goog = {}; goog.getCssName = function(x) {};" +
        "'foo';");
  }

// com.google.javascript.jscomp.IntegrationTest::testCssNameCheck
  public void testCssNameCheck() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkMissingGetCssNameLevel = CheckLevel.ERROR;
    options.checkMissingGetCssNameBlacklist = "foo";
    test(options, "var x = 'foo';",
         CheckMissingGetCssName.MISSING_GETCSSNAME);
  }

// com.google.javascript.jscomp.IntegrationTest::testBug2592659
  public void testBug2592659() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    options.checkMissingGetCssNameLevel = CheckLevel.WARNING;
    options.checkMissingGetCssNameBlacklist = "foo";
    test(options,
        "var goog = {};\n" +
        "\n" +
        "goog.getCssName = function(className, opt_modifier) {}\n" +
        "var x = goog.getCssName(123, 'a');",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypedefBeforeOwner1
  public void testTypedefBeforeOwner1() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    test(options,
         "goog.provide('foo.Bar.Type');\n" +
         "goog.provide('foo.Bar');\n" +
         " foo.Bar.Type;\n" +
         "foo.Bar = function() {};",
         "var foo = {}; foo.Bar.Type; foo.Bar = function() {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testTypedefBeforeOwner2
  public void testTypedefBeforeOwner2() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.collapseProperties = true;
    test(options,
         "goog.provide('foo.Bar.Type');\n" +
         "goog.provide('foo.Bar');\n" +
         " foo.Bar.Type;\n" +
         "foo.Bar = function() {};",
         "var foo$Bar$Type; var foo$Bar = function() {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testExportedNames
  public void testExportedNames() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options,
         " var COMPILED = false;" +
         "var goog = {}; goog.exportSymbol('b', goog);",
         "var a = true; var c = {}; c.exportSymbol('b', c);");
    test(options,
         " var COMPILED = false;" +
         "var goog = {}; goog.exportSymbol('a', goog);",
         "var b = true; var c = {}; c.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalThisOn
  public void testCheckGlobalThisOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    test(options, "function f() { this.y = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.IntegrationTest::testSusiciousCodeOff
  public void testSusiciousCodeOff() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = false;
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    test(options, "function f() { this.y = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalThisOff
  public void testCheckGlobalThisOff() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkGlobalThisLevel = CheckLevel.OFF;
    testSame(options, "function f() { this.y = 3; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckRequiresAndCheckProvidesOff
  public void testCheckRequiresAndCheckProvidesOff() {
    testSame(createCompilerOptions(), new String[] {
      " function Foo() {}",
      "new Foo();"
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckRequiresOn
  public void testCheckRequiresOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkRequires = CheckLevel.ERROR;
    test(options, new String[] {
      " function Foo() {}",
      "new Foo();"
    }, CheckRequiresForConstructors.MISSING_REQUIRE_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesOn
  public void testCheckProvidesOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkProvides = CheckLevel.ERROR;
    test(options, new String[] {
      " function Foo() {}",
      "new Foo();"
    }, CheckProvides.MISSING_PROVIDE_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testGenerateExportsOff
  public void testGenerateExportsOff() {
    testSame(createCompilerOptions(), " function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testGenerateExportsOn
  public void testGenerateExportsOn() {
    CompilerOptions options = createCompilerOptions();
    options.generateExports = true;
    test(options, " function f() {}",
         " function f() {} goog.exportSymbol('f', f);");
  }

// com.google.javascript.jscomp.IntegrationTest::testExportTestFunctionsOff
  public void testExportTestFunctionsOff() {
    testSame(createCompilerOptions(), "function testFoo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testExportTestFunctionsOn
  public void testExportTestFunctionsOn() {
    CompilerOptions options = createCompilerOptions();
    options.exportTestFunctions = true;
    test(options, "function testFoo() {}",
         " function testFoo() {}" +
         "goog.exportSymbol('testFoo', testFoo);");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckSymbolsOff
  public void testCheckSymbolsOff() {
    CompilerOptions options = createCompilerOptions();
    testSame(options, "x = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckSymbolsOn
  public void testCheckSymbolsOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    test(options, "x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckReferencesOff
  public void testCheckReferencesOff() {
    CompilerOptions options = createCompilerOptions();
    testSame(options, "x = 3; var x = 5;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckReferencesOn
  public void testCheckReferencesOn() {
    CompilerOptions options = createCompilerOptions();
    options.aggressiveVarCheck = CheckLevel.ERROR;
    test(options, "x = 3; var x = 5;",
         VariableReferenceCheck.UNDECLARED_REFERENCE);
  }

// com.google.javascript.jscomp.IntegrationTest::testInferTypes
  public void testInferTypes() {
    CompilerOptions options = createCompilerOptions();
    options.inferTypes = true;
    options.checkTypes = false;
    options.closurePass = true;

    test(options,
        CLOSURE_BOILERPLATE +
        "goog.provide('Foo');  Foo = {a: 3};",
        TypeCheck.ENUM_NOT_CONSTANT);
    assertTrue(lastCompiler.getErrorManager().getTypedPercent() == 0);

    
    test(options, " var n = window.name;",
        "var n = window.name;");
    assertTrue(lastCompiler.getErrorManager().getTypedPercent() == 0);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeCheckAndInference
  public void testTypeCheckAndInference() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, " var n = window.name;",
         TypeValidator.TYPE_MISMATCH_WARNING);
    assertTrue(lastCompiler.getErrorManager().getTypedPercent() > 0);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeNameParser
  public void testTypeNameParser() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, " var n = window.name;",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testMemoizedTypedScopeCreator
  public void testMemoizedTypedScopeCreator() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.ambiguateProperties = true;
    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, "function someTest() {\n"
        + "  \n"
        + "  function Foo() { this.instProp = 3; }\n"
        + "  Foo.prototype.protoProp = function(a, b) {};\n"
        + "  \n"
        + "  function Bar() {}\n"
        + "  goog.inherits(Bar, Foo);\n"
        + "  var o = new Bar();\n"
        + "  o.protoProp(o.protoProp, o.instProp);\n"
        + "}",
        "function someTest() {\n"
        + "  function Foo() { this.b = 3; }\n"
        + "  Foo.prototype.a = function(a, b) {};\n"
        + "  function Bar() {}\n"
        + "  goog.c(Bar, Foo);\n"
        + "  var o = new Bar();\n"
        + "  o.a(o.a, o.b);\n"
        + "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckTypes
  public void testCheckTypes() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, "var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceCssNames
  public void testReplaceCssNames() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.gatherCssNames = true;
    test(options, "\n"
         + "var COMPILED = false;\n"
         + "goog.setCssNameMapping({'foo':'bar'});\n"
         + "function getCss() {\n"
         + "  return goog.getCssName('foo');\n"
         + "}",
         "var COMPILED = true;\n"
         + "function getCss() {\n"
         + "  return \"bar\";"
         + "}");
    assertEquals(
        ImmutableMap.of("foo", new Integer(1)),
        lastCompiler.getPassConfig().getIntermediateState().cssNames);
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveClosureAsserts
  public void testRemoveClosureAsserts() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    testSame(options,
        "var goog = {};"
        + "goog.asserts.assert(goog);");
    options.removeClosureAsserts = true;
    test(options,
        "var goog = {};"
        + "goog.asserts.assert(goog);",
        "var goog = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testDeprecation
  public void testDeprecation() {
    String code = " function f() { } function g() { f(); }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setWarningLevel(DiagnosticGroups.DEPRECATED, CheckLevel.ERROR);
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.IntegrationTest::testVisibility
  public void testVisibility() {
    String[] code = {
        " function f() { }",
        "function g() { f(); }"
    };

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setWarningLevel(DiagnosticGroups.VISIBILITY, CheckLevel.ERROR);
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

// com.google.javascript.jscomp.IntegrationTest::testUnreachableCode
  public void testUnreachableCode() {
    String code = "function f() { return \n 3; }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.checkUnreachableCode = CheckLevel.ERROR;
    test(options, code, CheckUnreachableCode.UNREACHABLE_CODE);
  }

// com.google.javascript.jscomp.IntegrationTest::testMissingReturn
  public void testMissingReturn() {
    String code =
        " function f() { if (f) { return 3; } }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.checkMissingReturn = CheckLevel.ERROR;
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.IntegrationTest::testIdGenerators
  public void testIdGenerators() {
    String code =  "function f() {} f('id');";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.idGenerators = Sets.newHashSet("f");
    test(options, code, "function f() {} 'a';");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeArgumentsArray
  public void testOptimizeArgumentsArray() {
    String code =  "function f() { return arguments[0]; }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeArgumentsArray = true;
    String argName = "JSCompiler_OptimizeArgumentsArray_p0";
    test(options, code,
         "function f(" + argName + ") { return " + argName + "; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeParameters
  public void testOptimizeParameters() {
    String code = "function f(a) { return a; } f(true);";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeParameters = true;
    test(options, code, "function f() { var a = true; return a;} f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeReturns
  public void testOptimizeReturns() {
    String code = "function f(a) { return a; } f(true);";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeReturns = true;
    test(options, code, "function f(a) {return;} f(true);");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveAbstractMethods
  public void testRemoveAbstractMethods() {
    String code = CLOSURE_BOILERPLATE +
        "var x = {}; x.foo = goog.abstractMethod; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.closurePass = true;
    options.collapseProperties = true;
    test(options, code, CLOSURE_COMPILED + " var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseProperties1
  public void testCollapseProperties1() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    test(options, code, "var x$FOO = 5; var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseProperties2
  public void testCollapseProperties2() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    options.collapseObjectLiterals = true;
    test(options, code, "var x$FOO = 5; var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseObjectLiteral1
  public void testCollapseObjectLiteral1() {
    
    String code = "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseObjectLiterals = true;
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseObjectLiteral2
  public void testCollapseObjectLiteral2() {
    String code =
        "function f() {var x = {}; x.FOO = 5; x.bar = 3;}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseObjectLiterals = true;
    test(options, code,
        "function f(){" +
        "var JSCompiler_object_inline_FOO_0;" +
        "var JSCompiler_object_inline_bar_1;" +
        "JSCompiler_object_inline_FOO_0=5;" +
        "JSCompiler_object_inline_bar_1=3}");
  }

// com.google.javascript.jscomp.IntegrationTest::testTightenTypesWithoutTypeCheck
  public void testTightenTypesWithoutTypeCheck() {
    CompilerOptions options = createCompilerOptions();
    options.tightenTypes = true;
    test(options, "", DefaultPassConfig.TIGHTEN_TYPES_WITHOUT_TYPE_CHECK);
  }

// com.google.javascript.jscomp.IntegrationTest::testDisambiguateProperties
  public void testDisambiguateProperties() {
    String code =
        " function Foo(){} Foo.prototype.bar = 3;" +
        " function Baz(){} Baz.prototype.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.disambiguateProperties = true;
    options.checkTypes = true;
    test(options, code,
         "function Foo(){} Foo.prototype.Foo_prototype$bar = 3;" +
         "function Baz(){} Baz.prototype.Baz_prototype$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testMarkPureCalls
  public void testMarkPureCalls() {
    String testCode = "function foo() {} foo();";
    CompilerOptions options = createCompilerOptions();
    options.removeDeadCode = true;

    testSame(options, testCode);

    options.computeFunctionSideEffects = true;
    test(options, testCode, "function foo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testMarkNoSideEffects
  public void testMarkNoSideEffects() {
    String testCode = "noSideEffects();";
    CompilerOptions options = createCompilerOptions();
    options.removeDeadCode = true;

    testSame(options, testCode);

    options.markNoSideEffectCalls = true;
    test(options, testCode, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testChainedCalls
  public void testChainedCalls() {
    CompilerOptions options = createCompilerOptions();
    options.chainCalls = true;
    test(
        options,
        " function Foo() {} " +
        "Foo.prototype.bar = function() { return this; }; " +
        "var f = new Foo();" +
        "f.bar(); " +
        "f.bar(); ",
        "function Foo() {} " +
        "Foo.prototype.bar = function() { return this; }; " +
        "var f = new Foo();" +
        "f.bar().bar();");
  }

// com.google.javascript.jscomp.IntegrationTest::testExtraAnnotationNames
  public void testExtraAnnotationNames() {
    CompilerOptions options = createCompilerOptions();
    options.setExtraAnnotationNames(Sets.newHashSet("TagA", "TagB"));
    test(
        options,
        " var f = new Foo();  f.bar();",
        "var f = new Foo(); f.bar();");
  }

// com.google.javascript.jscomp.IntegrationTest::testDevirtualizePrototypeMethods
  public void testDevirtualizePrototypeMethods() {
    CompilerOptions options = createCompilerOptions();
    options.devirtualizePrototypeMethods = true;
    test(
        options,
        " var Foo = function() {}; " +
        "Foo.prototype.bar = function() {};" +
        "(new Foo()).bar();",
        "var Foo = function() {};" +
        "var JSCompiler_StaticMethods_bar = " +
        "    function(JSCompiler_StaticMethods_bar$self) {};" +
        "JSCompiler_StaticMethods_bar(new Foo());");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConsts
  public void testCheckConsts() {
    CompilerOptions options = createCompilerOptions();
    options.inlineConstantVars = true;
    test(options, "var FOO = true; FOO = false",
        ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testAllChecksOn
  public void testAllChecksOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkControlStructures = true;
    options.checkRequires = CheckLevel.ERROR;
    options.checkProvides = CheckLevel.ERROR;
    options.generateExports = true;
    options.exportTestFunctions = true;
    options.closurePass = true;
    options.checkMissingGetCssNameLevel = CheckLevel.ERROR;
    options.checkMissingGetCssNameBlacklist = "goog";
    options.syntheticBlockStartMarker = "synStart";
    options.syntheticBlockEndMarker = "synEnd";
    options.checkSymbols = true;
    options.aggressiveVarCheck = CheckLevel.ERROR;
    options.processObjectPropertyString = true;
    options.collapseProperties = true;
    test(options, CLOSURE_BOILERPLATE, CLOSURE_COMPILED);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeCheckingWithSyntheticBlocks
  public void testTypeCheckingWithSyntheticBlocks() {
    CompilerOptions options = createCompilerOptions();
    options.syntheticBlockStartMarker = "synStart";
    options.syntheticBlockEndMarker = "synEnd";
    options.checkTypes = true;

    
    
    
    testSame(
        options,
        " function f(x) {}" +
        "function g() {" +
        " synStart('foo');" +
        " var progress = 1;" +
        " f(progress);" +
        " synEnd('foo');" +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCompilerDoesNotBlowUpIfUndefinedSymbols
  public void testCompilerDoesNotBlowUpIfUndefinedSymbols() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;

    
    options.setWarningLevel(
        DiagnosticGroup.forType(VarCheck.UNDEFINED_VAR_ERROR),
        CheckLevel.OFF);

    
    testSame(options, "var x = {foo: y};");
  }

// com.google.javascript.jscomp.IntegrationTest::testConstantTagsMustAlwaysBeRemoved
  public void testConstantTagsMustAlwaysBeRemoved() {
    CompilerOptions options = createCompilerOptions();

    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    String originalText = "var G_GEO_UNKNOWN_ADDRESS=1;\n" +
        "function foo() {" +
        "  var localVar = 2;\n" +
        "  if (G_GEO_UNKNOWN_ADDRESS == localVar) {\n" +
        "    alert(\"A\"); }}";
    String expectedText = "var G_GEO_UNKNOWN_ADDRESS=1;" +
        "function foo(){var a=2;if(G_GEO_UNKNOWN_ADDRESS==a){alert(\"A\")}}";

    test(options, originalText, expectedText);
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassPreservesJsDoc
  public void testClosurePassPreservesJsDoc() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.closurePass = true;

    test(options,
         CLOSURE_BOILERPLATE +
         "goog.provide('Foo');  Foo = function() {};" +
         "var x = new Foo();",
         "var COMPILED=true;var goog={};goog.exportSymbol=function(){};" +
         "var Foo=function(){};var x=new Foo");
    test(options,
         CLOSURE_BOILERPLATE +
         "goog.provide('Foo');  Foo = {a: 3};",
         TypeCheck.ENUM_NOT_CONSTANT);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst
  public void testProvidedNamespaceIsConst() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo'); " +
         "function f() { foo = {};}",
         "var foo = {}; function f() { foo = {}; }",
         ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst2
  public void testProvidedNamespaceIsConst2() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.bar'); " +
         "function f() { foo.bar = {};}",
         "var foo$bar = {};" +
         "function f() { foo$bar = {}; }",
         ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst3
  public void testProvidedNamespaceIsConst3() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; " +
         "goog.provide('foo.bar'); goog.provide('foo.bar.baz'); " +
         " foo.bar = function() {};" +
         " foo.bar.baz = function() {};",
         "var foo$bar = function(){};" +
         "var foo$bar$baz = function(){};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst4
  public void testProvidedNamespaceIsConst4() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.Bar'); " +
         "var foo = {}; foo.Bar = {};",
         "var foo = {}; var foo = {}; foo.Bar = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst5
  public void testProvidedNamespaceIsConst5() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.Bar'); " +
         "foo = {}; foo.Bar = {};",
         "var foo = {}; foo = {}; foo.Bar = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProcessDefinesAlwaysOn
  public void testProcessDefinesAlwaysOn() {
    test(createCompilerOptions(),
         " var HI = true; HI = false;",
         "var HI = false;false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testProcessDefinesAdditionalReplacements
  public void testProcessDefinesAdditionalReplacements() {
    CompilerOptions options = createCompilerOptions();
    options.setDefineToBooleanLiteral("HI", false);
    test(options,
         " var HI = true;",
         "var HI = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceMessages
  public void testReplaceMessages() {
    CompilerOptions options = createCompilerOptions();
    String prefix = "var goog = {}; goog.getMsg = function() {};";
    testSame(options, prefix + "var MSG_HI = goog.getMsg('hi');");

    options.messageBundle = new EmptyMessageBundle();
    test(options,
        prefix + " var MSG_HI = goog.getMsg('hi');",
        prefix + "var MSG_HI = 'hi';");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalNames
  public void testCheckGlobalNames() {
    CompilerOptions options = createCompilerOptions();
    options.checkGlobalNamesLevel = CheckLevel.ERROR;
    test(options, "var x = {}; var y = x.z;",
         CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineGetters
  public void testInlineGetters() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function Foo() {} Foo.prototype.bar = function() { return 3; };" +
        "var x = new Foo(); x.bar();";

    testSame(options, code);
    options.inlineGetters = true;

    test(options, code,
         "function Foo() {} Foo.prototype.bar = function() { return 3 };" +
         "var x = new Foo(); 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineGettersWithAmbiguate
  public void testInlineGettersWithAmbiguate() {
    CompilerOptions options = createCompilerOptions();

    String code =
        "" +
        "function Foo() {}" +
        " Foo.prototype.field;" +
        "Foo.prototype.getField = function() { return this.field; };" +
        "" +
        "function Bar() {}" +
        " Bar.prototype.field;" +
        "Bar.prototype.getField = function() { return this.field; };" +
        "new Foo().getField();" +
        "new Bar().getField();";

    testSame(options, code);

    options.inlineGetters = true;

    test(options, code,
        "function Foo() {}" +
        "Foo.prototype.field;" +
        "Foo.prototype.getField = function() { return this.field; };" +
        "function Bar() {}" +
        "Bar.prototype.field;" +
        "Bar.prototype.getField = function() { return this.field; };" +
        "new Foo().field;" +
        "new Bar().field;");

    options.checkTypes = true;
    options.ambiguateProperties = true;

    
    
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineVariables
  public void testInlineVariables() {
    CompilerOptions options = createCompilerOptions();
    String code = "function foo() {} var x = 3; foo(x);";
    testSame(options, code);

    options.inlineVariables = true;
    test(options, code, "(function foo() {})(3);");

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    test(options, code, DefaultPassConfig.CANNOT_USE_PROTOTYPE_AND_VAR);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineConstants
  public void testInlineConstants() {
    CompilerOptions options = createCompilerOptions();
    String code = "function foo() {} var x = 3; foo(x); var YYY = 4; foo(YYY);";
    testSame(options, code);

    options.inlineConstantVars = true;
    test(options, code, "function foo() {} var x = 3; foo(x); foo(4);");
  }

// com.google.javascript.jscomp.IntegrationTest::testMinimizeExits
  public void testMinimizeExits() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() {" +
        "  if (window.foo) return; window.h(); " +
        "}";
    testSame(options, code);

    options.foldConstants = true;
    test(
        options, code,
        "function f() {" +
        "  window.foo || window.h(); " +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldConstants
  public void testFoldConstants() {
    CompilerOptions options = createCompilerOptions();
    String code = "if (true) { window.foo(); }";
    testSame(options, code);

    options.foldConstants = true;
    test(options, code, "window.foo();");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return; f(); }";
    testSame(options, code);

    options.removeDeadCode = true;
    test(options, code, "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedPrototypeProperties1
  public void testRemoveUnusedPrototypeProperties1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() {} " +
        "Foo.prototype.bar = function() { return new Foo(); };";
    testSame(options, code);

    options.removeUnusedPrototypeProperties = true;
    test(options, code, "function Foo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedPrototypeProperties2
  public void testRemoveUnusedPrototypeProperties2() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() {} " +
        "Foo.prototype.bar = function() { return new Foo(); };" +
        "function f(x) { x.bar(); }";
    testSame(options, code);

    options.removeUnusedPrototypeProperties = true;
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testSmartNamePass
  public void testSmartNamePass() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() { this.bar(); } " +
        "Foo.prototype.bar = function() { return Foo(); };";
    testSame(options, code);

    options.smartNameRemoval = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testDeadAssignmentsElimination
  public void testDeadAssignmentsElimination() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { var x = 3; 4; x = 5; return x; } f(); ";
    testSame(options, code);

    options.deadAssignmentElimination = true;
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "function f() { var x = 3; 4; x = 5; return x; } f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineFunctions
  public void testInlineFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return 3; } f(); ";
    testSame(options, code);

    options.inlineFunctions = true;
    test(options, code, "3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedVars1
  public void testRemoveUnusedVars1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f(x) {} f();";
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "function f() {} f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedVars2
  public void testRemoveUnusedVars2() {
    CompilerOptions options = createCompilerOptions();
    String code = "(function f(x) {})();var g = function() {}; g();";
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "(function() {})();var g = function() {}; g();");

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code, "(function f() {})();var g = function $g$() {}; g();");
  }

// com.google.javascript.jscomp.IntegrationTest::testCrossModuleCodeMotion
  public void testCrossModuleCodeMotion() {
    CompilerOptions options = createCompilerOptions();
    String[] code = new String[] {
      "var x = 1;",
      "x;",
    };
    testSame(options, code);

    options.crossModuleCodeMotion = true;
    test(options, code, new String[] {
      "",
      "var x = 1; x;",
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testCrossModuleMethodMotion
  public void testCrossModuleMethodMotion() {
    CompilerOptions options = createCompilerOptions();
    String[] code = new String[] {
      "var Foo = function() {}; Foo.prototype.bar = function() {};" +
      "var x = new Foo();",
      "x.bar();",
    };
    testSame(options, code);

    options.crossModuleMethodMotion = true;
    test(options, code, new String[] {
      CrossModuleMethodMotion.STUB_DECLARATIONS +
      "var Foo = function() {};" +
      "Foo.prototype.bar=JSCompiler_stubMethod(0); var x=new Foo;",
      "Foo.prototype.bar=JSCompiler_unstubMethod(0,function(){}); x.bar()",
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testFlowSensitiveInlineVariables1
  public void testFlowSensitiveInlineVariables1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { var x = 3; x = 5; return x; }";
    testSame(options, code);

    options.flowSensitiveInlineVariables = true;
    test(options, code, "function f() { var x = 3; return 5; }");

    String unusedVar = "function f() { var x; x = 5; return x; } f()";
    test(options, unusedVar, "function f() { var x; return 5; } f()");

    options.removeUnusedVars = true;
    test(options, unusedVar, "function f() { return 5; } f()");
  }

// com.google.javascript.jscomp.IntegrationTest::testFlowSensitiveInlineVariables2
  public void testFlowSensitiveInlineVariables2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
        "function f () {\n" +
        "    var ab = 0;\n" +
        "    ab += '-';\n" +
        "    alert(ab);\n" +
        "}",
        "function f () {\n" +
        "    alert('0-');\n" +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseAnonymousFunctions
  public void testCollapseAnonymousFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "var f = function() {};";
    testSame(options, code);

    options.collapseAnonymousFunctions = true;
    test(options, code, "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testMoveFunctionDeclarations
  public void testMoveFunctionDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var x = f(); function f() { return 3; }";
    testSame(options, code);

    options.moveFunctionDeclarations = true;
    test(options, code, "function f() { return 3; } var x = f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testNameAnonymousFunctions
  public void testNameAnonymousFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "var f = function() {};";
    testSame(options, code);

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.MAPPED;
    test(options, code, "var f = function $() {}");
    assertNotNull(lastCompiler.getResult().namedAnonFunctionMap);

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code, "var f = function $f$() {}");
    assertNull(lastCompiler.getResult().namedAnonFunctionMap);
  }

// com.google.javascript.jscomp.IntegrationTest::testExtractPrototypeMemberDeclarations
  public void testExtractPrototypeMemberDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var f = function() {};";
    String expected = "var a; var b = function() {}; a = b.prototype;";
    for (int i = 0; i < 10; i++) {
      code += "f.prototype.a = " + i + ";";
      expected += "a.a = " + i + ";";
    }
    testSame(options, code);

    options.extractPrototypeMemberDeclarations = true;
    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options, code, expected);

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    options.variableRenaming = VariableRenamingPolicy.OFF;
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testDevirtualizationAndExtractPrototypeMemberDeclarations
  public void testDevirtualizationAndExtractPrototypeMemberDeclarations() {
    CompilerOptions options = createCompilerOptions();
    options.devirtualizePrototypeMethods = true;
    options.collapseAnonymousFunctions = true;
    options.extractPrototypeMemberDeclarations = true;
    options.variableRenaming = VariableRenamingPolicy.ALL;
    String code = "var f = function() {};";
    String expected = "var a; function b() {} a = b.prototype;";
    for (int i = 0; i < 10; i++) {
      code += "f.prototype.argz = function() {arguments};";
      code += "f.prototype.devir" + i + " = function() {};";

      char letter = (char) ('d' + i);
      expected += "a.argz = function() {arguments};";
      expected += "function " + letter + "(c){}";
    }

    code += "var F = new f(); F.argz();";
    expected += "var n = new b(); n.argz();";

    for (int i = 0; i < 10; i++) {
      code += "F.devir" + i + "();";

      char letter = (char) ('d' + i);
      expected += letter + "(n);";
    }
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testCoalesceVariableNames
  public void testCoalesceVariableNames() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() {var x = 3; var y = x; var z = y; return z;}";
    testSame(options, code);

    options.coalesceVariableNames = true;
    test(options, code,
         "function f() {var x = 3; x = x; x = x; return x;}");
  }

// com.google.javascript.jscomp.IntegrationTest::testPropertyRenaming
  public void testPropertyRenaming() {
    CompilerOptions options = createCompilerOptions();
    options.propertyAffinity = true;
    String code =
        "function f() { return this.foo + this['bar'] + this.Baz; }" +
        "f.prototype.bar = 3; f.prototype.Baz = 3;";
    String heuristic =
        "function f() { return this.foo + this['bar'] + this.a; }" +
        "f.prototype.bar = 3; f.prototype.a = 3;";
    String aggHeuristic =
        "function f() { return this.foo + this['b'] + this.a; } " +
        "f.prototype.b = 3; f.prototype.a = 3;";
    String all =
        "function f() { return this.b + this['bar'] + this.a; }" +
        "f.prototype.c = 3; f.prototype.a = 3;";
    testSame(options, code);

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    test(options, code, heuristic);

    options.propertyRenaming = PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC;
    test(options, code, aggHeuristic);

    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, code, all);
  }

// com.google.javascript.jscomp.IntegrationTest::testConvertToDottedProperties
  public void testConvertToDottedProperties() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() { return this['bar']; } f.prototype.bar = 3;";
    String expected =
        "function f() { return this.bar; } f.prototype.a = 3;";
    testSame(options, code);

    options.convertToDottedProperties = true;
    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRewriteFunctionExpressions
  public void testRewriteFunctionExpressions() {
    CompilerOptions options = createCompilerOptions();
    String code = "var a = function() {};";
    String expected = "function JSCompiler_emptyFn(){return function(){}} " +
        "var a = JSCompiler_emptyFn();";
    for (int i = 0; i < 10; i++) {
      code += "a = function() {};";
      expected += "a = JSCompiler_emptyFn();";
    }
    testSame(options, code);

    options.rewriteFunctionExpressions = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAliasAllStrings
  public void testAliasAllStrings() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return 'a'; }";
    String expected = "var $$S_a = 'a'; function f() { return $$S_a; }";
    testSame(options, code);

    options.aliasAllStrings = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAliasExterns
  public void testAliasExterns() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return window + window + window + window; }";
    String expected = "var GLOBAL_window = window;" +
        "function f() { return GLOBAL_window + GLOBAL_window + " +
        "               GLOBAL_window + GLOBAL_window; }";
    testSame(options, code);

    options.aliasExternals = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testAliasKeywords
  public void testAliasKeywords() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() { return true + true + true + true + true + true; }";
    String expected = "var JSCompiler_alias_TRUE = true;" +
        "function f() { return JSCompiler_alias_TRUE + " +
        "    JSCompiler_alias_TRUE + JSCompiler_alias_TRUE + " +
        "    JSCompiler_alias_TRUE + JSCompiler_alias_TRUE + " +
        "    JSCompiler_alias_TRUE; }";
    testSame(options, code);

    options.aliasKeywords = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameVars1
  public void testRenameVars1() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "var abc = 3; function f() { var xyz = 5; return abc + xyz; }";
    String local = "var abc = 3; function f() { var a = 5; return abc + a; }";
    String all = "var a = 3; function c() { var b = 5; return a + b; }";
    testSame(options, code);

    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    test(options, code, local);

    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options, code, all);

    options.reserveRawExports = true;
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameVars2
  public void testRenameVars2() {
    CompilerOptions options = createCompilerOptions();
    options.variableRenaming = VariableRenamingPolicy.ALL;

    String code =     "var abc = 3; function f() { window['a'] = 5; }";
    String noexport = "var a = 3;   function b() { window['a'] = 5; }";
    String export =   "var b = 3;   function c() { window['a'] = 5; }";

    options.reserveRawExports = false;
    test(options, code, noexport);

    options.reserveRawExports = true;
    test(options, code, export);
  }

// com.google.javascript.jscomp.IntegrationTest::testShadowVaribles
  public void testShadowVaribles() {
    CompilerOptions options = createCompilerOptions();
    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    options.shadowVariables = true;
    String code =     "var f = function(x) { return function(y) {}}";
    String expected = "var f = function(a) { return function(a) {}}";
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameLabels
  public void testRenameLabels() {
    CompilerOptions options = createCompilerOptions();
    String code = "longLabel: while (true) { break longLabel; }";
    String expected = "a: while (true) { break a; }";
    testSame(options, code);

    options.labelRenaming = true;
    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testBadBreakStatementInIdeMode
  public void testBadBreakStatementInIdeMode() {
    
    
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    options.checkTypes = true;
    test(options,
         "function f() { try { } catch(e) { break; } }",
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue63SourceMap
  public void testIssue63SourceMap() {
    CompilerOptions options = createCompilerOptions();
    String code = "var a;";

    options.skipAllPasses = true;
    options.sourceMapOutputPath = "./src.map";

    Compiler compiler = compile(options, code);
    compiler.toSource();
  }

// com.google.javascript.jscomp.IntegrationTest::testRegExp1
  public void testRegExp1() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;

    String code = "/(a)/.test(\"a\");";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String expected = "";

    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testRegExp2
  public void testRegExp2() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = "/(a)/.test(\"a\");var a = RegExp.$1";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    test(options, code, CheckRegExp.REGEXP_REFERENCE);

    options.setWarningLevel(DiagnosticGroups.CHECK_REGEXP, CheckLevel.OFF);

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals1
  public void testFoldLocals1() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    
    
    String code = "new Widget().go();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals2
  public void testFoldLocals2() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;
    options.checkTypes = true;

    
    
    String code = "widgetToken().go();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    test(options, code, "widgetToken()");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals3
  public void testFoldLocals3() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    
    
    String definition = "function f(){return new Widget()}";
    String call = "f().go();";
    String code = definition + call;

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    
    
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals4
  public void testFoldLocals4() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = "\n"
        + "function InternalWidget(){this.x = 1;}"
        + "InternalWidget.prototype.internalGo = function (){this.x = 2};"
        + "new InternalWidget().internalGo();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String optimized = ""
      + "function InternalWidget(){this.x = 1;}"
      + "InternalWidget.prototype.internalGo = function (){this.x = 2};";

    test(options, code, optimized);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals5
  public void testFoldLocals5() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function fn(){var a={};a.x={};return a}"
        + "fn().x.y = 1;";

    
    
    String result = ""
        + "function fn(){var a={x:{}};return a}"
        + "fn().x.y = 1;";

    test(options, code, result);

    options.computeFunctionSideEffects = true;

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals6
  public void testFoldLocals6() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function fn(){return {}}"
        + "fn().x.y = 1;";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals7
  public void testFoldLocals7() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function InternalWidget(){return [];}"
        + "Array.prototype.internalGo = function (){this.x = 2};"
        + "InternalWidget().internalGo();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String optimized = ""
      + "function InternalWidget(){return [];}"
      + "Array.prototype.internalGo = function (){this.x = 2};";

    test(options, code, optimized);
  }

// com.google.javascript.jscomp.IntegrationTest::testVarDeclarationsIntoFor
  public void testVarDeclarationsIntoFor() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "var a = 1; for (var b = 2; ;) {}";

    testSame(options, code);

    options.collapseVariableDeclarations = false;

    test(options, code, "for (var a = 1, b = 2; ;) {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testExploitAssigns
  public void testExploitAssigns() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "a = 1; b = a; c = b";

    testSame(options, code);

    options.collapseVariableDeclarations = true;

    test(options, code, "c=b=a=1");
  }

// com.google.javascript.jscomp.IntegrationTest::testRecoverOnBadExterns
  public void testRecoverOnBadExterns() throws Exception {
    
    
    
    
    
    
    
    
    
    CompilerOptions options = createCompilerOptions();

    options.aliasExternals = true;
    externs = new JSSourceFile[] {
      JSSourceFile.fromCode("externs", "extern.foo")
    };

    test(options,
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testDuplicateVariablesInExterns
  public void testDuplicateVariablesInExterns() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    externs = new JSSourceFile[] {
      JSSourceFile.fromCode("externs",
          "var externs = {};  var externs = {};")
    };
    testSame(options, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode
  public void testLanguageMode() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);

    String code = "var a = {get f(){}}";

    Compiler compiler = compile(options, code);
    checkUnexpectedErrorsOrWarnings(compiler, 1);
    assertEquals(
        "JSC_PARSE_ERROR. Parse error. " +
        "getters are not supported in Internet Explorer " +
        "at i0 line 1 : 0",
        compiler.getErrors()[0].toString());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode2
  public void testLanguageMode2() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.OFF);

    String code = "var a  = 2; delete a;";

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    test(options,
        code,
        code,
        StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue598
  public void testIssue598() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    WarningLevel.VERBOSE.setOptionsForWarningLevel(options);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    String code =
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};";

    Compiler compiler = compile(options, code);
    testSame(options, code);
  }
