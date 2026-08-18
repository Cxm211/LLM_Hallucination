// buggy code
  public String getLine(int lineNumber) {
    String js = "";
    try {
      // NOTE(nicksantos): Right now, this is optimized for few warnings.
      // This is probably the right trade-off, but will be slow if there
      // are lots of warnings in one file.
      js = getCode();
    } catch (IOException e) {
      return null;
    }

    int pos = 0;
    int startLine = 1;

    // If we've saved a previous offset and it's for a line less than the
    // one we're searching for, then start at that point.
    if (lineNumber >= lastLine) {
      pos = lastOffset;
      startLine = lastLine;
    }

    for (int n = startLine; n < lineNumber; n++) {
      int nextpos = js.indexOf('\n', pos);
      if (nextpos == -1) {
        return null;
      }
      pos = nextpos + 1;
    }

    // Remember this offset for the next search we do.
    lastOffset = pos;
    lastLine = lineNumber;

    if (js.indexOf('\n', pos) == -1) {
      // If next new line cannot be found, there are two cases
      // 1. pos already reaches the end of file, then null should be returned
      // 2. otherwise, return the contents between pos and the end of file.
        return null;
    } else {
      return js.substring(pos, js.indexOf('\n', pos));
    }
  }

// relevant test
// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction7
  public void testNonHoistedFunction7() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("function g() { if (false) { f(); function f() {} }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction1
  public void testNonHoistedRecursiveFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() { f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction2
  public void testNonHoistedRecursiveFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction3
  public void testNonHoistedRecursiveFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); g(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns1
  public void testNoWarnInExterns1() {
    
    String externs =
       "var google;" +
       " var google";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns2
  public void testNoWarnInExterns2() {
    
    String externs =
       "window;" +
       "var window;";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoWarnShadowGlobal
  public void testNoWarnShadowGlobal() {
    
    
    assertNoError("", "var x; function foo() { var x } ");
    assertNoError("var x", "function foo() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal1
  public void testWarnShadowLocal1() {
    assertError("", "function a(){ var x; function b() { var x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal2
  public void testWarnShadowLocal2() {
    assertError("",
                "function a(){" +
                "   var x;" +
                "  function b() {" +
                "    var x = 1;" +
                "  }" +
                "}");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testUseShadowGlobals1
  public void testUseShadowGlobals1() {
    assertNoError("", " var x; function foo() { x = 1 } ");
    assertNoError("", "function a() { var x; function b() { x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotation
  public void testNoShadowAnnotation() {
    assertError("",
                " var x; function a() { var x } ");

    assertError("",
                " var x; function a() {function b(){var x}} ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns1
  public void testNoShadowAnnotationInExterns1() {
    assertError(" var x",
                "function a() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns2
  public void testNoShadowAnnotationInExterns2() {
    assertError(" var x",
                "function a() {function b(){var x}} ");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testCapturedVariables
  public void testCapturedVariables() {
    String source =
        "global:var global;\n" +
        "function Outer() {\n" +
        "  captured:var captured;\n" +
        "  notcaptured:var notCaptured;\n" +
        "  function Inner() {\n" +
        "    alert(captured);" +
        "   }\n" +
        "}\n";

    analyze(source);

    assertIsCapturedLocal("captured");
    assertIsUncapturedLocal("notcaptured");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testGlobals
  public void testGlobals() {
    String source =
      "global:var global;";

    analyze(source);

    assertIsGlobal("global");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testParameters
  public void testParameters() {
    String source =
      "function A(a,b,c) {\n" +
      "}\n";

    analyze(source);

    assertIsParameter("a");
    assertIsParameter("b");
    assertIsParameter("c");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testFunctions
  public void testFunctions() {
    String source =
        "function global() {\n" +
        "  function inner() {\n" +
        "  }\n" +
        "  function innerCaptured() {\n" +
        "    (function(){innerCaptured()})()\n" +
        "  }\n" +
        "}\n";

    analyze(source);

    assertFunctionHasVisibility("global",
        VariableVisibility.GLOBAL);

    assertFunctionHasVisibility("inner",
        VariableVisibility.LOCAL);

    assertFunctionHasVisibility("innerCaptured",
        VariableVisibility.CAPTURED_LOCAL);
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testArray
  public void testArray() throws Exception {
    testConversion("[,]");
    testConversion("[]");
    testConversion("[function (x) {}]");
    testConversion("[[], [a, [], [[[]], 1], f([a])], 1];");
    testConversion("x = [1, 2, 3]");
    testConversion("var x = [1, 2, 3]");
    testConversion("[, 1, Object(), , , 2]");
    testConversion("[{x: 'abc', y: 1}]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testArray1
  public void testArray1() throws Exception {
    testConversion("[,]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testAssignOperators
  public void testAssignOperators() throws Exception {
    testConversion("x += 1, x -= 1, x *= 1, x /= 1, x %= 1");
    testConversion("x |= 1, x ^= x, x &= 0");
    testConversion("x <<= 1, x >>= 1, x >>>= 1");
    testConversion("y = x += 1");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testCalls
  public void testCalls() throws Exception {
    testConversion("f()");
    testConversion("f(1)");
    testConversion("f('a')");
    testConversion("f(true)");
    testConversion("f(null)");
    testConversion("f(undefined)");

    testConversion("f(a + b)");
    testConversion("f(g(h(a)) * h(g(u(z('a')))))");

    testConversion("x = f()");
    testConversion("x = f(1)");
    testConversion("x = f(a + b)");
    testConversion("x = f(g(h(a)) * h(g(u(z('a')))))");

    testConversion("String('a')");
    testConversion("Number(1)");
    testConversion("Boolean(0)");
    testConversion("Object()");
    testConversion("Array('a', 1, false, null, Object(), String('a'))");

    testConversion("(function() {})()");
    testConversion("(function(x) {})(x)");
    testConversion("(function(x) {var y = x << 1; return y})(x)");
    testConversion("(function(x) {y = x << 1; return y})(x)");
    testConversion("var x = (function(x) {y = x << 1; return y})(x)");
    testConversion("var x = (function(x) {return x << 1})(x)");

    testConversion("eval()");
    testConversion("eval('x')");
    testConversion("x = eval('x')");
    testConversion("var x = eval('x')");
    testConversion("eval(Template('foo${bar}baz')); var Template;");

    testConversion("a.x()");
    testConversion("a[x]()");
    testConversion("z = a.x()");
    testConversion("var z = a.x()");
    testConversion("z = a[x]()");
    testConversion("z = a['x']()");
    testConversion("var z = a[x]()");
    testConversion("var z = a['x']()");
    testConversion("a.x(y)");
    testConversion("a[x](y)");
    testConversion("a['x'](y)");
    testConversion("a[x](y, z, 'a', null, true, f(y))");
    testConversion("a['x'](y, z, 'a', null, true, f(y))");
    testConversion("a[b[c[d]]()].x");

    testConversion("(f())()");
    testConversion("(f(x))(y)");
    testConversion("(f = getFn())()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testConditionals
  public void testConditionals() throws Exception {
    testConversion("x ? y : z");
    testConversion("result = x ? y : z");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDecIncOperators
  public void testDecIncOperators() throws Exception {
    testConversion("x--");
    testConversion("--x");
    testConversion("x++");
    testConversion("++x");
    testConversion("var y=x++, z=++x; var s=y--, r=++y;");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDelete
  public void testDelete() throws Exception {
    testConversion("delete a");
    testConversion("delete a.x");
    testConversion("delete a[0]");
    testConversion("delete a.x[0]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDirectives
  public void testDirectives() throws Exception {
    testConversion("'use strict'");
    testConversion("function foo() {'use strict'}");
    testConversion("'use strict'; function foo() {'use strict'}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDoWhile
  public void testDoWhile() throws Exception {
  
     testConversion("do {} while (true)");
     testConversion("do {;} while (true)");
     testConversion("do {} while (f(x, y))");
     testConversion("do {} while (f(f(f(x, y))))");
     testConversion("do {} while ((f(f(f(x, y))))())");
     testConversion("do {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}} while (--x)");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testFor
  public void testFor() throws Exception {
     testConversion("for (;true;) {;}");
     testJsonMLToAstConversion("for (i = 0; i < 10; ++i) x++");
     testConversion("for (i = 0; i < 10; ++i) {x++}");
     testConversion("for (i = 0; i < 10; ++i) {2 + 3; q = 2 + 3; "
         + "var v = y * z; g = function(a) {true; var b = a + 1;"
         + "return a * a}}");

     testConversion("for(;true;) {break}");
     testConversion("for(i = 0; i < 10; ++i) {if (i > 5) {break}}");
     testConversion("s: for(i = 0; i < 10; ++i) {if (i > 5) {break s}}");
     testConversion("for (i = 0;true; ++i) {"
         + "if (i % 2) {continue} else {var x = i / 3; f(x)}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testForIn
  public void testForIn() throws Exception {
    testConversion("for (var i in x) {}");
    testConversion("for (var i in x) {;}");
    testConversion("for (var i in x) {f(x)}");
    testConversion("s: for(var i in x) {if (i > 5) {break s}}");
    testConversion("for (var i in x) {if (i % 2) {"
        + "continue} else {var x = i / 3; f(x)}}");
    testConversion("for (var i in x) {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}");

    testConversion("for (i in x) {}");
    testConversion("for (i in x) {;}");
    testConversion("for (i in x) {f(x)}");
    testConversion("s: for (i in x) {if (i > 5) {break s}}");
    testConversion("for (i in x) {if (i % 2) {"
        + "continue} else {var x = i / 3; f(x)}}");
    testConversion("for (i in x) {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}");

  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testFunctions
  public void testFunctions() throws Exception {
    testConversion("(function () {})");
    testConversion("(function (x, y) {})");
    testConversion("(function () {})()");
    testConversion("(function (x, y) {})()");
    testConversion("[ function f() {} ]");
    testConversion("var f = function f() {};");
    testConversion("for (function f() {};true;) {}");
    testConversion("x = (function (x, y) {})");

    testConversion("function f() {}");
    testConversion("for (;true;) { function f() {} }");

    testConversion("function f() {;}");
    testConversion("function f() {x}");
    testConversion("function f() {x;y;z}");
    testConversion("function f() {{}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testIfElse1
  public void testIfElse1() throws Exception {
    testConversion("if (true) {x = 1}");
    testConversion("if (true) {x = 1} else {x = 2}");
    testConversion("if (f(f(f()))) {x = 1} else {x = 2}");
    testConversion("if ((f(f(f())))()) {x = 1} else {x = 2}");
    testConversion("if (true) {x = 1}; x = 1;");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testLabels
  public void testLabels() throws Exception {
    testConversion("s: ;");
    testConversion("s: {;}");
    testConversion("s: while(true) {;}");
    testConversion("s: switch (x) {case 'a': break s;}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testLogicalExpr
  public void testLogicalExpr() throws Exception {
    testConversion("a && b");
    testConversion("a || b");
    testConversion("a && b || c");
    testConversion("a && (b || c)");
    testConversion("f(x) && (function (x) {"
        + "return x % 2 == 0 })(z) || z % 3 == 0 ? true : false");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testMathExpr
  public void testMathExpr() throws Exception {
    testConversion("2 + 3 * 4");
    testConversion("(2 + 3) * 4");
    testConversion("2 * (3 + 4)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testMember
  public void testMember() throws Exception {
    testConversion("o.x");
    testConversion("a.b.c");
    testConversion("a.b.c.d");
    testConversion("o[x]");
    testConversion("o[0]");
    testConversion("o[2 + 3 * 4]");
    testConversion("o[(function (x){var y = g(x) << 1; return y * x})()]");
    testConversion("o[o.x]");
    testConversion("o.x[x]");
    testConversion("a.b[o.x]");
    testConversion("a.b[1]");
    testConversion("a[b[c[d]]].x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testNew
  public void testNew() throws Exception {
    testConversion("new A");
    testConversion("new A()");

    testConversion("new A(x, y, z)");
    testConversion("new A(f(x), g(y), h(z))");
    testConversion("new A(x, new B(x, y), z)");
    testConversion("new A(1), new B()");
    testConversion("new A, B");

    testConversion("x = new A(a)");
    testConversion("var x = new A(a, b)");
    testConversion("var x = new A(1), y = new B()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testObject0
  public void testObject0() throws Exception {
    
    
    
    
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testObject
  public void testObject() throws Exception {
    testConversion("x = {}");
    testConversion("var x = {}");
    testConversion("x = {x: 1, y: 2}");
    
    
    testConversion("x = {x: null}");
    testConversion("x = {a: function f() {}}");
    
    testConversion("x = {a: f()}");
    
    testConversion("x = {a: function f() {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}}");
    
    
    testConversion("x = {get a() {return 1}}");
    testConversion("x = {set a(b) {}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testOperators
  public void testOperators() throws Exception {
    testConversion("x instanceof Null");
    testConversion("!x instanceof A");
    testConversion("!(x instanceof A)");

    testConversion("'a' in x");
    testConversion("if('a' in x) {f(x)}");
    testConversion("undefined in A");
    testConversion("!(Number(1) in [2, 3, 4])");

    testConversion("true ? x : y");
    testConversion("(function() {var y = 2 + 3 * 4; return y >> 1})() ? x : y");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testReturnStatement
  public void testReturnStatement() throws Exception {
    testConversion("x = function f() {return}");
    testConversion("x = function f() {return 1}");
    testConversion("x = function f() {return 2 + 3 / 4}");
    testConversion("x = function f() {return function() {}}");
    testConversion("x = function f() {var y = 2; "
        + "return function() {return y * 3}}");
    testConversion("x = function f() {z = 2 + 3; "
        + "return (function(z) {return z * y})(z)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testRegExp
  public void testRegExp() throws Exception {
    testConversion("/ab/");
    testConversion("/ab/g");
    testConversion("x = /ab/");
    testConversion("x = /ab/g");
    testConversion("var x = /ab/");
    testConversion("var x = /ab/g");
    testConversion("function f() {"
        + "/ab/; var x = /ab/; (function g() {/ab/; var x = /ab/})()}");
    testConversion("var f = function () {return /ab/g;}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testSimplePrograms
  public void testSimplePrograms() throws Exception {
    testConversion(";");
    testConversion("1");
    testConversion("x");
    testConversion("x=1");
    testConversion("{}");
    testConversion("{;}");
    testConversion("{x=1}");
    testConversion("x='a'");

    testConversion("true");
    testConversion("false");
    testConversion("x=true");
    testConversion("x=false");

    testConversion("undefined");
    testConversion("x=undefined");

    testConversion("null");
    testConversion("x = null");

    testConversion("this");
    testConversion("2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}");

    testConversion("a; b");
    testConversion("a; b; c; d");

    testConversion("x = function () {}");
    testConversion("x = function f() {}");

    testConversion("x = function (arg1, arg2) {}");
    testConversion("x = function f(arg1, arg2) {}");

    testConversion("x = function f(arg1, arg2) {1}");
    testConversion("x = function f(arg1, arg2) {x}");

    testConversion("x = function f(arg1, arg2) {x = 1 + 1}");

    testConversion("var re = new RegExp(document.a.b.c);"
        + "var m = re.exec(document.a.b.c);");

  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testSwitch
  public void testSwitch() throws Exception {
  testConversion("switch (x) {}");
  testConversion("switch (x) {case 'a':}");
  testConversion("switch (x) {case 'a':case 'b':}");
  testConversion("switch (x) {case 'a':case 'b': x}");
  testConversion("switch (x) {case 'a':case 'b': {;}}");
  testConversion("switch (x) {case 'a':case 'b': f()}");
  testConversion("switch (x) {case 'x': case 'y': {;} case 'a':case 'b': f()}");
  testConversion("switch (x) {case 'a': f(x)}");
  testConversion("switch (x) {case 'a': {f()} {g(x)}}");
  testConversion("switch (x) {case 'a': f(); g(x)}");
  testConversion("switch (x) {default: ;}");
  testConversion("switch (x) {default:case 'a': ;}");
  testConversion("switch (x) {case 'a':case'b':default: f()}");
  testConversion("switch (x) {default:f(x); g(); case 'a': ; case 'b': g(x)}");
  testConversion("switch (x) {case 'a': default: {f(x); g(z)} case 'b': g(x)}");
  testConversion("switch (x) {case x: {;}}");
}

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testType
  public void testType() throws Exception {
    testConversion("undefined");
    testConversion("null");

    testConversion("0");
    testConversion("+0");
    testConversion("0.0");

    testConversion("3.14");
    testConversion("+3.14");

    testConversion("true");
    testConversion("false");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThis
  public void testThis() throws Exception {
    testConversion("this");
    testConversion("var x = this");
    testConversion("this.foo()");
    testConversion("var x = this.foo()");
    testConversion("this.bar");
    testConversion("var x = this.bar()");
    testConversion("switch(this) {}");
    testConversion("x + this");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThrow
  public void testThrow() throws Exception {
    testConversion("throw e");
    testConversion("throw 2 + 3 * 4");
    testConversion("throw (function () {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}})()");
    testConversion("throw f(x)");
    testConversion("throw f(f(f(x)))");
    testConversion("throw (f(f(x), y))()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTry
  public void testTry() throws Exception {
    testConversion("try {} catch (e) {}");
    testConversion("try {;} catch (e) {;}");
    testConversion("try {var x = 0; y / x} catch (e) {f(e)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} catch (e) {f(x)}");

    testConversion("try {} finally {}");
    testConversion("try {;} finally {;}");
    testConversion("try {var x = 0; y / x} finally {f(y)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} finally {f(x)}");

    testConversion("try {} catch (e) {} finally {}");
    testConversion("try {;} catch (e) {;} finally {;}");
    testConversion("try {var x = 0; y / x} catch (e) {;} finally {;}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; h(q)} "
        + "catch (e) {f(x)} finally {f(x)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTypeof
  public void testTypeof() throws Exception {
    testConversion("typeof undefined");
    testConversion("typeof null");
    testConversion("typeof 1");
    testConversion("typeof 'a'");
    testConversion("typeof false");

    testConversion("typeof Null()");
    testConversion("typeof Number(1)");
    testConversion("typeof String('a')");
    testConversion("typeof Boolean(0)");

    testConversion("typeof x");
    testConversion("typeof new A()");
    testConversion("typeof new A(x)");
    testConversion("typeof f(x)");
    testConversion("typeof (function() {})()");
    testConversion("typeof 2 + 3 * 4");

    testConversion("typeof typeof x");
    testConversion("typeof typeof typeof x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testUnaryExpressions
  public void testUnaryExpressions() throws Exception {
    testConversion("!x");
    testConversion("!null");
    testConversion("!3.14");
    testConversion("!true");

    testConversion("~x");
    testConversion("~null");
    testConversion("~3.14");
    testConversion("~true");

    testConversion("+x");
    testConversion("+null");
    testConversion("+3.14");
    testConversion("+true");

    testConversion("-x");
    testConversion("-null");
    testConversion("-true");

    testConversion("!~+-z");
    testConversion("void x");
    testConversion("void null");
    testConversion("void void !x");
    testConversion("void (x + 1)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testVarDeclarations
  public void testVarDeclarations() throws Exception {
    testConversion("var x");
    testConversion("var x = 1");
    testConversion("var x = 1 + 1");
    testConversion("var x = 'a' + 'b'");

    testConversion("var x, y, z");
    testConversion("var x = 2, y = 2 * x, z");

    testConversion("var x = function () {}");
    testConversion("var x = function f() {}");
    testConversion("var x = function f(arg1, arg2) {}");

    testConversion("var x = function f(arg1, arg2) {1}");
    testConversion("var x = function f(arg1, arg2) {x}");
    testConversion("var x = function f(arg1, arg2) {x = 2 * 3}");

    testConversion("var x = function f() {var x}");
    testConversion("var x = function f() {var y = (z + 2) * q}");

    testConversion("var x = function f(a, b) {"
        + "var y = function g(a, b) {z = a + b}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWhile
  public void testWhile() throws Exception {
     testConversion("while (true) {;}");
     testConversion("while (true) {f()}");
     testConversion("while (f(x, y)) {break;}");
     testConversion("while (f(f(f(x, y)))) {}");
     testConversion("while ((f(f(f(x, y))))()) {}");

     testConversion("while (x--) {2 + 3; q = 2 + 3; var v = y * z; "
         + "g = function(a) {true; var b = a + 1; return a * a}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWith
  public void testWith() throws Exception {
     testConversion("with ({}) {}");
     testConversion("with ({}) {;}");
     testConversion("with (x) {}");
     testConversion("with (x) {f(x)}");
     testConversion("with ({a: function f() {}}) {f(1)}");
     testConversion("with ({z: function f() {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}}}) {f(1)}");
     testConversion("with (x in X) {x++}");
   }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
