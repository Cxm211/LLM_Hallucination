// buggy code
  public void visit(NodeTraversal t, Node n, Node parent) {
    // VOID nodes appear when there are extra semicolons at the BLOCK level.
    // I've been unable to think of any cases where this indicates a bug,
    // and apparently some people like keeping these semicolons around,
    // so we'll allow it.
    if (n.isEmpty() ||
        n.isComma()) {
      return;
    }

    if (parent == null) {
      return;
    }

    // Do not try to remove a block or an expr result. We already handle
    // these cases when we visit the child, and the peephole passes will
    // fix up the tree in more clever ways when these are removed.
    if (n.isExprResult()) {
      return;
    }

    // This no-op statement was there so that JSDoc information could
    // be attached to the name. This check should not complain about it.
    if (n.isQualifiedName() && n.getJSDocInfo() != null) {
      return;
    }

    boolean isResultUsed = NodeUtil.isExpressionResultUsed(n);
    boolean isSimpleOp = NodeUtil.isSimpleOperatorType(n.getType());
    if (parent.getType() == Token.COMMA) {
      if (isResultUsed) {
        return;
      }
      if (n == parent.getLastChild()) {
        for (Node an : parent.getAncestors()) {
          int ancestorType = an.getType();
          if (ancestorType == Token.COMMA) continue;
          if (ancestorType != Token.EXPR_RESULT && ancestorType != Token.BLOCK) return;
          else break;
        }
      }
    } else if (parent.getType() != Token.EXPR_RESULT && parent.getType() != Token.BLOCK) {
      if (! (parent.getType() == Token.FOR && parent.getChildCount() == 4 && (n == parent.getFirstChild() || n == parent.getFirstChild().getNext().getNext()))) {
        return;
      }
    }
    if (
        (isSimpleOp || !NodeUtil.mayHaveSideEffects(n, t.getCompiler()))) {
      String msg = "This code lacks side-effects. Is there a bug?";
      if (n.isString()) {
        msg = "Is there a missing '+' on the previous line?";
      } else if (isSimpleOp) {
        msg = "The result of the '" + Token.name(n.getType()).toLowerCase() +
            "' operator is not being used.";
      }

      t.getCompiler().report(
          t.makeError(n, level, USELESS_CODE_ERROR, msg));
      // TODO(johnlenz): determine if it is necessary to
      // try to protect side-effect free statements as well.
      if (!NodeUtil.isStatement(n)) {
        problemNodes.add(n);
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.LooseTypeCheckTest::testNew3
  public void testNew3() throws Exception {
    testTypes("new Date()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew4
  public void testNew4() throws Exception {
    testTypes("function A(){}; new A();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew5
  public void testNew5() throws Exception {
    testTypes("function A(){}; new A();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew6
  public void testNew6() throws Exception {
    TypeCheckResult p =
      parseAndTypeCheckWithScope("function A(){};" +
      "var a = new A();");

    JSType aType = p.scope.getVar("a").getType();
    assertTrue(aType instanceof ObjectType);
    ObjectType aObjectType = (ObjectType) aType;
    assertEquals("A", aObjectType.getConstructor().getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew7
  public void testNew7() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "if (opt_constructor) { new opt_constructor; }" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew8
  public void testNew8() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new opt_constructor;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew9
  public void testNew9() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew10
  public void testNew10() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.Foo = function (opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew11
  public void testNew11() throws Exception {
    testTypes("" +
        "function f(c1) {" +
        "  var c2 = function(){};" +
        "  c1.prototype = new c2;" +
        "}", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew12
  public void testNew12() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = new Array();");
    Var a = p.scope.getVar("a");

    assertEquals(ARRAY_TYPE, a.getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew13
  public void testNew13() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "function FooBar(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew14
  public void testNew14() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var FooBar = function(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew15
  public void testNew15() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function(){};" +
        "var a = new goog.A();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("goog.A", a.getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew16
  public void testNew16() throws Exception {
    testTypes(
        "" +
        "function Foo(x) {}" +
        "function g() { new Foo(1); }",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName1
  public void testName1() throws Exception {
    assertEquals(VOID_TYPE, testNameNode("undefined"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName2
  public void testName2() throws Exception {
    assertEquals(OBJECT_FUNCTION_TYPE, testNameNode("Object"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName3
  public void testName3() throws Exception {
    assertEquals(ARRAY_FUNCTION_TYPE, testNameNode("Array"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName4
  public void testName4() throws Exception {
    assertEquals(DATE_FUNCTION_TYPE, testNameNode("Date"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName5
  public void testName5() throws Exception {
    assertEquals(REGEXP_FUNCTION_TYPE, testNameNode("RegExp"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation1
  public void testBitOperation1() throws Exception {
    testTypes("function foo(){ ~foo(); }",
        "operator ~ cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation2
  public void testBitOperation2() throws Exception {
    testTypes("function foo(){var a = foo()<<3;}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation3
  public void testBitOperation3() throws Exception {
    testTypes("function foo(){var a = 3<<foo();}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation4
  public void testBitOperation4() throws Exception {
    testTypes("function foo(){var a = foo()>>>3;}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation5
  public void testBitOperation5() throws Exception {
    testTypes("function foo(){var a = 3>>>foo();}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation6
  public void testBitOperation6() throws Exception {
    testTypes("function foo(){var a = foo()&3;}",
        "bad left operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation7
  public void testBitOperation7() throws Exception {
    testTypes("var x = null; x |= undefined; x &= 3; x ^= '3'; x |= true;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation8
  public void testBitOperation8() throws Exception {
    testTypes("var x = void 0; x |= new Number(3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation9
  public void testBitOperation9() throws Exception {
    testTypes("var x = void 0; x |= {};",
        "bad right operand to bitwise operator\n" +
        "found   : {}\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall1
  public void testCall1() throws Exception {
    testTypes("3();", "number expressions are not callable");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall2
  public void testCall2() throws Exception {
    testTypes("function bar(foo){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall3
  public void testCall3() throws Exception {
    
    
    testTypes("var opt_f;" +
        "var f1;" +
        "var f2 = opt_f || f1;" +
        "f2();",
        "Bad type annotation. Unknown type some.unknown.type");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall4
  public void testCall4() throws Exception {
    testTypes("var foo = function bar(a){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall5
  public void testCall5() throws Exception {
    testTypes("var foo = function bar(a){ foo('abc'); }",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall6
  public void testCall6() throws Exception {
    testTypes("function bar(foo){}" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall7
  public void testCall7() throws Exception {
    testTypes("var foo = function bar(a){};" +
        "foo('abc');",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall8
  public void testCall8() throws Exception {
    testTypes("var f;f();",
        "(Function|number) expressions are " +
        "not callable");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall9
  public void testCall9() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " var bar = function(a){};" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: goog.Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall10
  public void testCall10() throws Exception {
    testTypes("var f;f();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall11
  public void testCall11() throws Exception {
    testTypes("var f = new Function(); f();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall1
  public void testFunctionCall1() throws Exception {
    testTypes(
        " var foo = function(x) {};" +
        "foo.call(null, 3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall2
  public void testFunctionCall2() throws Exception {
    testTypes(
        " var foo = function(x) {};" +
        "foo.call(null, 'bar');",
        "actual parameter 2 of foo.call does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall3
  public void testFunctionCall3() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) { this.bar.call(null, x); };" +
        " Foo.prototype.bar;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall4
  public void testFunctionCall4() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) { this.bar.call(null, x); };" +
        " Foo.prototype.bar;",
        "actual parameter 2 of this.bar.call " +
        "does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall5
  public void testFunctionCall5() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler) { handler.call(this, x); };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall6
  public void testFunctionCall6() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler) { handler.apply(this, x); };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall7
  public void testFunctionCall7() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler, opt_context) { " +
        "  handler.call(opt_context, x);" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall8
  public void testFunctionCall8() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler, opt_context) { " +
        "  handler.apply(opt_context, x);" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast2
  public void testCast2() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = new derived();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast3
  public void testCast3() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = new base();\n",
        "initializing variable\n" +
        "found   : base\n" +
        "required: derived");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast4
  public void testCast4() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = " +
        "(new base());\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast5
  public void testCast5() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var baz = (new bar);\n",
        "invalid cast - must be a subtype or supertype\n" +
        "from: bar\n" +
        "to  : foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast6
  public void testCast6() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast7
  public void testCast7() throws Exception {
    testTypes("var x =  (new Object());",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast8
  public void testCast8() throws Exception {
    testTypes("function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast9
  public void testCast9() throws Exception {
    testTypes("var foo = {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast10
  public void testCast10() throws Exception {
    testTypes("var foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast11
  public void testCast11() throws Exception {
    testTypes("var goog = {}; goog.foo = {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast12
  public void testCast12() throws Exception {
    testTypes("var goog = {}; goog.foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast13
  public void testCast13() throws Exception {
    
    
    testClosureTypes("var goog = {}; " +
        "goog.addDependency('zzz.js', ['goog.foo'], []);" +
        "goog.foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast14
  public void testCast14() throws Exception {
    
    
    testClosureTypes("var goog = {}; " +
        "goog.addDependency('zzz.js', ['goog.bar'], []);" +
        "function f() { return  (new Object()); }",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast15
  public void testCast15() throws Exception {
    
    
    
    
    
    
    
    testTypes(
        "for (var i = 0; i < 10; i++) {" +
          "var x =  ({foo: 3});" +
          " function f(x) {}" +
          "f(x.foo);" +
          "f([].foo);" +
        "}",
        "Property foo never defined on Array");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast16
  public void testCast16() throws Exception {
    
    testTypes(" function Foo() {} \n" +
        " var x =  ({})");

    testTypes(" function Foo() {} \n" +
        " var x = ( {})");

    
    testTypes(" function Foo() {} \n" +
        " var x =  {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNestedCasts
  public void testNestedCasts() throws Exception {
    testTypes("var T = function() {};\n" +
        "var V = function() {};\n" +
        "\n" +
        "function f(b) { return b ? new T() : new V(); }\n" +
        "\n" +
        "function g(b) { return b ? true : undefined; }\n" +
        "\n" +
        "function h() {\n" +
        "return  (f( (g(true))));\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast1
  public void testNativeCast1() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(String(true));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast2
  public void testNativeCast2() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Number(true));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast3
  public void testNativeCast3() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Boolean(''));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast4
  public void testNativeCast4() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Error(''));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : Error\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadConstructorCall
  public void testBadConstructorCall() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo();",
        "Constructor function (new:Foo): undefined should be called " +
        "with the \"new\" keyword");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeof
  public void testTypeof() throws Exception {
    testTypes("function foo(){ var a = typeof foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType1
  public void testConstructorType1() throws Exception {
    testTypes("function Foo(){}" +
        "var f = new Date();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType2
  public void testConstructorType2() throws Exception {
    testTypes("function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n" +
        "var f = new Foo();\n" +
        "var n = f.bar;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType3
  public void testConstructorType3() throws Exception {
    
    
    testTypes("var f = new Foo();\n" +
        "var n = f.bar;" +
        "function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType4
  public void testConstructorType4() throws Exception {
    testTypes("function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n" +
        "var f = new Foo();\n" +
        "var n = f.bar;",
        "initializing variable\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType5
  public void testConstructorType5() throws Exception {
    testTypes("function Foo(){}\n" +
        "if (Foo){}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType6
  public void testConstructorType6() throws Exception {
    testTypes("\n" +
        "function bar() {}\n" +
        "function _foo() {\n" +
        " \n" +
        "  function f(x) {}\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType7
  public void testConstructorType7() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("function A(){};");

    JSType type = p.scope.getVar("A").getType();
    assertTrue(type instanceof FunctionType);
    FunctionType fType = (FunctionType) type;
    assertEquals("A", fType.getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnonymousType1
  public void testAnonymousType1() throws Exception {
    testTypes("function f() {}" +
        "\n" +
        "f().bar = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnonymousType2
  public void testAnonymousType2() throws Exception {
    testTypes("function f() {}" +
        "\n" +
        "f().bar = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnonymousType3
  public void testAnonymousType3() throws Exception {
    testTypes("function f() {}" +
        "\n" +
        "f().bar = {FOO: 1};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang1
  public void testBang1() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }",
        "inconsistent return type\n" +
        "found   : (Object|null|undefined)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang2
  public void testBang2() throws Exception {
    testTypes("\n" +
        "function f(x) { return x ? x : new Object(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang3
  public void testBang3() throws Exception {
    testTypes("\n" +
        "function f(x) { return  (x); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang4
  public void testBang4() throws Exception {
    testTypes("\n" +
        "function f(x, y) {\n" +
        "if (typeof x != 'undefined') { return x == y; }\n" +
        "else { return x != y; }\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang5
  public void testBang5() throws Exception {
    testTypes("\n" +
        "function f(x, y) { return !!x && x == y; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang6
  public void testBang6() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang7
  public void testBang7() throws Exception {
    testTypes("function f(x) { return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDefinePropertyOnNullableObject1
  public void testDefinePropertyOnNullableObject1() throws Exception {
    testTypes(" var n = {};\n" +
        " n.x = 1;\n" +
        "function f() { return n.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDefinePropertyOnNullableObject2
  public void testDefinePropertyOnNullableObject2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "function f(t) {\n" +
        "t.x = 1; return t.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownConstructorInstanceType1
  public void testUnknownConstructorInstanceType1() throws Exception {
    testTypes(" function g(f) { return new f(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownConstructorInstanceType2
  public void testUnknownConstructorInstanceType2() throws Exception {
    testTypes("function g(f) { return  new f(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownConstructorInstanceType3
  public void testUnknownConstructorInstanceType3() throws Exception {
    testTypes("function g(f) { var x = new f(); x.a = 1; return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownPrototypeChain
  public void testUnknownPrototypeChain() throws Exception {
    testTypes("\n" +
              "function inst(co) {\n" +
              " \n" +
              " var c = function() {};\n" +
              " c.prototype = co.prototype;\n" +
              " return new c;\n" +
              "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNamespacedConstructor
  public void testNamespacedConstructor() throws Exception {
    Node root = parseAndTypeCheck(
        "var goog = {};" +
        " goog.MyClass = function() {};" +
        " " +
        "function foo() { return new goog.MyClass(); }");

    JSType typeOfFoo = root.getLastChild().getJSType();
    assert(typeOfFoo instanceof FunctionType);

    JSType retType = ((FunctionType) typeOfFoo).getReturnType();
    assert(retType instanceof ObjectType);
    assertEquals("goog.MyClass", ((ObjectType) retType).getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComplexNamespace
  public void testComplexNamespace() throws Exception {
    String js =
      "var goog = {};" +
      "goog.foo = {};" +
      "goog.foo.bar = 5;";

    TypeCheckResult p = parseAndTypeCheckWithScope(js);

    
    JSType googScopeType = p.scope.getVar("goog").getType();
    assertTrue(googScopeType instanceof ObjectType);
    assertTrue("foo property not present on goog type",
        ((ObjectType) googScopeType).hasProperty("foo"));
    assertFalse("bar property present on goog type",
        ((ObjectType) googScopeType).hasProperty("bar"));

    
    Node varNode = p.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    JSType googNodeType = varNode.getFirstChild().getJSType();
    assertTrue(googNodeType instanceof ObjectType);

    
    assertTrue(googScopeType == googNodeType);

    
    Node getpropFoo1 = varNode.getNext().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo1.getType());
    assertEquals("goog", getpropFoo1.getFirstChild().getString());
    JSType googGetpropFoo1Type = getpropFoo1.getFirstChild().getJSType();
    assertTrue(googGetpropFoo1Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo1Type == googScopeType);

    
    JSType googFooType = ((ObjectType) googScopeType).getPropertyType("foo");
    assertTrue(googFooType instanceof ObjectType);

    
    
    Node getpropFoo2 = varNode.getNext().getNext()
        .getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo2.getType());
    assertEquals("goog", getpropFoo2.getFirstChild().getString());
    JSType googGetpropFoo2Type = getpropFoo2.getFirstChild().getJSType();
    assertTrue(googGetpropFoo2Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo2Type == googScopeType);

    
    
    JSType googFooGetprop2Type = getpropFoo2.getJSType();
    assertTrue("goog.foo incorrectly annotated in goog.foo.bar selection",
        googFooGetprop2Type instanceof ObjectType);
    ObjectType googFooGetprop2ObjectType = (ObjectType) googFooGetprop2Type;
    assertFalse("foo property present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("foo"));
    assertTrue("bar property not present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("bar"));
    assertEquals("bar property on goog.foo type incorrectly inferred",
        NUMBER_TYPE, googFooGetprop2ObjectType.getPropertyType("bar"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomSimpleNamespace
  public void testAddingMethodsUsingPrototypeIdiomSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype.m1 = 5");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 1,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace1
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace1()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace2
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace2()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace
  public void testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 2,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDontAddMethodsIfNoConstructor
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionAssignement
  public void testFunctionAssignement() throws Exception {
    testTypes("" +
        "function MSG_CALENDAR_ACCESS_ERROR(ph0, ph1) {return ''}" +
        "" +
        "var MSG_CALENDAR_ADD_ERROR = MSG_CALENDAR_ACCESS_ERROR;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals("A", instanceType.toString());
    assertEquals(NATIVE_PROPERTIES_COUNT + 3,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m3", STRING_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypePropertyTypes
  public void testPrototypePropertyTypes() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {\n" +
        "   this.m1;\n" +
        "   this.m2 = {};\n" +
        "   this.m3;\n" +
        "}\n" +
        " A.prototype.m4;\n" +
        " A.prototype.m5 = 0;\n" +
        " A.prototype.m6;\n");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 6,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", STRING_TYPE);
    checkObjectType(instanceType, "m2",
        createUnionType(createUnionType(OBJECT_TYPE, NULL_TYPE), VOID_TYPE));
    checkObjectType(instanceType, "m3", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m4", STRING_TYPE);
    checkObjectType(instanceType, "m5", NUMBER_TYPE);
    checkObjectType(instanceType, "m6", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueTypeBuiltInPrototypePropertyType
  public void testValueTypeBuiltInPrototypePropertyType() throws Exception {
    Node node = parseAndTypeCheck("\"x\".charAt(0)");
    assertEquals(STRING_TYPE, node.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDeclareBuiltInConstructor
  public void testDeclareBuiltInConstructor() throws Exception {
    
    
    Node node = parseAndTypeCheck(
        " var String = function(opt_str) {};\n" +
        "(new String(\"x\")).charAt(0)");
    assertEquals(STRING_TYPE, node.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendBuiltInType1
  public void testExtendBuiltInType1() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n1 = parseAndTypeCheck(externs + "(new String(\"x\")).substr(0,1);");
    assertEquals(STRING_TYPE, n1.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendBuiltInType2
  public void testExtendBuiltInType2() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n2 = parseAndTypeCheck(externs + "\"x\".substr(0,1);");
    assertEquals(STRING_TYPE, n2.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendFunction1
  public void testExtendFunction1() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(new Function()).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendFunction2
  public void testExtendFunction2() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(function() {}).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck1
  public void testInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck2
  public void testInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo not defined on any superclass of Sub");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck3
  public void testInheritanceCheck3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck4
  public void testInheritanceCheck4() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck5
  public void testInheritanceCheck5() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Root; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck6
  public void testInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck7
  public void testInheritanceCheck7() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck8
  public void testInheritanceCheck8() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_1
  public void testInheritanceCheck9_1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 3; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_2
  public void testInheritanceCheck9_2() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_3
  public void testInheritanceCheck9_3() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck10_1
  public void testInheritanceCheck10_1() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() { return 4; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck10_2
  public void testInheritanceCheck10_2() throws Exception {
    testTypes(
        "function Root() {};" +
        "" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck10_3
  public void testInheritanceCheck10_3() throws Exception {
    testTypes(
        "function Root() {};" +
        "" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Root\n" +
        "original: function (this:Root): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck11
  public void testInterfaceInheritanceCheck11() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck12
  public void testInheritanceCheck12() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = \"some string\";");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck13
  public void testInheritanceCheck13() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck14
  public void testInheritanceCheck14() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "\n" +
        "goog.Super = function() {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck1
  public void testInterfaceInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on interface Super; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck2
  public void testInterfaceInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck3
  public void testInterfaceInheritanceCheck3() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() { return 1;};",
        "property foo already defined on interface Root; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck4
  public void testInterfaceInheritanceCheck4() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1;};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck5
  public void testInterfaceInheritanceCheck5() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck6
  public void testInterfaceInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Root\n" +
        "original: function (this:Root): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck7
  public void testInterfaceInheritanceCheck7() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck8
  public void testInterfaceInheritanceCheck8() throws Exception {
    testTypes(
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        new String[] {
          "Bad type annotation. Unknown type Super",
          "property foo not defined on any superclass of Sub"
        });
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfacePropertyNotImplemented
  public void testInterfacePropertyNotImplemented() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfacePropertyNotImplemented2
  public void testInterfacePropertyNotImplemented2() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubConstructorImplementingInterface
  public void testStubConstructorImplementingInterface() throws Exception {
    
    
    testTypes(
        
        " function Int() {}\n" +
        "Int.prototype.foo = function() {};" +
        " var Foo;\n",
        "", null, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectLiteral
  public void testObjectLiteral() throws Exception {
    Node n = parseAndTypeCheck("var a = {m1: 7, m2: 'hello'}");

    Node nameNode = n.getFirstChild().getFirstChild();
    Node objectNode = nameNode.getFirstChild();

    
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals(Token.OBJECTLIT, objectNode.getType());

    
    ObjectType objectType =
        (ObjectType) objectNode.getJSType();
    assertEquals(NUMBER_TYPE, objectType.getPropertyType("m1"));
    assertEquals(STRING_TYPE, objectType.getPropertyType("m2"));

    
    assertEquals(objectType, nameNode.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectLiteralDeclaration1
  public void testObjectLiteralDeclaration1() throws Exception {
    testTypes(
        "var x = {" +
        " abc: true," +
        " 'def': 0," +
        " 3: 'fgh'" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCallDateConstructorAsFunction
  public void testCallDateConstructorAsFunction() throws Exception {
    
    
    Node n = parseAndTypeCheck("Date()");
    assertEquals(STRING_TYPE, n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCallErrorConstructorAsFunction
  public void testCallErrorConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Error('x')");
    assertEquals(ERROR_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCallArrayConstructorAsFunction
  public void testCallArrayConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Array()");
    assertEquals(ARRAY_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyTypeOfUnionType
  public void testPropertyTypeOfUnionType() throws Exception {
    testTypes("var a = {};" +
        " a.N = function() {};\n" +
        "a.N.prototype.p = 1;\n" +
        " a.S = function() {};\n" +
        "a.S.prototype.p = 'a';\n" +
        "\n" +
        "var f = function(x) { return x.p; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface1
  public void testAnnotatedPropertyOnInterface1() throws Exception {
    
    
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface2
  public void testAnnotatedPropertyOnInterface2() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface3
  public void testAnnotatedPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        " T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface4
  public void testAnnotatedPropertyOnInterface4() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function T() {};\n" +
        " T.prototype.f = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnUnannotatedPropertyOnInterface5
  public void testWarnUnannotatedPropertyOnInterface5() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnUnannotatedPropertyOnInterface6
  public void testWarnUnannotatedPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnDataPropertyOnInterface3
  public void testWarnDataPropertyOnInterface3() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnDataPropertyOnInterface4
  public void testWarnDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testErrorMismatchingPropertyOnInterface4
  public void testErrorMismatchingPropertyOnInterface4() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x =\n" +
        "function() {};",
        "parameter foo does not appear in u.T.prototype.x's parameter list");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testErrorMismatchingPropertyOnInterface5
  public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testErrorMismatchingPropertyOnInterface6
  public void testErrorMismatchingPropertyOnInterface6() throws Exception {
    testClosureTypesMultipleWarnings(
        " function T() {};\n" +
        "T.prototype.x = 1",
        Lists.newArrayList(
            "assignment to property x of T.prototype\n" +
            "found   : number\n" +
            "required: function (this:T): number",
            "interface members can only be empty property declarations, " +
            "empty functions, or goog.abstractMethod"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceNonEmptyFunction
  public void testInterfaceNonEmptyFunction() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { return 'foo'; }",
        "interface member functions must have an empty body"
        );
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDoubleNestedInterface
  public void testDoubleNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.I3 = function() {};\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticDataPropertyOnNestedInterface
  public void testStaticDataPropertyOnNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.x = 1;\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() throws Exception {
    testTypes("var f = function(){}; new f",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypeLoop
  public void testPrototypeLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T",
            "Could not resolve type in @extends tag of T"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry1
  public void testResolutionViaRegistry1() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry3
  public void testResolutionViaRegistry3() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a = 0;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry4
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry5
  public void testResolutionViaRegistry5() throws Exception {
    Node n = parseAndTypeCheck(" u.T = function() {}; u.T");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof FunctionType);
    assertEquals("u.T",
        ((FunctionType) type).getInstanceType().getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGatherProperyWithoutAnnotation1
  public void testGatherProperyWithoutAnnotation1() throws Exception {
    Node n = parseAndTypeCheck(" var T = function() {};" +
        "var t; t.x; t;");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Lists.newArrayList(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGatherProperyWithoutAnnotation2
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
        Lists.newArrayList(OBJECT_TYPE),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionMasksVariableBug
  public void testFunctionMasksVariableBug() throws Exception {
    testTypes("var x = 4; var f = function x(b) { return b ? 1 : x(true); };",
        "function x masks variable (IE bug)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa1
  public void testDfa1() throws Exception {
    testTypes("var x = null;\n x = 1;\n  var y = x;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa2
  public void testDfa2() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\nvar x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa3
  public void testDfa3() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\n" +
        " var x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa4
  public void testDfa4() throws Exception {
    testTypes(" function f(d) {\n" +
        "if (!d) { return; }\n" +
        " var e = d;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa5
  public void testDfa5() throws Exception {
    testTypes(" function u() {return 'a';}\n" +
        " function f(x) {\n" +
        "while (!x) { x = u(); }\nreturn x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa6
  public void testDfa6() throws Exception {
    testTypes(" function u() {return {};}\n" +
        " function f(x) {\n" +
        "while (x) { x = u(); if (!x) { x = u(); } }\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa7
  public void testDfa7() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = null;\n" +
        " function f(t) {\n" +
        "if (!t.x) { return; }\n" +
        " var e = t.x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa8
  public void testDfa8() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = '';\n" +
        "function u() {}\n" +
        " function f(t) {\n" +
        "if (u()) { t.x = 1; } else { t.x = 2; } return t.x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa9
  public void testDfa9() throws Exception {
    testTypes("function f() {\nvar x;\nx = null;\n" +
        "if (x == null) { return 0; } else { return 1; } }",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa10
  public void testDfa10() throws Exception {
    testTypes(" function g(x) {}" +
        "function f(x) {\n" +
        "if (!x) { x = ''; }\n" +
        "if (g(x)) { return 0; } else { return 1; } }",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa11
  public void testDfa11() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { if (!opt_x) { " +
        "throw new Error('x cannot be empty'); } return opt_x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa12
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa13
  public void testDfa13() throws Exception {
    testTypes(
        "" +
        "function g(x, y, z) {}" +
        "function f() { " +
        "  var x = 'a'; g(x, x = 3, x);" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast1
  public void testTypeInferenceWithCast1() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast2
  public void testTypeInferenceWithCast2() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y; y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast3
  public void testTypeInferenceWithCast3() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x));}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast4
  public void testTypeInferenceWithCast4() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x)) && 1;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast5
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithClosure1
  public void testTypeInferenceWithClosure1() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x == null;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithClosure2
  public void testTypeInferenceWithClosure2() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x === 3;" +
        "}",
        "condition always evaluates to false\n" +
        "left : (null|string|undefined)\n" +
        "right: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardPropertyReference
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoForwardTypeDeclaration
  public void testNoForwardTypeDeclaration() throws Exception {
    testTypes(
        " function f(x) {}",
        "Bad type annotation. Unknown type MyType");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoForwardTypeDeclarationAndNoBraces
  public void testNoForwardTypeDeclarationAndNoBraces() throws Exception {
    testTypes(" function f() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardTypeDeclaration1
  public void testForwardTypeDeclaration1() throws Exception {
    testClosureTypes(
        
        "goog.addDependency();" +
        "goog.addDependency('y', [goog]);" +

        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return 3; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardTypeDeclaration2
  public void testForwardTypeDeclaration2() throws Exception {
    String f = "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }";
    testClosureTypes(f, null);
    testClosureTypes(f + "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardTypeDeclaration3
  public void testForwardTypeDeclaration3() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateTypeDef
  public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar = function() {};" +
        " goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (new:goog.Bar): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeDef1
  public void testTypeDef1() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeDef2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeDef3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testCircularTypeDef
  public void testCircularTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent1
  public void testGetTypedPercent1() throws Exception {
    String js = "var id = function(x) { return x; }\n" +
                "var id2 = function(x) { return id(x); }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent2
  public void testGetTypedPercent2() throws Exception {
    String js = "var x = {}; x.y = 1;";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent3
  public void testGetTypedPercent3() throws Exception {
    String js = "var f = function(x) { x.a = x.b; }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent4
  public void testGetTypedPercent4() throws Exception {
    String js = "var n = {};\n  n.T = function() {};\n" +
        " var x = new n.T();";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypePropertyReference
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolvingNamedTypes
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty1
  public void testMissingProperty1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty2
  public void testMissingProperty2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty3
  public void testMissingProperty3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).a = 3;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty4
  public void testMissingProperty4() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).b = 3;",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty5
  public void testMissingProperty5() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " function Bar() { this.a = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty6
  public void testMissingProperty6() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " " +
        "function Bar() { this.a = 3; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty7
  public void testMissingProperty7() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return obj.impossible; }",
        "Property impossible never defined on Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty8
  public void testMissingProperty8() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return typeof obj.impossible; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty9
  public void testMissingProperty9() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { if (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty10
  public void testMissingProperty10() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { while (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty11
  public void testMissingProperty11() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { for (;obj.impossible;) { return true; } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty12
  public void testMissingProperty12() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { do { } while (obj.impossible); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty13
  public void testMissingProperty13() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isDef(obj.impossible); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty14
  public void testMissingProperty14() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isNull(obj.impossible); }",
        "Property isNull never defined on goog");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty15
  public void testMissingProperty15() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo) { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty16
  public void testMissingProperty16() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo(); if (x.foo) {} }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty17
  public void testMissingProperty17() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (typeof x.foo == 'function') { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty18
  public void testMissingProperty18() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo instanceof Function) { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty19
  public void testMissingProperty19() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.bar) { if (x.foo) {} } else { x.foo(); } }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty20
  public void testMissingProperty20() throws Exception {
    
    
    
    
    
    
    
    
    testTypes(
        "" +
        "function f(x) { if (x.foo) { } else { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty21
  public void testMissingProperty21() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo && x.foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty22
  public void testMissingProperty22() throws Exception {
    testTypes(
        "" +
        "function f(x) { return x.foo ? x.foo() : true; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty23
  public void testMissingProperty23() throws Exception {
    testTypes(
        "function f(x) { x.impossible(); }",
        "Property impossible never defined on x");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty24
  public void testMissingProperty24() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty25
  public void testMissingProperty25() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "Foo.prototype.bar = function() {};" +
        " var FooAlias = Foo;" +
        "(new FooAlias()).bar();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty26
  public void testMissingProperty26() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        "FooAlias.prototype.bar = function() {};" +
        "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty27
  public void testMissingProperty27() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) {" +
        "  for (var parent = x; parent; parent = parent.getParent()) {}" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty28
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty29
  public void testMissingProperty29() throws Exception {
    
    testTypes(
        
        " var Foo;" +
        "Foo.prototype.opera;" +
        "Foo.prototype.opera.postError;",
        "",
        null,
        false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFlowScopeBug1
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testFlowScopeBug2
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

    
    assertEquals(registry.createOptionalType(
            registry.createNullableType(registry.getType("Foo"))),
        n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddSingletonGetter
  public void testAddSingletonGetter() {
    Node n = parseAndTypeCheck(
        " function Foo() {};\n" +
        "goog.addSingletonGetter(Foo);");
    ObjectType o = (ObjectType) n.getFirstChild().getJSType();
    assertEquals("function (): Foo",
        o.getPropertyType("getInstance").toString());
    assertEquals("Foo", o.getPropertyType("instance_").toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckStandaloneAST
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

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext5
  public void testMakeLocalNamesUniqueWithContext5() {
    
    this.useDefaultRenamer = true;

    testWithInversion(
        "function f(){var f; f = 1}",
        "function f(){var f$$1; f$$1 = 1}");
    testWithInversion(
        "function f(f){f = 1}",
        "function f(f$$1){f$$1 = 1}");
    testWithInversion(
        "function f(f){var f; f = 1}",
        "function f(f$$1){var f$$1; f$$1 = 1}");

    test(
        "var fn = function f(){var f; f = 1}",
        "var fn = function f(){var f$$1; f$$1 = 1}");
    test(
        "var fn = function f(f){f = 1}",
        "var fn = function f(f$$1){f$$1 = 1}");
    test(
        "var fn = function f(f){var f; f = 1}",
        "var fn = function f(f$$1){var f$$1; f$$1 = 1}");
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
    test("function f() {var CONST = 3; var ACONST$$1 = 2;}",
         "function f() {var CONST = 3; var ACONST = 2;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion2
  public void testOnlyInversion2() {
    invert = true;
    test("function f() {try { } catch(e) {e;}; try { } catch(e$$0) {e$$0;}}",
        "function f() {try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}}");
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
    test("(function () {var CONST = 3; var ACONST$$1 = 2;})",
         "(function () {var CONST$$unique_0 = 3; var ACONST$$unique_1 = 2;})");
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

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testStraightLine
  public void testStraightLine() {
    assertMatch("D:var x=1; U: x");
    assertMatch("var x; D:x=1; U: x");
    assertNotMatch("D:var x=1; x = 2; U: x");
    assertMatch("var x=1; D:x=2; U: x");
    assertNotMatch("U:x; D:var x = 1");
    assertMatch("D: var x = 1; var y = 2; y; U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testIf
  public void testIf() {
    assertMatch("var x; if(a){ D:x=1 }else { x=2 }; U:x");
    assertMatch("var x; if(a){ x=1 }else { D:x=2 }; U:x");
    assertMatch("D:var x=1; if(a){ U1: x }else { U2: x };");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testLoops
  public void testLoops() {
    assertMatch("var x=0; while(a){ D:x=1 }; U:x");
    assertMatch("var x=0; for(;;) { D:x=1 }; U:x");

    assertMatch("D:var x=1; while(a) { U:x }");
    assertMatch("D:var x=1; for(;;)  { U:x }");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testConditional
  public void testConditional() {
    assertMatch("var x=0; var y; D:(x=1)&&y; U:x");
    assertMatch("var x=0; var y; D:y&&(x=1); U:x");
    assertMatch("var x=0; var y=0; D:(x=1)&&(y=0); U:x");
    assertMatch("var x=0; var y=0; D:(y=0)&&(x=1); U:x");
    assertNotMatch("D: var x=0; var y=0; (x=1)&&(y=0); U:x");
    assertMatch("D: var x=0; var y=0; (y=1)&&((y=2)||(x=1)); U:x");
    assertMatch("D: var x=0; var y=0; (y=0)&&(x=1); U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testUseAndDefInSameInstruction
  public void testUseAndDefInSameInstruction() {
    assertNotMatch("D:var x=0; U:x=1,x");
    assertMatch("D:var x=0; U:x,x=1");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testAssignmentInExpressions
  public void testAssignmentInExpressions() {
    assertMatch("var x=0; D:foo(bar(x=1)); U:x");
    assertMatch("var x=0; D:foo(bar + (x = 1)); U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testHook
  public void testHook() {
    assertMatch("var x=0; D:foo() ? x=1 : bar(); U:x");
    assertMatch("var x=0; D:foo() ? x=1 : x=2; U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testAssignmentOps
  public void testAssignmentOps() {
    assertNotMatch("D: var x = 0; U: x = 100");
    assertMatch("D: var x = 0; U: x += 100");
    assertMatch("D: var x = 0; U: x -= 100");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testInc
  public void testInc() {
    assertMatch("D: var x = 0; U:x++");
    assertMatch("var x = 0; D:x++; U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testForIn
  public void testForIn() {
    
    
    assertNotMatch("D: var x = [], foo; U: for (x in foo) { }");
    assertNotMatch("D: var x = [], foo; for (x in foo) { U:x }");
    assertMatch("var x = [], foo; D: for (x in foo) { U:x }");
  }

// com.google.javascript.jscomp.MemoizedScopeCreatorTest::testMemoization
  public void testMemoization() throws Exception {
    Node trueNode = new Node(Token.TRUE);
    Node falseNode = new Node(Token.FALSE);
    
    
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    ScopeCreator creator = new MemoizedScopeCreator(
        new SyntacticScopeCreator(compiler));
    Scope scopeA = creator.createScope(trueNode, null);
    assertSame(scopeA, creator.createScope(trueNode, null));
    assertNotSame(scopeA, creator.createScope(falseNode, null));
  }

// com.google.javascript.jscomp.MemoizedScopeCreatorTest::testPreconditionCheck
  public void testPreconditionCheck() throws Exception {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node trueNode = new Node(Token.TRUE);
    ScopeCreator creator = new MemoizedScopeCreator(
        new SyntacticScopeCreator(compiler));
    Scope scopeA = creator.createScope(trueNode, null);

    boolean handled = false;
    try {
      creator.createScope(trueNode, scopeA);
    } catch (IllegalStateException e) {
      handled = true;
    }
    assertTrue(handled);
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
    fold("function f(){if(a()){b();if(c())return;}}",
         "function f(){if(a()){b();if(c());}}");
    fold("function f(){if(x)return; x=3; return; }",
         "function f(){if(x); else x=3}");
    fold("function f(){if(true){a();return;}else;b();}",
         "function f(){if(true){a();}else{b();}}");
    fold("function f(){if(false){a();return;}else;b();return;}",
         "function f(){if(false){a();}else{b();}}");
    fold("function f(){if(a()){b();return;}else;c();}",
         "function f(){if(a()){b();}else{c();}}");
    fold("function f(){if(a()){b()}else{c();return;}}",
         "function f(){if(a()){b()}else{c();}}");
    fold("function f(){if(a()){b();return;}else;}",
         "function f(){if(a()){b();}else;}");
    fold("function f(){if(a()){return;}else{return;} return;}",
         "function f(){if(a()){}else{}}");
    fold("function f(){if(a()){return;}else{return;} b();}",
         "function f(){if(a()){}else{return;b()}}");

    fold("function f(){while(a())return;}",
         "function f(){while(a())return}");
    foldSame("function f(){for(x in a())return}");

    fold("function f(){while(a())break;}",
         "function f(){while(a())break}");
    foldSame("function f(){for(x in a())break}");

    fold("function f(){try{return;}catch(e){return;}finally{return}}",
         "function f(){try{}catch(e){}finally{}}");
    fold("function f(){try{return;}catch(e){return;}}",
         "function f(){try{}catch(e){}}");
    fold("function f(){try{return;}finally{return;}}",
         "function f(){try{}finally{}}");
    fold("function f(){try{if(a()){return;}else{return;} return;}catch(e){}}",
         "function f(){try{if(a()){}else{}}catch(e){}}");

    fold("function f(){g:return}",
         "function f(){}");
    fold("function f(){g:if(a()){return;}else{return;} return;}",
         "function f(){g:if(a()){}else{}}");
    fold("function f(){try{g:if(a()){} return;}finally{return}}",
         "function f(){try{g:if(a()){}}finally{}}");
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

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testStraightLine
  public void testStraightLine() {
    assertMatch("D:var x=1; U: x");
    assertMatch("var x; D:x=1; U: x");
    assertNotMatch("D:var x=1; x = 2; U: x");
    assertMatch("var x=1; D:x=2; U: x");
    assertNotMatch("U:x; D:var x = 1");
    assertNotMatch("D:var x; U:x; x=1");
    assertNotMatch("D:var x; U:x; x=1; x");
    assertMatch("D: var x = 1; var y = 2; y; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testIf
  public void testIf() {
    assertNotMatch("var x; if(a){ D:x=1 } else { x=2 }; U:x");
    assertNotMatch("var x; if(a){ x=1 } else { D:x=2 }; U:x");
    assertMatch("D:var x=1; if(a){ U:x } else { x };");
    assertMatch("D:var x=1; if(a){ x } else { U:x };");
    assertNotMatch("var x; if(a) { D: x = 1 }; U:x;");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testLoops
  public void testLoops() {
    assertNotMatch("var x=0; while(a){ D:x=1 }; U:x");
    assertNotMatch("var x=0; for(;;) { D:x=1 }; U:x");
    assertMatch("D:var x=1; while(a) { U:x }");
    assertMatch("D:var x=1; for(;;)  { U:x }");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testConditional
  public void testConditional() {
    assertMatch("var x=0,y; D:(x=1)&&y; U:x");
    assertNotMatch("var x=0,y; D:y&&(x=1); U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testUseAndDefInSameInstruction
  public void testUseAndDefInSameInstruction() {
    assertMatch("D:var x=0; U:x=1,x");
    assertMatch("D:var x=0; U:x,x=1");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testAssignmentInExpressions
  public void testAssignmentInExpressions() {
    assertMatch("var x=0; D:foo(bar(x=1)); U:x");
    assertMatch("var x=0; D:foo(bar + (x = 1)); U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testHook
  public void testHook() {
    assertNotMatch("var x=0; D:foo() ? x=1 : bar(); U:x");
    assertNotMatch("var x=0; D:foo() ? x=1 : x=2; U:x");
  }
