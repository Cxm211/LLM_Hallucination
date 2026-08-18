// buggy code
  public final boolean isEmptyType() {
    return isNoType() || isNoObjectType() || isNoResolvedType();
  }

// relevant test
// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments10
  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments11
  public void testFunctionArguments11() throws Exception {
    testTypes(
        "function a(a,opt_a,b,c,var_args,d) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments12
  public void testFunctionArguments12() throws Exception {
    testTypes("function bar(baz){}",
        "parameter foo does not appear in bar's parameter list");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments13
  public void testFunctionArguments13() throws Exception {
    
    testTypes(
        " function u() { return true; }" +
        "" +
        "function f(b) { if (u()) { b = null; } return b; }",
        "inconsistent return type\n" +
        "found   : (boolean|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments14
  public void testFunctionArguments14() throws Exception {
    testTypes(
        " function f(x, opt_y, var_args) {}" +
        "f('3'); f('3', 2); f('3', 2, true); f('3', 2, true, false);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments15
  public void testFunctionArguments15() throws Exception {
    testTypes(
        "" +
        "function g(f) { f(1, 2); }",
        "Function f: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrintFunctionName1
  public void testPrintFunctionName1() throws Exception {
    
    testTypes(
        "var goog = {}; goog.run = function(f) {};" +
        "goog.run();",
        "Function goog.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrintFunctionName2
  public void testPrintFunctionName2() throws Exception {
    testTypes(
        " var Foo = function() {}; " +
        "Foo.prototype.run = function(f) {};" +
        "(new Foo).run();",
        "Function Foo.prototype.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference1
  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference2
  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference3
  public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference4
  public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference5
  public void testFunctionInference5() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference6
  public void testFunctionInference6() throws Exception {
    testFunctionType(
        "function f(opt_a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference7
  public void testFunctionInference7() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference8
  public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference9
  public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference10
  public void testFunctionInference10() throws Exception {
    testFunctionType(
        "" +
        "var f = function(a,b) {};",
        "function (this:Date, ?, boolean): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference11
  public void testFunctionInference11() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference12
  public void testFunctionInference12() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference13
  public void testFunctionInference13() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(f){};",
        "eatFoo",
        "function (goog.Foo): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference14
  public void testFunctionInference14() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(){ return new goog.Foo; };",
        "eatFoo",
        "function (): goog.Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference15
  public void testFunctionInference15() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "f.prototype.foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference16
  public void testFunctionInference16() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference17
  public void testFunctionInference17() throws Exception {
    testFunctionType(
        " function f() {}" +
        "function abstractMethod() {}" +
        " f.prototype.foo = abstractMethod;",
        "(new f).foo",
        "function (this:f, number): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference18
  public void testFunctionInference18() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.eatWithDate;",
        "goog.eatWithDate",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference19
  public void testFunctionInference19() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (string): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference20
  public void testFunctionInference20() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction1
  public void testInnerFunction1() throws Exception {
    testTypes(
        "function f() {" +
        "  var x = 3;\n" +
        " function g() { x = null; }" +
        " return x;" +
        "}",
        "assignment\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction2
  public void testInnerFunction2() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = null;\n" +
        " function g() { x = 3; }" +
        " g();" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : (null|number)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction3
  public void testInnerFunction3() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = 3;\n" +
        " \n" +
        " function g() { x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction4
  public void testInnerFunction4() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = '3';\n" +
        " \n" +
        " function g() { x = 3; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction5
  public void testInnerFunction5() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = 3;\n" +
        " " +
        " function g() { var x = 3;x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction6
  public void testInnerFunction6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction7
  public void testInnerFunction7() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " " +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction8
  public void testInnerFunction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " function x() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction9
  public void testInnerFunction9() throws Exception {
    testTypes(
        "function f() {" +
        " var x = 3;\n" +
        " function g() { x = null; };\n" +
        " function h() { return x == null; }" +
        " return h();" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling1
  public void testAbstractMethodHandling1() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        "abstractFn(1);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling2
  public void testAbstractMethodHandling2() throws Exception {
    testTypes(
        "var abstractFn = function() {};" +
        "abstractFn(1);",
        "Function abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling3
  public void testAbstractMethodHandling3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        "goog.abstractFn(1);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling4
  public void testAbstractMethodHandling4() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.abstractFn = function() {};" +
        "goog.abstractFn(1);",
        "Function goog.abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling5
  public void testAbstractMethodHandling5() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        " var f = abstractFn;" +
        "f('x');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling6
  public void testAbstractMethodHandling6() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        " goog.f = abstractFn;" +
        "goog.f('x');",
        "actual parameter 1 of goog.f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference1
  public void testMethodInference1() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function() { return 3; };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference2
  public void testMethodInference2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.F = function() {};" +
        " goog.F.prototype.foo = " +
        "    function() { return 3; };" +
        " " +
        "goog.G = function() {};" +
        " goog.G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference3
  public void testMethodInference3() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference4
  public void testMethodInference4() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(y) { return y; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference5
  public void testMethodInference5() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 'x'; };" +
        " " +
        "function G() {}" +
        " G.prototype.num = 3;" +
        " " +
        "G.prototype.foo = function(y) { return this.num + y; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference6
  public void testMethodInference6() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function(x) { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { };" +
        "(new G()).foo(1);",
        "Function G.prototype.foo: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference7
  public void testMethodInference7() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function(x, y) { };" +
        "(new G()).foo();",
        "Function G.prototype.foo: called with 0 argument(s). " +
        "Function requires at least 2 argument(s) " +
        "and no more than 2 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference8
  public void testMethodInference8() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(a, opt_b, var_args) { };" +
        "(new G()).foo();",
        "Function G.prototype.foo: called with 0 argument(s). " +
        "Function requires at least 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference9
  public void testMethodInference9() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(a, var_args, opt_b) { };",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticMethodDeclaration1
  public void testStaticMethodDeclaration1() throws Exception {
    testTypes(
        " function F() { F.foo(true); }" +
        " F.foo = function(x) {};",
        "actual parameter 1 of F.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticMethodDeclaration2
  public void testStaticMethodDeclaration2() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        " goog.foo = function(x) {};",
        "actual parameter 1 of goog.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticMethodDeclaration3
  public void testStaticMethodDeclaration3() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        "goog.foo = function() {};",
        "Function goog.foo: called with 1 argument(s). Function requires " +
        "at least 0 argument(s) and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl1
  public void testDuplicateStaticMethodDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (number): undefined, " +
        "original definition at [testcode]:1 " +
        "with type function (number): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl2
  public void testDuplicateStaticMethodDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " " +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl3
  public void testDuplicateStaticMethodDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl4
  public void testDuplicateStaticMethodDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl5
  public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl1
  public void testDuplicateStaticPropertyDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl2
  public void testDuplicateStaticPropertyDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl3
  public void testDuplicateStaticPropertyDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl4
  public void testDuplicateStaticPropertyDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl5
  public void testDuplicateStaticPropertyDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl6
  public void testDuplicateStaticPropertyDecl6() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = 'y';" +
        " goog.foo = 'x';");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl7
  public void testDuplicateStaticPropertyDecl7() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl8
  public void testDuplicateStaticPropertyDecl8() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " function EventCopy() {}" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl9
  public void testDuplicateStaticPropertyDecl9() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function EventCopy() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateLocalVarDecl
  public void testDuplicateLocalVarDecl() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {  var x = ''; }",
        "variable x redefined with type string, " +
        "original definition at [testcode]:2 with type number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration1
  public void testStubFunctionDeclaration1() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.prototype.foo;",
        "(new f).foo",
        "function (this:f, number, string): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration2
  public void testStubFunctionDeclaration2() throws Exception {
    testExternFunctionType(
        
        " function f() {};" +
        " f.subclass;",
        "f.subclass",
        "function (new:f.subclass): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration3
  public void testStubFunctionDeclaration3() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.foo;",
        "f.foo",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration4
  public void testStubFunctionDeclaration4() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        "function (this:f): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration5
  public void testStubFunctionDeclaration5() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration6
  public void testStubFunctionDeclaration6() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo;",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration7
  public void testStubFunctionDeclaration7() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo = function() {};",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration8
  public void testStubFunctionDeclaration8() throws Exception {
    
    testFunctionType(
        " var f = function() {}; ",
        "f",
        createNullableType(U2U_CONSTRUCTOR_TYPE).
          restrictByNotNullOrUndefined().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration9
  public void testStubFunctionDeclaration9() throws Exception {
    testFunctionType(
        " var f; ",
        "f",
        "function (): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration10
  public void testStubFunctionDeclaration10() throws Exception {
    testFunctionType(
        " var f = function(x) {};",
        "f",
        "function (number): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNestedFunctionInference1
  public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        " function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeRedefinition
  public void testTypeRedefinition() throws Exception {
    testTypes("a={}; a.A = {ZOR:'b'};"
        + " a.A = function() {}",
        "variable a.A redefined with type function (new:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn1
  public void testIn1() throws Exception {
    testTypes("'foo' in Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn2
  public void testIn2() throws Exception {
    testTypes("3 in Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn3
  public void testIn3() throws Exception {
    testTypes("undefined in Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn4
  public void testIn4() throws Exception {
    testTypes("Date in Object",
        "left side of 'in'\n" +
        "found   : function (new:Date, ?, ?, ?, ?, ?, ?, ?): string\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn5
  public void testIn5() throws Exception {
    testTypes("'x' in null",
        "'in' requires an object\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn6
  public void testIn6() throws Exception {
    testTypes(
        "" +
        "function g(x) {}" +
        "g(1 in {});",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn7
  public void testIn7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  return g(x.foo) in {};" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison2
  public void testComparison2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "if (a!==b) {}",
        "condition always evaluates to the same value\n" +
        "left : number\n" +
        "right: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison3
  public void testComparison3() throws Exception {
    
    testTypes("var a;" +
        "var b = a == null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison4
  public void testComparison4() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a == b");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison5
  public void testComparison5() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison6
  public void testComparison6() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a != b",
        "condition always evaluates to false\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison7
  public void testComparison7() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison8
  public void testComparison8() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null || a[1] == undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison9
  public void testComparison9() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison10
  public void testComparison10() throws Exception {
    testTypes(" var a = [];" +
        "a[0] === null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnumStaticMethod1
  public void testEnumStaticMethod1() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "Foo.method(true);",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnumStaticMethod2
  public void testEnumStaticMethod2() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "function f() { Foo.method(true); }",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum1
  public void testEnum1() throws Exception {
    testTypes("var a={BB:1,CC:2};\n" +
        "var d;d=a.BB;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum2
  public void testEnum2() throws Exception {
    testTypes("var a={b:1}",
        "enum key b must be a syntactic constant");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum3
  public void testEnum3() throws Exception {
    testTypes("var a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum4
  public void testEnum4() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum5
  public void testEnum5() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: (String|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum6
  public void testEnum6() throws Exception {
    testTypes("var a={BB:1,CC:2};\nvar d;d=a.BB;",
        "assignment\n" +
        "found   : a.<number>\n" +
        "required: Array");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum7
  public void testEnum7() throws Exception {
    testTypes("var a={AA:1,BB:2,CC:3};" +
        "var b=a.D;",
        "element D does not exist on this enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum8
  public void testEnum8() throws Exception {
    testTypes("var a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum9
  public void testEnum9() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum10
  public void testEnum10() throws Exception {
    testTypes(
        "" +
        "goog.K = { A : 3 };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum11
  public void testEnum11() throws Exception {
    testTypes(
        "" +
        "goog.K = { 502 : 3 };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum12
  public void testEnum12() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum13
  public void testEnum13() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;",
        "incompatible enum element types\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum14
  public void testEnum14() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.FOO;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum15
  public void testEnum15() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.BAR;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum16
  public void testEnum16() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum17
  public void testEnum17() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum18
  public void testEnum18() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum19
  public void testEnum19() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: E.<number>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum20
  public void testEnum20() throws Exception {
    testTypes(" var E = {A: 1, B: 2}; var x = []; x[E.A] = 0;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum21
  public void testEnum21() throws Exception {
    Node n = parseAndTypeCheck(
        " var E = {A : 'a', B : 'b'};\n" +
        " function f(x) { return x; }");
    Node nodeX = n.getLastChild().getLastChild().getLastChild().getLastChild();
    JSType typeE = nodeX.getJSType();
    assertFalse(typeE.isObject());
    assertFalse(typeE.isNullable());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum22
  public void testEnum22() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum23
  public void testEnum23() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum24
  public void testEnum24() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<(Object|null|undefined)>\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum25
  public void testEnum25() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum26
  public void testEnum26() throws Exception {
    testTypes("var a = {};  a.B = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum27
  public void testEnum27() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A == x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum28
  public void testEnum28() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A.B == x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum29
  public void testEnum29() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum30
  public void testEnum30() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum31
  public void testEnum31() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: A.<number>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum32
  public void testEnum32() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum34
  public void testEnum34() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f(x) { return x == A.B; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum35
  public void testEnum35() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return a.b.C; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum36
  public void testEnum36() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return 1; }",
              "inconsistent return type\n" +
              "found   : number\n" +
              "required: a.b.<number>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum37
  public void testEnum37() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.a = {};" +
        " var b = goog.a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum38
  public void testEnum38() throws Exception {
    testTypes(
        " var MyEnum = {};" +
        " function f(x) {}",
        "Parse error. Cycle detected in inheritance chain " +
        "of type MyEnum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum39
  public void testEnum39() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testEnum40
  public void testEnum40() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAliasedEnum1
  public void testAliasedEnum1() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAliasedEnum2
  public void testAliasedEnum2() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAliasedEnum3
  public void testAliasedEnum3() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAliasedEnum4
  public void testAliasedEnum4() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAliasedEnum5
  public void testAliasedEnum5() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : YourEnum.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsEnumUse1
  public void testBackwardsEnumUse1() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsEnumUse2
  public void testBackwardsEnumUse2() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};",
        "inconsistent return type\n" +
        "found   : MyEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsEnumUse3
  public void testBackwardsEnumUse3() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsEnumUse4
  public void testBackwardsEnumUse4() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "inconsistent return type\n" +
        "found   : YourEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsEnumUse5
  public void testBackwardsEnumUse5() throws Exception {
    testTypes(
        " function f() { return MyEnum.BAR; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsConstructor1
  public void testBackwardsConstructor1() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var Foo = function(x) {};",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBackwardsConstructor2
  public void testBackwardsConstructor2() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var YourFoo = function(x) {};" +
        "" +
        "var Foo = YourFoo;",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMinimalConstructorAnnotation
  public void testMinimalConstructorAnnotation() throws Exception {
    testTypes("function Foo(){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends1
  public void testGoodExtends1() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends2
  public void testGoodExtends2() throws Exception {
    testTypes("function derived() {}\n" +
        "function base() {}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends3
  public void testGoodExtends3() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends4
  public void testGoodExtends4() throws Exception {
    
    
    
    Node n = parseAndTypeCheck(
        "var goog = {};\n" +
        "goog.Base = function(){};\n" +
        "goog.Derived = function(){};\n");
    Node subTypeName = n.getLastChild().getLastChild().getFirstChild();
    assertEquals("goog.Derived", subTypeName.getQualifiedName());

    FunctionType subCtorType =
        (FunctionType) subTypeName.getNext().getJSType();
    assertEquals("goog.Derived", subCtorType.getInstanceType().toString());

    JSType superType = subCtorType.getPrototype().getImplicitPrototype();
    assertEquals("goog.Base", superType.toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends5
  public void testGoodExtends5() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends6
  public void testGoodExtends6() throws Exception {
    testFunctionType(
        CLOSURE_DEFS +
        "function base() {}\n" +
        " " +
        "  base.prototype.foo = function() { return 1; };\n" +
        "function derived() {}\n" +
        "goog.inherits(derived, base);",
        "derived.superClass_.foo",
        "function (this:base): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends7
  public void testGoodExtends7() throws Exception {
    testFunctionType(
        "Function.prototype.inherits = function(x) {};" +
        "function base() {}\n" +
        "function derived() {}\n" +
        "derived.inherits(base);",
        "(new derived).constructor",
        "function (new:derived): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends8
  public void testGoodExtends8() throws Exception {
    testTypes(" function Sub() {}" +
        " function f() { return (new Sub()).foo; }" +
        " function Base() {}" +
        " Base.prototype.foo = true;",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends9
  public void testGoodExtends9() throws Exception {
    testTypes(
        " function Super() {}" +
        "Super.prototype.foo = function() {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends10
  public void testGoodExtends10() throws Exception {
    testTypes(
        " function Super() {}" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " function foo() { return new Sub(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodExtends11
  public void testGoodExtends11() throws Exception {
    testTypes(
        " function Super() {}" +
        " Super.prototype.foo = function(x) {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        "(new Sub()).foo(0);",
        "actual parameter 1 of Super.prototype.foo " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadExtends1
  public void testBadExtends1() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n",
        "Parse error. Unknown type not_base");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadExtends2
  public void testBadExtends2() throws Exception {
    testTypes("function base() {\n" +
        "\n" +
        "this.baseMember = new Number(4);\n" +
        "}\n" +
        "function derived() {}\n" +
        "\n" +
        "function foo(x){ }\n" +
        "var y;\n" +
        "foo(y.baseMember);\n",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadExtends3
  public void testBadExtends3() throws Exception {
    testTypes("function base() {}",
        "@extends used without @constructor or @interface for base");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testLateExtends
  public void testLateExtends() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function Foo() {}\n" +
        "Foo.prototype.foo = function() {};\n" +
        "function Bar() {}\n" +
        "goog.inherits(Foo, Bar);\n",
        "Missing @extends tag on type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSuperclassMatch
  public void testSuperclassMatch() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSuperclassMatchWithMixin
  public void testSuperclassMatchWithMixin() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Baz = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.mixin = function(y){};" +
        "Bar.inherits(Foo);\n" +
        "Bar.mixin(Baz);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSuperclassMismatch1
  public void testSuperclassMismatch1() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSuperclassMismatch2
  public void testSuperclassMismatch2() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function(){};\n" +
        " var Bar = function(){};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSuperClassDefinedAfterSubClass1
  public void testSuperClassDefinedAfterSubClass1() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " function Base() {}" +
        " " +
        "function foo(x) { return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSuperClassDefinedAfterSubClass2
  public void testSuperClassDefinedAfterSubClass2() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " " +
        "function foo(x) { return x; }" +
        " function Base() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDirectPrototypeAssignment1
  public void testDirectPrototypeAssignment1() throws Exception {
    testTypes(
        " function Base() {}" +
        "Base.prototype.foo = 3;" +
        " function A() {}" +
        "A.prototype = new Base();" +
        " function foo() { return (new A).foo; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDirectPrototypeAssignment2
  public void testDirectPrototypeAssignment2() throws Exception {
    
    
    testTypes(
        " function Base() {}" +
        " function A() {}" +
        "A.prototype = new Base();" +
        "A.prototype.foo = 3;" +
        " function foo() { return (new Base).foo; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodImplements1
  public void testGoodImplements1() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodImplements2
  public void testGoodImplements2() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGoodImplements3
  public void testGoodImplements3() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadImplements1
  public void testBadImplements1() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}",
        "Parse error. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadImplements2
  public void testBadImplements2() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "@implements used without @constructor or @interface for f");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadInterfaceExtends1
  public void testBadInterfaceExtends1() throws Exception {
    testTypes("function A() {}",
        "Parse error. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadInterfaceExtends2
  public void testBadInterfaceExtends2() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadInterfaceExtends3
  public void testBadInterfaceExtends3() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadInterfaceExtends4
  public void testBadInterfaceExtends4() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadInterfaceExtends5
  public void testBadInterfaceExtends5() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadImplementsAConstructor
  public void testBadImplementsAConstructor() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadImplementsNonInterfaceType
  public void testBadImplementsNonInterfaceType() throws Exception {
    testTypes("function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadImplementsNonObjectType
  public void testBadImplementsNonObjectType() throws Exception {
    testTypes("function S() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment1
  public void testInterfaceAssignment1() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment2
  public void testInterfaceAssignment2() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;",
        "initializing variable\n" +
        "found   : T\n" +
        "required: I");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment3
  public void testInterfaceAssignment3() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment4
  public void testInterfaceAssignment4() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment5
  public void testInterfaceAssignment5() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment6
  public void testInterfaceAssignment6() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var i1 = new T();\n" +
        "var i2 = i1;\n",
        "initializing variable\n" +
        "found   : I1\n" +
        "required: I2");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment7
  public void testInterfaceAssignment7() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n" +
        "i1 = i2;\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment8
  public void testInterfaceAssignment8() throws Exception {
    testTypes("var I = function() {};\n" +
        "var i;\n" +
        "var o = i;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment9
  public void testInterfaceAssignment9() throws Exception {
    testTypes("var I = function() {};\n" +
        "function f() { return null; }\n" +
        "var i = f();\n",
        "initializing variable\n" +
        "found   : (I|null|undefined)\n" +
        "required: I");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment10
  public void testInterfaceAssignment10() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment11
  public void testInterfaceAssignment11() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2|T)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment12
  public void testInterfaceAssignment12() throws Exception {
    testTypes("var I = function() {};\n" +
              "var T1 = function() {};\n" +
              "var T2 = function() {};\n" +
              "function f() { return new T2(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment13
  public void testInterfaceAssignment13() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "function Super() {};\n" +
        "Super.prototype.foo = " +
        "function() { return new T(); };\n" +
        "function Sub() {}\n" +
        "Sub.prototype.foo = " +
        "function() { return new T(); };\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetprop1
  public void testGetprop1() throws Exception {
    testTypes("function foo(){foo().bar;}",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess1
  public void testArrayAccess1() throws Exception {
    testTypes("var a = []; var b = a['hi'];");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess2
  public void testArrayAccess2() throws Exception {
    testTypes("var a = []; var b = a[[1,2]];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess3
  public void testArrayAccess3() throws Exception {
    testTypes("var bar = [];" +
        "function baz(){};" +
        "var foo = bar[baz()];",
        "array access\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess4
  public void testArrayAccess4() throws Exception {
    testTypes("function foo(){};var bar = foo()[foo()];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess6
  public void testArrayAccess6() throws Exception {
    testTypes("var bar = null[1];",
        "only arrays or objects can be accessed\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess7
  public void testArrayAccess7() throws Exception {
    testTypes("var bar = void 0; bar[0];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess8
  public void testArrayAccess8() throws Exception {
    
    
    testTypes("var bar = void 0; bar[0]; bar[1];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess
  public void testPropAccess() throws Exception {
    testTypes("var f = function(x) {\n" +
        "var o = String(x);\n" +
        "if (typeof o['a'] != 'undefined') { return o['a']; }\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess2
  public void testPropAccess2() throws Exception {
    testTypes("var bar = void 0; bar.baz;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess3
  public void testPropAccess3() throws Exception {
    
    
    testTypes("var bar = void 0; bar.baz; bar.bax;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess4
  public void testPropAccess4() throws Exception {
    testTypes(" function f(x) { return x['hi']; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase1
  public void testSwitchCase1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "switch(a){case b:;}",
        "case expression doesn't match switch\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase2
  public void testSwitchCase2() throws Exception {
    testTypes("var a = null; switch (typeof a) { case 'foo': }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar1
  public void testVar1() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("var a = null");

    assertEquals(createUnionType(STRING_TYPE, NULL_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar2
  public void testVar2() throws Exception {
    testTypes(" var a = function(){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar3
  public void testVar3() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = 3;");

    assertEquals(NUMBER_TYPE, p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar4
  public void testVar4() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var a = 3; a = 'string';");

    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar5
  public void testVar5() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';" +
        "var a = goog.foo;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar6
  public void testVar6() throws Exception {
    testTypes(
        "function f() {" +
        "  return function() {" +
        "    " +
        "    var a = 7;" +
        "  };" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar7
  public void testVar7() throws Exception {
    testTypes("var a, b;",
        "declaration of multiple variables with shared type information");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar8
  public void testVar8() throws Exception {
    testTypes("var a, b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar9
  public void testVar9() throws Exception {
    testTypes("var a;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar10
  public void testVar10() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar11
  public void testVar11() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar12
  public void testVar12() throws Exception {
    testTypes("var foo = 'abc', " +
        "bar = 5;",
        new String[] {
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date",
        "initializing variable\n" +
        "found   : number\n" +
        "required: RegExp"});
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar13
  public void testVar13() throws Exception {
    
    testTypes("var a,a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar14
  public void testVar14() throws Exception {
    testTypes(" function f() { var x; return x; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar15
  public void testVar15() throws Exception {
    testTypes("" +
        "function f() { var x = x || {}; return x; }",
        "inconsistent return type\n" +
        "found   : {}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign1
  public void testAssign1() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign2
  public void testAssign2() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign3
  public void testAssign3() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 4;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign4
  public void testAssign4() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssignInference
  public void testAssignInference() throws Exception {
    testTypes(
        "" +
        "function f(x) {" +
        "  var y = null;" +
        "  y = x[0];" +
        "  if (y == null) { return 4; } else { return 6; }" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr1
  public void testOr1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b || undefined;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr2
  public void testOr2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b || undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr3
  public void testOr3() throws Exception {
    testTypes("var a;" +
        "var c = a || 3;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr4
  public void testOr4() throws Exception {
     testTypes("var x;x=null || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr5
  public void testOr5() throws Exception {
     testTypes("var x;x=undefined || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd1
  public void testAnd1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b && undefined;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd2
  public void testAnd2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b && undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd3
  public void testAnd3() throws Exception {
    testTypes("var a;" +
        "var c = a && undefined;",
        "initializing variable\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd4
  public void testAnd4() throws Exception {
    testTypes("function f(x){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(y) }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd5
  public void testAnd5() throws Exception {
    testTypes("function f(x,y){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(x, y) }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd6
  public void testAnd6() throws Exception {
    testTypes("function f(x){};\n" +
        "var x;\n" +
        "if (x && f(x)) { f(x) }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd7
  public void testAnd7() throws Exception {
    
    
    
    
    testTypes("var x; if (x && x) {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHook
  public void testHook() throws Exception {
    testTypes("function foo(){ var x=foo()?a:b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType1
  public void testHookRestrictsType1() throws Exception {
    testTypes("" +
        "function f() { return null;}" +
        " var a = f();" +
        "" +
        "var b = a ? a : 'default';");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType2
  public void testHookRestrictsType2() throws Exception {
    testTypes("" +
        "var a = null;" +
        "" +
        "var b = a ? null : a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType3
  public void testHookRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = (!a) ? a : null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType4
  public void testHookRestrictsType4() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a != null ? a : true;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType5
  public void testHookRestrictsType5() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? a : undefined;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType6
  public void testHookRestrictsType6() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? 5 : a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType7
  public void testHookRestrictsType7() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == undefined ? 5 : a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWhileRestrictsType1
  public void testWhileRestrictsType1() throws Exception {
    testTypes(" function g(x) {}" +
        "\n" +
        "function f(x) {\n" +
        "while (x) {\n" +
        "if (g(x)) { x = 1; }\n" +
        "x = x-1;\n}\n}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : number\n" +
        "required: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWhileRestrictsType2
  public void testWhileRestrictsType2() throws Exception {
    testTypes("\n" +
        "function f(x) {\nvar y = 0;" +
        "while (x) {\n" +
        "y = x;\n" +
        "x = x-1;\n}\n" +
        "return y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions1
  public void testHigherOrderFunctions1() throws Exception {
    testTypes(
        "var f;" +
        "f(true);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions2
  public void testHigherOrderFunctions2() throws Exception {
    testTypes(
        "var f;" +
        "var a = f();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions3
  public void testHigherOrderFunctions3() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions4
  public void testHigherOrderFunctions4() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias1
  public void testConstructorAlias1() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " Foo.prototype.bar = 3;" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return (new FooAlias()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias2
  public void testConstructorAlias2() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " FooAlias.prototype.bar = 3;" +
        " function foo() { " +
        "  return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias3
  public void testConstructorAlias3() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " Foo.prototype.bar = 3;" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return (new FooAlias()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias4
  public void testConstructorAlias4() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "var FooAlias = Foo;" +
        " FooAlias.prototype.bar = 3;" +
        " function foo() { " +
        "  return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias5
  public void testConstructorAlias5() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new Foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias6
  public void testConstructorAlias6() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias7
  public void testConstructorAlias7() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias8
  public void testConstructorAlias8() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function(x) {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias9
  public void testConstructorAlias9() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function(x) {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias10
  public void testConstructorAlias10() throws Exception {
    testTypes(
        " var Foo = function(x) {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure1
  public void testClosure1() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDef(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure2
  public void testClosure2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure3
  public void testClosure3() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDefAndNotNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure4
  public void testClosure4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDef(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure5
  public void testClosure5() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure6
  public void testClosure6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDefAndNotNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn1
  public void testReturn1() throws Exception {
    testTypes("function foo(){ return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn2
  public void testReturn2() throws Exception {
    testTypes("function foo(){ return; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn3
  public void testReturn3() throws Exception {
    testTypes("function foo(){ return 'abc'; }",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn4
  public void testReturn4() throws Exception {
    testTypes("\n function a(){return new Array();}",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn5
  public void testReturn5() throws Exception {
    testTypes("function n(n){return};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn6
  public void testReturn6() throws Exception {
    testTypes(
        "" +
        "function a(opt_a) { return opt_a }",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn7
  public void testReturn7() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn8
  public void testReturn8() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.prototype.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis1
  public void testThis1() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){};" +
        "goog.A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis2
  public void testThis2() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "" +
        "goog.A.prototype.n = function() { return this.foo };",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis3
  public void testThis3() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "  this.foo = 5;" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis4
  public void testThis4() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "goog.A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis5
  public void testThis5() throws Exception {
    testTypes("function h() { return this }",
        "inconsistent return type\n" +
        "found   : Date\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis6
  public void testThis6() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.A = function(){ return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis7
  public void testThis7() throws Exception {
    testTypes("function A(){};" +
        "A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis8
  public void testThis8() throws Exception {
    testTypes("function A(){" +
        "  this.foo = null;" +
        "};" +
        "A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis9
  public void testThis9() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        " A.bar = function() { return this.foo; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis10
  public void testThis10() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        "" +
        "A.bar = function() { return this.foo; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis1
  public void testGlobalThis1() throws Exception {
    testTypes(" function Window() {}" +
        " " +
        "Window.prototype.alert = function(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of Window.prototype.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis2
  public void testGlobalThis2() throws Exception {
    testTypes(" function Bindow() {}" +
        " " +
        "Bindow.prototype.alert = function(msg) {};" +
        "this.alert = 3;" +
        "(new Bindow()).alert(this.alert)",
        "actual parameter 1 of Bindow.prototype.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis3
  public void testGlobalThis3() throws Exception {
    testTypes(
        " " +
        "function alert(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of this.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis4
  public void testGlobalThis4() throws Exception {
    testTypes(
        " " +
        "var alert = function(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of this.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }
