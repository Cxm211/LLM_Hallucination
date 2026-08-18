// buggy code
  private JSType getNativeType(JSTypeNative nativeType) {
    return typeRegistry.getNativeType(nativeType);
  }

    public void visit(NodeTraversal t, Node n, Node parent) {
      inputId = t.getInputId();
      attachLiteralTypes(t, n);

      switch (n.getType()) {
        case Token.CALL:
          checkForClassDefiningCalls(t, n, parent);
          checkForCallingConventionDefiningCalls(n, delegateCallingConventions);
          break;

        case Token.FUNCTION:
          if (t.getInput() == null || !t.getInput().isExtern()) {
            nonExternFunctions.add(n);
          }

          // Hoisted functions are handled during pre-traversal.
          if (!NodeUtil.isHoistedFunctionDeclaration(n)) {
            defineFunctionLiteral(n, parent);
          }
          break;

        case Token.ASSIGN:
          // Handle initialization of properties.
          Node firstChild = n.getFirstChild();
          if (firstChild.isGetProp() &&
              firstChild.isQualifiedName()) {
            maybeDeclareQualifiedName(t, n.getJSDocInfo(),
                firstChild, n, firstChild.getNext());
          }
          break;

        case Token.CATCH:
          defineCatch(n, parent);
          break;

        case Token.VAR:
          defineVar(n, parent);
          break;

        case Token.GETPROP:
          // Handle stubbed properties.
          if (parent.isExprResult() &&
              n.isQualifiedName()) {
            maybeDeclareQualifiedName(t, n.getJSDocInfo(), n, parent, null);
          }
          break;
      }

      // Analyze any @lends object literals in this statement.
    }

    private void attachLiteralTypes(NodeTraversal t, Node n) {
      switch (n.getType()) {
        case Token.NULL:
          n.setJSType(getNativeType(NULL_TYPE));
          break;

        case Token.VOID:
          n.setJSType(getNativeType(VOID_TYPE));
          break;

        case Token.STRING:
          // Defer keys to the Token.OBJECTLIT case
          if (!NodeUtil.isObjectLitKey(n, n.getParent())) {
            n.setJSType(getNativeType(STRING_TYPE));
          }
          break;

        case Token.NUMBER:
          n.setJSType(getNativeType(NUMBER_TYPE));
          break;

        case Token.TRUE:
        case Token.FALSE:
          n.setJSType(getNativeType(BOOLEAN_TYPE));
          break;

        case Token.REGEXP:
          n.setJSType(getNativeType(REGEXP_TYPE));
          break;

        case Token.OBJECTLIT:
            defineObjectLiteral(n);
          break;

          // NOTE(nicksantos): If we ever support Array tuples,
          // we will need to put ARRAYLIT here as well.
      }
    }

// relevant test
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

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteUsedResult1
  public void testNoRewriteUsedResult1() throws Exception {
    String source = newlineJoin(
        "function a(){return 1}",
        "var x = a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteUsedResult2
  public void testNoRewriteUsedResult2() throws Exception {
    String source = newlineJoin(
        "var a = function(){return 1}",
        "a(); var b = a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult1
  public void testRewriteUnusedResult1() throws Exception {
    String source = newlineJoin(
        "function a(){return 1}",
        "a()");
    String expected = newlineJoin(
        "function a(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult2
  public void testRewriteUnusedResult2() throws Exception {
    String source = newlineJoin(
        "var a; a = function(){return 1}",
        "a()");
    String expected = newlineJoin(
        "var a; a = function(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult3
  public void testRewriteUnusedResult3() throws Exception {
    String source = newlineJoin(
        "var a = function(){return 1}",
        "a()");
    String expected = newlineJoin(
        "var a = function(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4a
  public void testRewriteUnusedResult4a() throws Exception {
    String source = newlineJoin(
        "var a = function(){return a()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4b
  public void testRewriteUnusedResult4b() throws Exception {
    String source = newlineJoin(
        "var a = function b(){return b()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4c
  public void testRewriteUnusedResult4c() throws Exception {
    String source = newlineJoin(
        "function a(){return a()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult5
  public void testRewriteUnusedResult5() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return};",
        "var o = new a;",
        "o.foo()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult6
  public void testRewriteUnusedResult6() throws Exception {
    String source = newlineJoin(
        "function a(){return (g = 1)}",
        "a()");
    String expected = newlineJoin(
        "function a(){g = 1;return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult7a
  public void testRewriteUnusedResult7a() throws Exception {
    String source = newlineJoin(
        "function a() { return 1 }",
        "function b() { return a() }",
        "function c() { return b() }",
        "c();");

    String expected = newlineJoin(
        "function a() { return 1 }",
        "function b() { return a() }",
        "function c() { b(); return }",
        "c();");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult7b
  public void testRewriteUnusedResult7b() throws Exception {
    String source = newlineJoin(
        "c();",
        "function c() { return b() }",
        "function b() { return a() }",
        "function a() { return 1 }");

    
    String expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { return a() }",
        "function a() { return 1 }");
    test(source, expected);

    
    source = expected;
    expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { a(); return }",
        "function a() { return 1 }");
    test(source, expected);

    
    source = expected;
    expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { a(); return }",
        "function a() { return }");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult8
  public void testRewriteUnusedResult8() throws Exception {
    String source = newlineJoin(
        "function a() { return c() }",
        "function b() { return a() }",
        "function c() { return b() }",
        "c();");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit1
  public void testNoRewriteObjLit1() throws Exception {
    String source = newlineJoin(
        "var a = {b:function(){return 1;}}",
        "for(c in a) (a[c])();",
        "a.b()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit2
  public void testNoRewriteObjLit2() throws Exception {
    String source = newlineJoin(
        "var a = {b:function fn(){return 1;}}",
        "for(c in a) (a[c])();",
        "a.b()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteArrLit
  public void testNoRewriteArrLit() throws Exception {
    String source = newlineJoin(
        "var a = [function(){return 1;}]",
        "(a[0])();");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod1
  public void testPrototypeMethod1() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "x.a()");
    String result = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return}",
        "var x = new c;",
        "x.a()");
    test(source, result);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod2
  public void testPrototypeMethod2() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "goog.reflect.object({a: 'v'})",
        "var x = new c;",
        "x.a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod3
  public void testPrototypeMethod3() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "for(var key in goog.reflect.object({a: 'v'})){ x[key](); }",
        "x.a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod4
  public void testPrototypeMethod4() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "for(var key in goog.reflect.object({a: 'v'})){ x[key](); }");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testCallOrApply
  public void testCallOrApply() throws Exception {
    
    testSame("function a() {return 1}; a.call(new foo);");

    testSame("function a() {return 1}; a.apply(new foo);");
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUseSiteRemoval
  public void testRewriteUseSiteRemoval() throws Exception {
    String source = newlineJoin(
        "function a() { return {\"_id\" : 1} }",
        "a();");
    String expected = newlineJoin(
        "function a() { return }",
        "a();");
    test(source, expected);
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testNoFunction
  public void testNoFunction() {
    replace("\"foo\"");
    replace("var foo");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testOneFunction
  public void testOneFunction() {
    replace("\"foo\";function foo(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testTwoFunctions
  public void testTwoFunctions() {
    replace("\"foo\";function f1(){\"foo\"}function f2(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testInnerFunctions
  public void testInnerFunctions() {
    replace("\"foo\";function f1(){\"foo\";function f2(){\"foo\"}}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testManyFunctions
  public void testManyFunctions() {
    StringBuilder sb = new StringBuilder("\"foo\";");
    for (int i = 0; i < 20; i++) {
      sb.append("function f");
      sb.append(i);
      sb.append("(){\"foo\"}");
    }
    replace(sb.toString());
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison1
  public void testUndefinedComparison1() {
    fold("undefined == undefined", "true");
    fold("undefined == null", "true");
    fold("undefined == void 0", "true");

    fold("undefined == 0", "false");
    fold("undefined == 1", "false");
    fold("undefined == 'hi'", "false");
    fold("undefined == true", "false");
    fold("undefined == false", "false");

    fold("undefined === undefined", "true");
    fold("undefined === null", "false");
    fold("undefined === void 0", "true");

    foldSame("undefined == this");
    foldSame("undefined == x");

    fold("undefined != undefined", "false");
    fold("undefined != null", "false");
    fold("undefined != void 0", "false");

    fold("undefined != 0", "true");
    fold("undefined != 1", "true");
    fold("undefined != 'hi'", "true");
    fold("undefined != true", "true");
    fold("undefined != false", "true");

    fold("undefined !== undefined", "false");
    fold("undefined !== void 0", "false");
    fold("undefined !== null", "true");

    foldSame("undefined != this");
    foldSame("undefined != x");

    fold("undefined < undefined", "false");
    fold("undefined > undefined", "false");
    fold("undefined >= undefined", "false");
    fold("undefined <= undefined", "false");

    fold("0 < undefined", "false");
    fold("true > undefined", "false");
    fold("'hi' >= undefined", "false");
    fold("null <= undefined", "false");

    fold("undefined < 0", "false");
    fold("undefined > true", "false");
    fold("undefined >= 'hi'", "false");
    fold("undefined <= null", "false");

    fold("null == undefined", "true");
    fold("0 == undefined", "false");
    fold("1 == undefined", "false");
    fold("'hi' == undefined", "false");
    fold("true == undefined", "false");
    fold("false == undefined", "false");
    fold("null === undefined", "false");
    fold("void 0 === undefined", "true");

    fold("undefined == NaN", "false");
    fold("NaN == undefined", "false");
    fold("undefined == Infinity", "false");
    fold("Infinity == undefined", "false");
    fold("undefined == -Infinity", "false");
    fold("-Infinity == undefined", "false");
    fold("({}) == undefined", "false");
    fold("undefined == ({})", "false");
    fold("([]) == undefined", "false");
    fold("undefined == ([])", "false");
    fold("(/a/g) == undefined", "false");
    fold("undefined == (/a/g)", "false");
    fold("(function(){}) == undefined", "false");
    fold("undefined == (function(){})", "false");

    fold("undefined != NaN", "true");
    fold("NaN != undefined", "true");
    fold("undefined != Infinity", "true");
    fold("Infinity != undefined", "true");
    fold("undefined != -Infinity", "true");
    fold("-Infinity != undefined", "true");
    fold("({}) != undefined", "true");
    fold("undefined != ({})", "true");
    fold("([]) != undefined", "true");
    fold("undefined != ([])", "true");
    fold("(/a/g) != undefined", "true");
    fold("undefined != (/a/g)", "true");
    fold("(function(){}) != undefined", "true");
    fold("undefined != (function(){})", "true");

    foldSame("this == undefined");
    foldSame("x == undefined");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison2
  public void testUndefinedComparison2() {
    fold("\"123\" !== void 0", "true");
    fold("\"123\" === void 0", "false");

    fold("void 0 !== \"123\"", "true");
    fold("void 0 === \"123\"", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison3
  public void testUndefinedComparison3() {
    fold("\"123\" !== undefined", "true");
    fold("\"123\" === undefined", "false");

    fold("undefined !== \"123\"", "true");
    fold("undefined === \"123\"", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison4
  public void testUndefinedComparison4() {
    fold("1 !== void 0", "true");
    fold("1 === void 0", "false");

    fold("null !== void 0", "true");
    fold("null === void 0", "false");

    fold("undefined !== void 0", "false");
    fold("undefined === void 0", "true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testNullComparison1
  public void testNullComparison1() {
    fold("null == undefined", "true");
    fold("null == null", "true");
    fold("null == void 0", "true");

    fold("null == 0", "false");
    fold("null == 1", "false");
    fold("null == 'hi'", "false");
    fold("null == true", "false");
    fold("null == false", "false");

    fold("null === undefined", "false");
    fold("null === null", "true");
    fold("null === void 0", "false");

    foldSame("null == this");
    foldSame("null == x");

    fold("null != undefined", "false");
    fold("null != null", "false");
    fold("null != void 0", "false");

    fold("null != 0", "true");
    fold("null != 1", "true");
    fold("null != 'hi'", "true");
    fold("null != true", "true");
    fold("null != false", "true");

    fold("null !== undefined", "true");
    fold("null !== void 0", "true");
    fold("null !== null", "false");

    foldSame("null != this");
    foldSame("null != x");

    fold("null < null", "false");
    fold("null > null", "false");
    fold("null >= null", "true");
    fold("null <= null", "true");

    foldSame("0 < null"); 
    fold("true > null", "true");
    foldSame("'hi' >= null"); 
    fold("null <= null", "true");

    foldSame("null < 0");  
    fold("null > true", "false");
    foldSame("null >= 'hi'"); 
    fold("null <= null", "true");

    fold("null == null", "true");
    fold("0 == null", "false");
    fold("1 == null", "false");
    fold("'hi' == null", "false");
    fold("true == null", "false");
    fold("false == null", "false");
    fold("null === null", "true");
    fold("void 0 === null", "false");

    fold("null == NaN", "false");
    fold("NaN == null", "false");
    fold("null == Infinity", "false");
    fold("Infinity == null", "false");
    fold("null == -Infinity", "false");
    fold("-Infinity == null", "false");
    fold("({}) == null", "false");
    fold("null == ({})", "false");
    fold("([]) == null", "false");
    fold("null == ([])", "false");
    fold("(/a/g) == null", "false");
    fold("null == (/a/g)", "false");
    fold("(function(){}) == null", "false");
    fold("null == (function(){})", "false");

    fold("null != NaN", "true");
    fold("NaN != null", "true");
    fold("null != Infinity", "true");
    fold("Infinity != null", "true");
    fold("null != -Infinity", "true");
    fold("-Infinity != null", "true");
    fold("({}) != null", "true");
    fold("null != ({})", "true");
    fold("([]) != null", "true");
    fold("null != ([])", "true");
    fold("(/a/g) != null", "true");
    fold("null != (/a/g)", "true");
    fold("(function(){}) != null", "true");
    fold("null != (function(){})", "true");

    foldSame("({a:f()}) == null");
    foldSame("null == ({a:f()})");
    foldSame("([f()]) == null");
    foldSame("null == ([f()])");

    foldSame("this == null");
    foldSame("x == null");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUnaryOps
  public void testUnaryOps() {
    
    foldSame("!foo()");
    foldSame("~foo()");
    foldSame("-foo()");

    
    fold("a=!true", "a=false");
    fold("a=!10", "a=false");
    fold("a=!false", "a=true");
    fold("a=!foo()", "a=!foo()");
    fold("a=-0", "a=0");
    fold("a=-Infinity", "a=-Infinity");
    fold("a=-NaN", "a=NaN");
    fold("a=-foo()", "a=-foo()");
    fold("a=~~0", "a=0");
    fold("a=~~10", "a=10");
    fold("a=~-7", "a=6");

    fold("a=+true", "a=1");
    fold("a=+10", "a=10");
    fold("a=+false", "a=0");
    foldSame("a=+foo()");
    foldSame("a=+f");
    fold("a=+(f?true:false)", "a=+(f?1:0)"); 
    fold("a=+0", "a=0");
    fold("a=+Infinity", "a=Infinity");
    fold("a=+NaN", "a=NaN");
    fold("a=+-7", "a=-7");
    fold("a=+.5", "a=.5");

    fold("a=~0x100000000", "a=~0x100000000",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~-0x100000000", "a=~-0x100000000",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~.5", "~.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUnaryOpsStringCompare
  public void testUnaryOpsStringCompare() {
    
    assertResultString("a=-1", "a=-1");
    assertResultString("a=~0", "a=-1");
    assertResultString("a=~1", "a=-2");
    assertResultString("a=~101", "a=-102");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLogicalOp
  public void testFoldLogicalOp() {
    fold("x = true && x", "x = x");
    foldSame("x = [foo()] && x");

    fold("x = false && x", "x = false");
    fold("x = true || x", "x = true");
    fold("x = false || x", "x = x");
    fold("x = 0 && x", "x = 0");
    fold("x = 3 || x", "x = 3");
    fold("x = false || 0", "x = 0");

    
    fold("a = x && true", "a=x&&true");
    fold("a = x && false", "a=x&&false");
    fold("a = x || 3", "a=x||3");
    fold("a = x || false", "a=x||false");
    fold("a = b ? c : x || false", "a=b?c:x||false");
    fold("a = b ? x || false : c", "a=b?x||false:c");
    fold("a = b ? c : x && true", "a=b?c:x&&true");
    fold("a = b ? x && true : c", "a=b?x&&true:c");

    
    foldSame("a = x || false ? b : c");
    foldSame("a = x && true ? b : c");

    fold("x = foo() || true || bar()", "x = foo()||true");
    fold("x = foo() || false || bar()", "x = foo()||bar()");
    fold("x = foo() || true && bar()", "x = foo()||bar()");
    fold("x = foo() || false && bar()", "x = foo()||false");
    fold("x = foo() && false && bar()", "x = foo()&&false");
    fold("x = foo() && true && bar()", "x = foo()&&bar()");
    fold("x = foo() && false || bar()", "x = foo()&&false||bar()");

    fold("1 && b()", "b()");
    fold("a() && (1 && b())", "a() && b()");
    
    
    fold("(a() && 1) && b()", "(a() && 1) && b()");

    
    
    
    foldSame("x = foo() && true || bar()");
    foldSame("foo() && true || bar()");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitwiseOp
  public void testFoldBitwiseOp() {
    fold("x = 1 & 1", "x = 1");
    fold("x = 1 & 2", "x = 0");
    fold("x = 3 & 1", "x = 1");
    fold("x = 3 & 3", "x = 3");

    fold("x = 1 | 1", "x = 1");
    fold("x = 1 | 2", "x = 3");
    fold("x = 3 | 1", "x = 3");
    fold("x = 3 | 3", "x = 3");

    fold("x = 1 ^ 1", "x = 0");
    fold("x = 1 ^ 2", "x = 3");
    fold("x = 3 ^ 1", "x = 2");
    fold("x = 3 ^ 3", "x = 0");

    fold("x = -1 & 0", "x = 0");
    fold("x = 0 & -1", "x = 0");
    fold("x = 1 & 4", "x = 0");
    fold("x = 2 & 3", "x = 2");

    
    
    fold("x = 1 & 1.1", "x = 1");
    fold("x = 1.1 & 1", "x = 1");
    fold("x = 1 & 3000000000", "x = 0");
    fold("x = 3000000000 & 1", "x = 0");

    
    fold("x = 1 | 4", "x = 5");
    fold("x = 1 | 3", "x = 3");
    fold("x = 1 | 1.1", "x = 1");
    foldSame("x = 1 | 3E9");
    fold("x = 1 | 3000000001", "x = -1294967295");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitwiseOp2
  public void testFoldBitwiseOp2() {
    fold("x = y & 1 & 1", "x = y & 1");
    fold("x = y & 1 & 2", "x = y & 0");
    fold("x = y & 3 & 1", "x = y & 1");
    fold("x = 3 & y & 1", "x = y & 1");
    fold("x = y & 3 & 3", "x = y & 3");
    fold("x = 3 & y & 3", "x = y & 3");

    fold("x = y | 1 | 1", "x = y | 1");
    fold("x = y | 1 | 2", "x = y | 3");
    fold("x = y | 3 | 1", "x = y | 3");
    fold("x = 3 | y | 1", "x = y | 3");
    fold("x = y | 3 | 3", "x = y | 3");
    fold("x = 3 | y | 3", "x = y | 3");

    fold("x = y ^ 1 ^ 1", "x = y ^ 0");
    fold("x = y ^ 1 ^ 2", "x = y ^ 3");
    fold("x = y ^ 3 ^ 1", "x = y ^ 2");
    fold("x = 3 ^ y ^ 1", "x = y ^ 2");
    fold("x = y ^ 3 ^ 3", "x = y ^ 0");
    fold("x = 3 ^ y ^ 3", "x = y ^ 0");

    fold("x = Infinity | NaN", "x=0");
    fold("x = 12 | NaN", "x=12");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldingMixTypesLate
  public void testFoldingMixTypesLate() {
    late = true;
    fold("x = x + '2'", "x+='2'");
    fold("x = +x + +'2'", "x = +x + 2");
    fold("x = x - '2'", "x-=2");
    fold("x = x ^ '2'", "x^=2");
    fold("x = '2' ^ x", "x^=2");
    fold("x = '2' & x", "x&=2");
    fold("x = '2' | x", "x|=2");

    fold("x = '2' | y", "x=2|y");
    fold("x = y | '2'", "x=y|2");
    fold("x = y | (a && '2')", "x=y|(a&&2)");
    fold("x = y | (a,'2')", "x=y|(a,2)");
    fold("x = y | (a?'1':'2')", "x=y|(a?1:2)");
    fold("x = y | ('x'?'1':'2')", "x=y|('x'?1:2)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldingMixTypesEarly
  public void testFoldingMixTypesEarly() {
    late = false;
    foldSame("x = x + '2'");
    fold("x = +x + +'2'", "x = +x + 2");
    fold("x = x - '2'", "x = x - 2");
    fold("x = x ^ '2'", "x = x ^ 2");
    fold("x = '2' ^ x", "x = 2 ^ x");
    fold("x = '2' & x", "x = 2 & x");
    fold("x = '2' | x", "x = 2 | x");

    fold("x = '2' | y", "x=2|y");
    fold("x = y | '2'", "x=y|2");
    fold("x = y | (a && '2')", "x=y|(a&&2)");
    fold("x = y | (a,'2')", "x=y|(a,2)");
    fold("x = y | (a?'1':'2')", "x=y|(a?1:2)");
    fold("x = y | ('x'?'1':'2')", "x=y|('x'?1:2)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldingAdd
  public void testFoldingAdd() {
    fold("x = null + true", "x=1");
    foldSame("x = a + true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitwiseOpStringCompare
  public void testFoldBitwiseOpStringCompare() {
    assertResultString("x = -1 | 0", "x=-1");
    
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitShifts
  public void testFoldBitShifts() {
    fold("x = 1 << 0", "x = 1");
    fold("x = -1 << 0", "x = -1");
    fold("x = 1 << 1", "x = 2");
    fold("x = 3 << 1", "x = 6");
    fold("x = 1 << 8", "x = 256");

    fold("x = 1 >> 0", "x = 1");
    fold("x = -1 >> 0", "x = -1");
    fold("x = 1 >> 1", "x = 0");
    fold("x = 2 >> 1", "x = 1");
    fold("x = 5 >> 1", "x = 2");
    fold("x = 127 >> 3", "x = 15");
    fold("x = 3 >> 1", "x = 1");
    fold("x = 3 >> 2", "x = 0");
    fold("x = 10 >> 1", "x = 5");
    fold("x = 10 >> 2", "x = 2");
    fold("x = 10 >> 5", "x = 0");

    fold("x = 10 >>> 1", "x = 5");
    fold("x = 10 >>> 2", "x = 2");
    fold("x = 10 >>> 5", "x = 0");
    fold("x = -1 >>> 1", "x = 2147483647"); 
    fold("x = -1 >>> 0", "x = 4294967295"); 
    fold("x = -2 >>> 0", "x = 4294967294"); 

    fold("3000000000 << 1", "3000000000<<1",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 << 32", "1<<32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1 << -1", "1<<32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("3000000000 >> 1", "3000000000>>1",
        PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 >> 32", "1>>32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1.5 << 0",  "1.5<<0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 << .5",   "1.5<<0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >>> 0", "1.5>>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >>> .5",  "1.5>>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >> 0",  "1.5>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >> .5",   "1.5>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitShiftsStringCompare
  public void testFoldBitShiftsStringCompare() {
    
    assertResultString("x = -1 << 1", "x=-2");
    assertResultString("x = -1 << 8", "x=-256");
    assertResultString("x = -1 >> 1", "x=-1");
    assertResultString("x = -2 >> 1", "x=-1");
    assertResultString("x = -1 >> 0", "x=-1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testStringAdd
  public void testStringAdd() {
    fold("x = 'a' + \"bc\"", "x = \"abc\"");
    fold("x = 'a' + 5", "x = \"a5\"");
    fold("x = 5 + 'a'", "x = \"5a\"");
    fold("x = 'a' + ''", "x = \"a\"");
    fold("x = \"a\" + foo()", "x = \"a\"+foo()");
    fold("x = foo() + 'a' + 'b'", "x = foo()+\"ab\"");
    fold("x = (foo() + 'a') + 'b'", "x = foo()+\"ab\"");  
    fold("x = foo() + 'a' + 'b' + 'cd' + bar()", "x = foo()+\"abcd\"+bar()");
    fold("x = foo() + 2 + 'b'", "x = foo()+2+\"b\"");  
    fold("x = foo() + 'a' + 2", "x = foo()+\"a2\"");
    fold("x = '' + null", "x = \"null\"");
    fold("x = true + '' + false", "x = \"truefalse\"");
    fold("x = '' + []", "x = ''");      
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldConstructor
  public void testFoldConstructor() {
    fold("x = this[new String('a')]", "x = this['a']");
    fold("x = ob[new String(12)]", "x = ob['12']");
    fold("x = ob[new String(false)]", "x = ob['false']");
    fold("x = ob[new String(null)]", "x = ob['null']");
    fold("x = 'a' + new String('b')", "x = 'ab'");
    fold("x = 'a' + new String(23)", "x = 'a23'");
    fold("x = 2 + new String(1)", "x = '21'");
    foldSame("x = ob[new String(a)]");
    foldSame("x = new String('a')");
    foldSame("x = (new String('a'))[3]");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic
  public void testFoldArithmetic() {
    fold("x = 10 + 20", "x = 30");
    fold("x = 2 / 4", "x = 0.5");
    fold("x = 2.25 * 3", "x = 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = 1 / 0", "x = 1 / 0");
    fold("x = 3 % 2", "x = 1");
    fold("x = 3 % -2", "x = 1");
    fold("x = -1 % 3", "x = -1");
    fold("x = 1 % 0", "x = 1 % 0");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic2
  public void testFoldArithmetic2() {
    foldSame("x = y + 10 + 20");
    foldSame("x = y / 2 / 4");
    fold("x = y * 2.25 * 3", "x = y * 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = y + (z * 24 * 60 * 60 * 1000)", "x = y + z * 864E5");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic3
  public void testFoldArithmetic3() {
    fold("x = null * undefined", "x = NaN");
    fold("x = null * 1", "x = 0");
    fold("x = (null - 1) * 2", "x = -2");
    fold("x = (null + 1) * 2", "x = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmeticInfinity
  public void testFoldArithmeticInfinity() {
    fold("x=-Infinity-2", "x=-Infinity");
    fold("x=Infinity-2", "x=Infinity");
    fold("x=Infinity*5", "x=Infinity");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmeticStringComp
  public void testFoldArithmeticStringComp() {
    
    assertResultString("x = 10 - 20", "x=-10");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison
  public void testFoldComparison() {
    fold("x = 0 == 0", "x = true");
    fold("x = 1 == 2", "x = false");
    fold("x = 'abc' == 'def'", "x = false");
    fold("x = 'abc' == 'abc'", "x = true");
    fold("x = \"\" == ''", "x = true");
    fold("x = foo() == bar()", "x = foo()==bar()");

    fold("x = 1 != 0", "x = true");
    fold("x = 'abc' != 'def'", "x = true");
    fold("x = 'a' != 'a'", "x = false");

    fold("x = 1 < 20", "x = true");
    fold("x = 3 < 3", "x = false");
    fold("x = 10 > 1.0", "x = true");
    fold("x = 10 > 10.25", "x = false");
    fold("x = y == y", "x = y==y");
    fold("x = y < y", "x = false");
    fold("x = y > y", "x = false");
    fold("x = 1 <= 1", "x = true");
    fold("x = 1 <= 0", "x = false");
    fold("x = 0 >= 0", "x = true");
    fold("x = -1 >= 9", "x = false");

    fold("x = true == true", "x = true");
    fold("x = false == false", "x = true");
    fold("x = false == null", "x = false");
    fold("x = false == true", "x = false");
    fold("x = true == null", "x = false");

    fold("0 == 0", "true");
    fold("1 == 2", "false");
    fold("'abc' == 'def'", "false");
    fold("'abc' == 'abc'", "true");
    fold("\"\" == ''", "true");
    foldSame("foo() == bar()");

    fold("1 != 0", "true");
    fold("'abc' != 'def'", "true");
    fold("'a' != 'a'", "false");

    fold("1 < 20", "true");
    fold("3 < 3", "false");
    fold("10 > 1.0", "true");
    fold("10 > 10.25", "false");
    foldSame("x == x");
    fold("x < x", "false");
    fold("x > x", "false");
    fold("1 <= 1", "true");
    fold("1 <= 0", "false");
    fold("0 >= 0", "true");
    fold("-1 >= 9", "false");

    fold("true == true", "true");
    fold("false == null", "false");
    fold("false == true", "false");
    fold("true == null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison2
  public void testFoldComparison2() {
    fold("x = 0 === 0", "x = true");
    fold("x = 1 === 2", "x = false");
    fold("x = 'abc' === 'def'", "x = false");
    fold("x = 'abc' === 'abc'", "x = true");
    fold("x = \"\" === ''", "x = true");
    fold("x = foo() === bar()", "x = foo()===bar()");

    fold("x = 1 !== 0", "x = true");
    fold("x = 'abc' !== 'def'", "x = true");
    fold("x = 'a' !== 'a'", "x = false");

    fold("x = y === y", "x = y===y");

    fold("x = true === true", "x = true");
    fold("x = false === false", "x = true");
    fold("x = false === null", "x = false");
    fold("x = false === true", "x = false");
    fold("x = true === null", "x = false");

    fold("0 === 0", "true");
    fold("1 === 2", "false");
    fold("'abc' === 'def'", "false");
    fold("'abc' === 'abc'", "true");
    fold("\"\" === ''", "true");
    foldSame("foo() === bar()");

    
    foldSame("1 === '1'");
    foldSame("1 === true");
    foldSame("1 !== '1'");
    foldSame("1 !== true");

    fold("1 !== 0", "true");
    fold("'abc' !== 'def'", "true");
    fold("'a' !== 'a'", "false");

    foldSame("x === x");

    fold("true === true", "true");
    fold("false === null", "false");
    fold("false === true", "false");
    fold("true === null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison3
  public void testFoldComparison3() {
    fold("x = !1 == !0", "x = false");

    fold("x = !0 == !0", "x = true");
    fold("x = !1 == !1", "x = true");
    fold("x = !1 == null", "x = false");
    fold("x = !1 == !0", "x = false");
    fold("x = !0 == null", "x = false");

    fold("!0 == !0", "true");
    fold("!1 == null", "false");
    fold("!1 == !0", "false");
    fold("!0 == null", "false");

    fold("x = !0 === !0", "x = true");
    fold("x = !1 === !1", "x = true");
    fold("x = !1 === null", "x = false");
    fold("x = !1 === !0", "x = false");
    fold("x = !0 === null", "x = false");

    fold("!0 === !0", "true");
    fold("!1 === null", "false");
    fold("!1 === !0", "false");
    fold("!0 === null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldGetElem
  public void testFoldGetElem() {
    fold("x = [,10][0]", "x = void 0");
    fold("x = [10, 20][0]", "x = 10");
    fold("x = [10, 20][1]", "x = 20");
    fold("x = [10, 20][0.5]", "",
        PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
    fold("x = [10, 20][-1]",    "",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    fold("x = [10, 20][2]",     "",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComplex
  public void testFoldComplex() {
    fold("x = (3 / 1.0) + (1 * 2)", "x = 5");
    fold("x = (1 == 1.0) && foo() && true", "x = foo()&&true");
    fold("x = 'abc' + 5 + 10", "x = \"abc510\"");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeft
  public void testFoldLeft() {
    foldSame("(+x - 1) + 2"); 
    fold("(+x + 1) + 2", "+x + 3");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArrayLength
  public void testFoldArrayLength() {
    
    fold("x = [].length", "x = 0");
    fold("x = [1,2,3].length", "x = 3");
    fold("x = [a,b].length", "x = 2");

    
    fold("x = [,,1].length", "x = 3");

    
    fold("x = [foo(), 0].length", "x = [foo(),0].length");
    fold("x = y.length", "x = y.length");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldStringLength
  public void testFoldStringLength() {
    
    fold("x = ''.length", "x = 0");
    fold("x = '123'.length", "x = 3");

    
    fold("x = '123\u01dc'.length", "x = 4");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldTypeof
  public void testFoldTypeof() {
    fold("x = typeof 1", "x = \"number\"");
    fold("x = typeof 'foo'", "x = \"string\"");
    fold("x = typeof true", "x = \"boolean\"");
    fold("x = typeof false", "x = \"boolean\"");
    fold("x = typeof null", "x = \"object\"");
    fold("x = typeof undefined", "x = \"undefined\"");
    fold("x = typeof void 0", "x = \"undefined\"");
    fold("x = typeof []", "x = \"object\"");
    fold("x = typeof [1]", "x = \"object\"");
    fold("x = typeof [1,[]]", "x = \"object\"");
    fold("x = typeof {}", "x = \"object\"");
    fold("x = typeof function() {}", "x = 'function'");

    foldSame("x = typeof[1,[foo()]]");
    foldSame("x = typeof{bathwater:baby()}");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldInstanceOf
  public void testFoldInstanceOf() {
    
    fold("64 instanceof Object", "false");
    fold("64 instanceof Number", "false");
    fold("'' instanceof Object", "false");
    fold("'' instanceof String", "false");
    fold("true instanceof Object", "false");
    fold("true instanceof Boolean", "false");
    fold("!0 instanceof Object", "false");
    fold("!0 instanceof Boolean", "false");
    fold("false instanceof Object", "false");
    fold("null instanceof Object", "false");
    fold("undefined instanceof Object", "false");
    fold("NaN instanceof Object", "false");
    fold("Infinity instanceof Object", "false");

    
    fold("[] instanceof Object", "true");
    fold("({}) instanceof Object", "true");

    
    foldSame("new Foo() instanceof Object");
    
    foldSame("[] instanceof Foo");
    foldSame("({}) instanceof Foo");

    fold("(function() {}) instanceof Object", "true");

    
    foldSame("x instanceof Foo");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testDivision
  public void testDivision() {
    
    fold("print(1/3)", "print(1/3)");

    
    
    fold("print(1/2)", "print(0.5)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOpsLate
  public void testAssignOpsLate() {
    late = true;
    fold("x=x+y", "x+=y");
    foldSame("x=y+x");
    fold("x=x*y", "x*=y");
    fold("x=y*x", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");

    fold("x=x-y", "x-=y");
    foldSame("x=y-x");
    fold("x=x|y", "x|=y");
    fold("x=y|x", "x|=y");
    fold("x=x*y", "x*=y");
    fold("x=y*x", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");
    
    fold("({a:1}).a = ({a:1}).a + 1", "({a:1}).a = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOpsEarly
 public void testAssignOpsEarly() {
    late = false;
    foldSame("x=x+y");
    foldSame("x=y+x");
    foldSame("x=x*y");
    foldSame("x=y*x");
    foldSame("x.y=x.y+z");
    foldSame("next().x = next().x + 1");

    foldSame("x=x-y");
    foldSame("x=y-x");
    foldSame("x=x|y");
    foldSame("x=y|x");
    foldSame("x=x*y");
    foldSame("x=y*x");
    foldSame("x.y=x.y+z");
    foldSame("next().x = next().x + 1");
    
    fold("({a:1}).a = ({a:1}).a + 1", "({a:1}).a = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldAdd1
  public void testFoldAdd1() {
    fold("x=false+1","x=1");
    fold("x=true+1","x=2");
    fold("x=1+false","x=1");
    fold("x=1+true","x=2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralNames
  public void testFoldLiteralNames() {
    foldSame("NaN == NaN");
    foldSame("Infinity == Infinity");
    foldSame("Infinity == NaN");
    fold("undefined == NaN", "false");
    fold("undefined == Infinity", "false");

    foldSame("Infinity >= Infinity");
    foldSame("NaN >= NaN");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralsTypeMismatches
  public void testFoldLiteralsTypeMismatches() {
    fold("true == true", "true");
    fold("true == false", "false");
    fold("true == null", "false");
    fold("false == null", "false");

    
    fold("null <= null", "true"); 
    fold("null >= null", "true");
    fold("null > null", "false");
    fold("null < null", "false");

    fold("false >= null", "true"); 
    fold("false <= null", "true");
    fold("false > null", "false");
    fold("false < null", "false");

    fold("true >= null", "true");  
    fold("true <= null", "false");
    fold("true > null", "true");
    fold("true < null", "false");

    fold("true >= false", "true");  
    fold("true <= false", "false");
    fold("true > false", "true");
    fold("true < false", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeftChildConcat
  public void testFoldLeftChildConcat() {
    foldSame("x +5 + \"1\"");
    fold("x+\"5\" + \"1\"", "x + \"51\"");
    
    fold("\"a\"+(\"b\"+c)","\"ab\"+c");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeftChildOp
  public void testFoldLeftChildOp() {
    fold("x * Infinity * 2", "x * Infinity");
    foldSame("x - Infinity - 2"); 
    foldSame("x - 1 + Infinity");
    foldSame("x - 2 + 1");
    foldSame("x - 2 + 3");
    foldSame("1 + x - 2 + 1");
    foldSame("1 + x - 2 + 3");
    foldSame("1 + x - 2 + 3 - 1");
    foldSame("f(x)-0");
    foldSame("x-0-0");
    foldSame("x+2-2+2");
    foldSame("x+2-2+2-2");
    foldSame("x-2+2");
    foldSame("x-2+2-2");
    foldSame("x-2+2-2+2");

    foldSame("1+x-0-NaN");
    foldSame("1+f(x)-0-NaN");
    foldSame("1+x-0+NaN");
    foldSame("1+f(x)-0+NaN");

    foldSame("1+x+NaN"); 
    foldSame("x+2-2");   
    foldSame("x+2");  
    foldSame("x-2");  
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldSimpleArithmeticOp
  public void testFoldSimpleArithmeticOp() {
    foldSame("x*NaN");
    foldSame("NaN/y");
    foldSame("f(x)-0");
    foldSame("f(x)*1");
    foldSame("1*f(x)");
    foldSame("0+a+b");
    foldSame("0-a-b");
    foldSame("a+b-0");
    foldSame("(1+x)*NaN");

    foldSame("(1+f(x))*NaN"); 
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralsAsNumbers
  public void testFoldLiteralsAsNumbers() {
    fold("x/'12'","x/12");
    fold("x/('12'+'6')", "x/126");
    fold("true*x", "1*x");
    fold("x/false", "x/0");  
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testNotFoldBackToTrueFalse
  public void testNotFoldBackToTrueFalse() {
    late = false;
    fold("!0", "true");
    fold("!1", "false");
    fold("!3", "false");

    late = true;
    foldSame("!0");
    foldSame("!1");
    fold("!3", "false");
    foldSame("false");
    foldSame("true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBangConstants
  public void testFoldBangConstants() {
    fold("1 + !0", "2");
    fold("1 + !1", "1");
    fold("'a ' + !1", "'a false'");
    fold("'a ' + !0", "'a true'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldMixed
  public void testFoldMixed() {
    fold("''+[1]", "'1'");
    foldSame("false+[]"); 
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldVoid
  public void testFoldVoid() {
    foldSame("void 0");
    fold("void 1", "void 0");
    fold("void x", "void 0");
    fold("void x()", "void x()");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("(!{})", "false");
    test("(!{a:1})", "false");
    testSame("(!{a:foo()})");
    testSame("(!{'a':foo()})");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testArrayLiteral
  public void testArrayLiteral() {
    test("(![])", "false");
    test("(![1])", "false");
    test("(![a])", "false");
    testSame("(![foo()])");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue601
  public void testIssue601() {
    testSame("'\\v' == 'v'");
    testSame("'v' == '\\v'");
    testSame("'\\u000B' == '\\v'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldObjectLiteralRef1
  public void testFoldObjectLiteralRef1() {
    
    testSame("var x = ({a:foo(),b:bar()}).a");
    testSame("var x = ({a:1,b:bar()}).a");
    testSame("function f() { return {b:foo(), a:2}.a; }");

    
    testSame("({a:x}).a = 1");
    test("({a:x}).a += 1", "({a:x}).a = x + 1");
    testSame("({a:x}).a ++");
    testSame("({a:x}).a --");

    
    testSame("({a:function(){return this}}).a");
    testSame("({get a() {return this}}).a");
    testSame("({set a(b) {return this}}).a");

    
    testSame("({}).a");

    
    testSame("({}).a");
    testSame("({set a(b) {}}).a");
    
    test("({a:1,set a(b) {}}).a", "1");

    
    test("({get a() {}}).a", "(function (){})()");
    
    test("({get a() {},set a(b) {}}).a", "(function (){})()");

    
    test("var x = ({a:function(){return 1}}).a",
         "var x = function(){return 1}");

    test("var x = ({a:1}).a", "var x = 1");
    test("var x = ({a:1, a:2}).a", "var x = 2");
    test("var x = ({a:1, a:foo()}).a", "var x = foo()");
    test("var x = ({a:foo()}).a", "var x = foo()");

    test("function f() { return {a:1, b:2}.a; }",
         "function f() { return 1; }");

    
    test("var x = ({'a':1})['a']", "var x = 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldObjectLiteralRef2
  public void testFoldObjectLiteralRef2() {
    late = false;
    test("({a:x}).a += 1", "({a:x}).a = x + 1");
    late = true;
    testSame("({a:x}).a += 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIEString
  public void testIEString() {
    testSame("!+'\\v1'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue522
  public void testIssue522() {
    testSame("[][1] = 1;");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testInvertibleOperators
  public void testInvertibleOperators() {
    Map<String, String> inverses = ImmutableMap.<String, String>builder()
        .put("==", "!=")
        .put("===", "!==")
        .put("<=", ">")
        .put("<", ">=")
        .put(">=", "<")
        .put(">", "<=")
        .put("!=", "==")
        .put("!==", "===")
        .build();
    Set<String> comparators = ImmutableSet.of("<=", "<", ">=", ">");
    Set<String> equalitors = ImmutableSet.of("==", "===");
    Set<String> uncomparables = ImmutableSet.of("undefined", "void 0");
    List<String> operators = ImmutableList.copyOf(inverses.values());
    for (int iOperandA = 0; iOperandA < LITERAL_OPERANDS.size(); iOperandA++) {
      for (int iOperandB = 0;
           iOperandB < LITERAL_OPERANDS.size();
           iOperandB++) {
        for (int iOp = 0; iOp < operators.size(); iOp++) {
          String a = LITERAL_OPERANDS.get(iOperandA);
          String b = LITERAL_OPERANDS.get(iOperandB);
          String op = operators.get(iOp);
          String inverse = inverses.get(op);

          
          if (comparators.contains(op) &&
              (uncomparables.contains(a) || uncomparables.contains(b))) {
            assertSameResults(join(a, op, b), "false");
            assertSameResults(join(a, inverse, b), "false");
          } else if (a.equals(b) && equalitors.contains(op)) {
            if (a.equals("NaN") || a.equals("Infinity")) {
              foldSame(join(a, op, b));
              foldSame(join(a, inverse, b));
            } else {
              assertSameResults(join(a, op, b), "true");
              assertSameResults(join(a, inverse, b), "false");
            }
          } else {
            assertNotSameResults(join(a, op, b), join(a, inverse, b));
          }
        }
      }
    }
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testCommutativeOperators
  public void testCommutativeOperators() {
    late = true;
    List<String> operators =
        ImmutableList.of(
            "==",
            "!=",
            "===",
            "!==",
            "*",
            "|",
            "&",
            "^");
    for (int iOperandA = 0; iOperandA < LITERAL_OPERANDS.size(); iOperandA++) {
      for (int iOperandB = iOperandA;
           iOperandB < LITERAL_OPERANDS.size();
           iOperandB++) {
        for (int iOp = 0; iOp < operators.size(); iOp++) {
          String a = LITERAL_OPERANDS.get(iOperandA);
          String b = LITERAL_OPERANDS.get(iOperandB);
          String op = operators.get(iOp);

          
          
          assertSameResultsOrUncollapsed(join(a, op, b), join(b, op, a));
        }
      }
    }
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofObject
  public void testFoldTypeofObject() {
    test("var x = {};typeof x",
         "var x = {};\"object\"");

    test("var x = [];typeof x",
         "var x = [];\"object\"");

    
    test("var x = null;typeof x",
         "var x = null;\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofString
  public void testFoldTypeofString() {
    test("var x = \"foo\";typeof x",
         "var x = \"foo\";\"string\"");

    test("var x = new String(\"foo\");typeof x",
         "var x = new String(\"foo\");\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofNumber
  public void testFoldTypeofNumber() {
    test("var x = 10;typeof x",
         "var x = 10;\"number\"");

    test("var x = new Number(6);typeof x",
         "var x = new Number(6);\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofBoolean
  public void testFoldTypeofBoolean() {
    test("var x = false;typeof x",
         "var x = false;\"boolean\"");

    test("var x = new Boolean(true);typeof x",
         "var x = new Boolean(true);\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofUndefined
  public void testFoldTypeofUndefined() {
    test("var x = undefined;typeof x",
         "var x = undefined;\"undefined\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofUnionTypes
  public void testDontFoldTypeofUnionTypes() {
    
    testSame("var x = (unknown ? {} : null);typeof x");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofSideEffects
  public void testDontFoldTypeofSideEffects() {
    
    testSame("var x = 6 ;typeof (x++)");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofWithTypeCheckDisabled
  public void testDontFoldTypeofWithTypeCheckDisabled() {
    disableTypeCheck();
    testSame("var x = {};typeof x");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testTrueFalse
  public void testTrueFalse() {
    late = false;
    foldSame("x = true");
    foldSame("x = false");
    fold("x = !1", "x = false");
    fold("x = !0", "x = true");
    late = true;
    fold("x = true", "x = !0");
    fold("x = false", "x = !1");
    foldSame("x = !1");
    foldSame("x = !0");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldOneChildBlocksIntegration
  public void testFoldOneChildBlocksIntegration() {
     fold("function f(){switch(foo()){default:{break}}}",
          "function f(){foo()}");

     fold("function f(){switch(x){default:{break}}}",
          "function f(){}");

     fold("function f(){switch(x){default:x;case 1:return 2}}",
          "function f(){switch(x){default:case 1:return 2}}");

     
     fold("if(x){if(true){foo();foo()}else{bar();bar()}}",
          "if(x){foo();foo()}");

     fold("if(x){if(false){foo();foo()}else{bar();bar()}}",
          "if(x){bar();bar()}");

     
     fold("if(x()){}", "x()");

     fold("if(x()){} else {x()}", "x()||x()");
     fold("if(x){}", ""); 
     fold("if(a()){A()} else if (b()) {} else {C()}", "a()?A():b()||C()");

     fold("if(a()){} else if (b()) {} else {C()}",
          "a()||b()||C()");
     fold("if(a()){A()} else if (b()) {} else if (c()) {} else{D()}",
          "a()?A():b()||c()||D()");
     fold("if(a()){} else if (b()) {} else if (c()) {} else{D()}",
          "a()||b()||c()||D()");
     fold("if(a()){A()} else if (b()) {} else if (c()) {} else{}",
          "a()?A():b()||c()");

     
     fold("function foo(){if(x()){}}", "function foo(){x()}");

  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldOneChildBlocksStringCompare
  public void testFoldOneChildBlocksStringCompare() {
    
    assertResultString("if(x){if(y){var x;}}else{var z;}",
        "if(x){if(y)var x}else var z");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testNecessaryDanglingElse
  public void testNecessaryDanglingElse() {
    
    
    
    assertResultString(
        "if(x)if(y){y();z()}else;else x()", "if(x){if(y){y();z()}}else x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldReturnsIntegration
  public void testFoldReturnsIntegration() {
    
    fold("function f(){if(x)return;else return}",
         "function f(){}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1059649
  public void testBug1059649() {
    
    fold("if(x){var y=3;}var z=5", "if(x)var y=3;var z=5");

    
    foldSame("if(x){var y=3;}else{var y=4;}var z=5");
    fold("while(x){var y=3;}var z=5", "while(x)var y=3;var z=5");
    fold("for(var i=0;i<10;i++){var y=3;}var z=5",
         "for(var i=0;i<10;i++)var y=3;var z=5");
    fold("for(var i in x){var y=3;}var z=5",
         "for(var i in x)var y=3;var z=5");
    fold("do{var y=3;}while(x);var z=5", "do var y=3;while(x);var z=5");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testHookIfIntegration
  public void testHookIfIntegration() {
    fold("if (false){ x = 1; } else if (cond) { x = 2; } else { x = 3; }",
         "x=cond?2:3");

    fold("x?void 0:y()", "x||y()");
    fold("!x?void 0:y()", "(!x)||y()");
    fold("x?y():void 0", "x&&y()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testRemoveDuplicateStatementsIntegration
  public void testRemoveDuplicateStatementsIntegration() {
    fold("function z() {if (a) { return true }" +
         "else if (b) { return true }" +
         "else { return true }}",
         "function z() {return !0;}");

    fold("function z() {if (a()) { return true }" +
         "else if (b()) { return true }" +
         "else { return true }}",
         "function z() {a()||b();return !0;}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldLogicalOpIntegration
  public void testFoldLogicalOpIntegration() {
    test("if(x && true) z()", "x&&z()");
    test("if(x && false) z()", "");
    fold("if(x || 3) z()", "z()");
    fold("if(x || false) z()", "x&&z()");
    test("if(x==y && false) z()", "");
    
    fold("if(y() || x || 3) z()", "(y()||1)&&z()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldBitwiseOpStringCompareIntegration
  public void testFoldBitwiseOpStringCompareIntegration() {
    assertResultString("while(-1 | 0){}", "while(1);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testVarLiftingIntegration
  public void testVarLiftingIntegration() {
    fold("if(true);else var a;", "var a");
    fold("if(false) foo();else var a;", "var a");
    fold("if(true)var a;else;", "var a");
    fold("if(false)var a;else;", "var a");
    fold("if(false)var a,b;", "var b; var a");
    fold("if(false){var a;var a;}", "var a");
    fold("if(false)var a=function(){var b};", "var a");
    fold("if(a)if(false)var a;else var b;", "var a;if(a)var b");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1438784
  public void testBug1438784() throws Exception {
    fold("for(var i=0;i<10;i++)if(x)x.y;", "for(var i=0;i<10;i++);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessWhileIntegration
  public void testFoldUselessWhileIntegration() {
    fold("while(!true) { foo() }", "");
    fold("while(!false) foo() ", "while(1) foo()");
    fold("while(!void 0) foo()", "while(1) foo()");

    
    fold("if(foo())while(false){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessForIntegration
  public void testFoldUselessForIntegration() {
    fold("for(;!true;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;1;) foo()", "for(;;) foo()");
    fold("for(;!void 0;) foo()", "for(;;) foo()");

    
    fold("if(foo())for(;false;){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessDoIntegration
  public void testFoldUselessDoIntegration() {
    test("do { foo() } while(!true);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(!void 0);", "do { foo() } while(1);");

    
    test("if(foo())do {foo()} while(false) else bar()", "foo()?foo():bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeWhileConstantConditionIntegration
  public void testMinimizeWhileConstantConditionIntegration() {
    fold("while(!false) foo()", "while(1) foo()");
    fold("while(202) foo()", "while(1) foo()");
    fold("while(Infinity) foo()", "while(1) foo()");
    fold("while('text') foo()", "while(1) foo()");
    fold("while([]) foo()", "while(1) foo()");
    fold("while({}) foo()", "while(1) foo()");
    fold("while(/./) foo()", "while(1) foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeExpr
  public void testMinimizeExpr() {
    test("!!true", "");

    fold("!!x()", "x()");
    test("!(!x()&&!y())", "x()||y()");
    fold("x()||!!y()", "x()||y()");

    
    fold("!!x()&&y()", "x()&&y()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1509085
  public void testBug1509085() {
    PeepholeIntegrationTest oneRepetitiontest = new PeepholeIntegrationTest() {
      @Override
      protected int getNumRepetitions() {
        return 1;
      }
    };

    oneRepetitiontest.test("x ? x() : void 0", "x&&x();");
    oneRepetitiontest.foldSame("y = x ? x() : void 0");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBugIssue3
  public void testBugIssue3() {
    foldSame("function foo() {" +
             "  if(sections.length != 1) children[i] = 0;" +
             "  else var selectedid = children[i]" +
             "}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBugIssue43
  public void testBugIssue43() {
    foldSame("function foo() {" +
             "  if (a) { var b = 1; } else { a.b = 1; }" +
             "}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldNegativeBug
  public void testFoldNegativeBug() {
    fold("while(-3){};", "while(1);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testNoNormalizeLabeledExpr
  public void testNoNormalizeLabeledExpr() {
    enableNormalize(true);
    foldSame("var x; foo:{x = 3;}");
    foldSame("var x; foo:x = 3;");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit1
  public void testShortCircuit1() {
    test("1 && a()", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit4
  public void testShortCircuit4() {
    test("a() && (1 && b())", "a() && b()");
    test("a() && 1 && b()", "a() && b()");
    test("(a() && 1) && b()", "a() && b()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeExprCondition
  public void testMinimizeExprCondition() {
    fold("(x || true) && y()", "y()");
    fold("(x || false) && y()", "x&&y()");
    fold("(x && true) && y()", "x && y()");
    fold("(x && false) && y()", "");
    fold("a = x || false ? b : c", "a=x?b:c");
    fold("do {x()} while((x && false) && y())", "x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testTrueFalseFolding
  public void testTrueFalseFolding() {
    fold("x = true", "x = !0");
    fold("x = false", "x = !1");
    fold("x = !3", "x = !1");
    fold("x = true && !0", "x = !0");
    fold("x = !!!!!!!!!!!!3", "x = !0");
    fold("if(!3){x()}", "");
    fold("if(!!3){x()}", "x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testCommaSplitingConstantCondition
  public void testCommaSplitingConstantCondition() {
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testAvoidCommaSplitting
  public void testAvoidCommaSplitting() {
    fold("x(),y(),z()", "x();y();z()");
    late = false;
    foldSame("x(),y(),z()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testObjectLiteral
  public void testObjectLiteral() {
    test("({})", "");
    test("({a:1})", "");
    test("({a:foo()})", "foo()");
    test("({'a':foo()})", "foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testArrayLiteral
  public void testArrayLiteral() {
    test("([])", "");
    test("([1])", "");
    test("([a])", "");
    test("([foo()])", "foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldIfs1
  public void testFoldIfs1() {
    fold("function f() {if (x) return 1; else if (y) return 1;}",
         "function f() {if (x||y) return 1;}");
    fold("function f() {if (x) return 1; else {if (y) return 1; else foo();}}",
         "function f() {if (x||y) return 1; foo();}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldIfs2
  public void testFoldIfs2() {
    fold("function f() {if (x) { a(); } else if (y) { a() }}",
         "function f() {x?a():y&&a();}");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testEmptyPass
  public void testEmptyPass() {
    currentPeepholePasses = ImmutableList.<AbstractPeepholeOptimization>of();

    testSame("var x; var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationOrder
  public void testOptimizationOrder() {
    

    final List<String> visitationLog = Lists.newArrayList();

    AbstractPeepholeOptimization note1Applied =
        new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node node) {
        if (node.isName()) {
          visitationLog.add(node.getString() + "1");
        }

        return node;
      }
    };

    AbstractPeepholeOptimization note2Applied =
        new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node node) {
        if (node.isName()) {
          visitationLog.add(node.getString() + "2");
        }

        return node;
      }
    };

    currentPeepholePasses =
      ImmutableList.<
       AbstractPeepholeOptimization>of(note1Applied, note2Applied);

    test("var x; var y", "var x; var y");

    

    assertEquals(4, visitationLog.size());
    assertEquals("x1", visitationLog.get(0));
    assertEquals("x2", visitationLog.get(1));
    assertEquals("y1", visitationLog.get(2));
    assertEquals("y2", visitationLog.get(3));
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtreeChild
  public void testOptimizationRemovingSubtreeChild() {
    currentPeepholePasses = ImmutableList.<AbstractPeepholeOptimization>of(new
          RemoveNodesNamedXUnderVarOptimization());

    test("var x,y;", "var y;");
    test("var y,x;", "var y;");
    test("var x,y,x;", "var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtree
  public void testOptimizationRemovingSubtree() {
    currentPeepholePasses = ImmutableList.<AbstractPeepholeOptimization>of(new
          RemoveNodesNamedXOptimization());

    test("var x,y;", "var y;");
    test("var y,x;", "var y;");
    test("var x,y,x;", "var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtreeParent
  public void testOptimizationRemovingSubtreeParent() {
    currentPeepholePasses = ImmutableList.<AbstractPeepholeOptimization>of(new
          RemoveParentVarsForNodesNamedX());

    test("var x; var y", "var y");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationsRemoveParentAfterRemoveChild
  public void testOptimizationsRemoveParentAfterRemoveChild() {
    currentPeepholePasses = ImmutableList.<AbstractPeepholeOptimization>of(
          new RemoveNodesNamedXOptimization(),
          new RemoveParentVarsForNodesNamedX());

    test("var x,y; var z;", "var y; var z;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationReplacingNode
  public void testOptimizationReplacingNode() {
    currentPeepholePasses = ImmutableList.<AbstractPeepholeOptimization>of(
          new RenameYToX(),
          new RemoveParentVarsForNodesNamedX());

    test("var y; var z;", "var z;");
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

    
    foldSame("switch(a){case 1: var c =2; break;}");
    foldSame("function f() {switch(a){case 1: return;}}");
    foldSame("x:switch(a){case 1: break x;}");
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

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringIndexOf
  public void testStringIndexOf() {
    fold("x = 'abcdef'.indexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.indexOf('b', 2)", "x = 6");
    fold("x = 'abcdef'.indexOf('bcd')", "x = 1");
    fold("x = 'abcdefsdfasdfbcdassd'.indexOf('bcd', 4)", "x = 13");

    fold("x = 'abcdef'.lastIndexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.lastIndexOf('b')", "x = 6");
    fold("x = 'abcdefbe'.lastIndexOf('b', 5)", "x = 1");

    
    
    fold("x = 'abc1def'.indexOf(1)", "x = 3");
    fold("x = 'abcNaNdef'.indexOf(NaN)", "x = 3");
    fold("x = 'abcundefineddef'.indexOf(undefined)", "x = 3");
    fold("x = 'abcnulldef'.indexOf(null)", "x = 3");
    fold("x = 'abctruedef'.indexOf(true)", "x = 3");

    
    
    foldSame("x = NaN.indexOf('bcd')");
    foldSame("x = undefined.indexOf('bcd')");
    foldSame("x = null.indexOf('bcd')");
    foldSame("x = true.indexOf('bcd')");
    foldSame("x = false.indexOf('bcd')");

    
    foldSame("x = 'abcdef'.indexOf(/b./)");
    foldSame("x = 'abcdef'.indexOf({a:2})");
    foldSame("x = 'abcdef'.indexOf([1,2])");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringJoinAddSparse
  public void testStringJoinAddSparse() {
    fold("x = [,,'a'].join(',')", "x = ',,a'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testNoStringJoin
  public void testNoStringJoin() {
    foldSame("x = [].join(',',2)");
    foldSame("x = [].join(f)");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringJoinAdd
  public void testStringJoinAdd() {
    fold("x = ['a', 'b', 'c'].join('')", "x = \"abc\"");
    fold("x = [].join(',')", "x = \"\"");
    fold("x = ['a'].join(',')", "x = \"a\"");
    fold("x = ['a', 'b', 'c'].join(',')", "x = \"a,b,c\"");
    fold("x = ['a', foo, 'b', 'c'].join(',')",
        "x = [\"a\",foo,\"b,c\"].join()");
    fold("x = [foo, 'a', 'b', 'c'].join(',')",
        "x = [foo,\"a,b,c\"].join()");
    fold("x = ['a', 'b', 'c', foo].join(',')",
        "x = [\"a,b,c\",foo].join()");

    
    fold("x = ['a=', 5].join('')", "x = \"a=5\"");
    fold("x = ['a', '5'].join(7)", "x = \"a75\"");

    
    fold("x = ['a=', false].join('')", "x = \"a=false\"");
    fold("x = ['a', '5'].join(true)", "x = \"atrue5\"");
    fold("x = ['a', '5'].join(false)", "x = \"afalse5\"");

    
    fold("x = ['a', '5', 'c'].join('a very very very long chain')",
         "x = [\"a\",\"5\",\"c\"].join(\"a very very very long chain\")");

    
    foldSame("x = ['', foo].join('-')");
    foldSame("x = ['', foo, ''].join()");

    fold("x = ['', '', foo, ''].join(',')",
         "x = [',', foo, ''].join()");
    fold("x = ['', '', foo, '', ''].join(',')",
         "x = [',', foo, ','].join()");

    fold("x = ['', '', foo, '', '', bar].join(',')",
         "x = [',', foo, ',', bar].join()");

    fold("x = [1,2,3].join('abcdef')",
         "x = '1abcdef2abcdef3'");

    fold("x = [1,2].join()", "x = '1,2'");
    fold("x = [null,undefined,''].join(',')", "x = ',,'");
    fold("x = [null,undefined,0].join(',')", "x = ',,0'");
    
    foldSame("x = [[1,2],[3,4]].join()"); 
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringJoinAdd_b1992789
  public void testStringJoinAdd_b1992789() {
    fold("x = ['a'].join('')", "x = \"a\"");
    fold("x = [foo()].join('')", "x = '' + foo()");
    fold("[foo()].join('')", "'' + foo()");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringSubstr
  public void testFoldStringSubstr() {
    fold("x = 'abcde'.substr(0,2)", "x = 'ab'");
    fold("x = 'abcde'.substr(1,2)", "x = 'bc'");
    fold("x = 'abcde'['substr'](1,3)", "x = 'bcd'");
    fold("x = 'abcde'.substr(2)", "x = 'cde'");

    
    foldSame("x = 'abcde'.substr(-1)");
    foldSame("x = 'abcde'.substr(1, -2)");
    foldSame("x = 'abcde'.substr(1, 2, 3)");
    foldSame("x = 'a'.substr(0, 2)");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringSubstring
  public void testFoldStringSubstring() {
    fold("x = 'abcde'.substring(0,2)", "x = 'ab'");
    fold("x = 'abcde'.substring(1,2)", "x = 'b'");
    fold("x = 'abcde'['substring'](1,3)", "x = 'bc'");
    fold("x = 'abcde'.substring(2)", "x = 'cde'");

    
    foldSame("x = 'abcde'.substring(-1)");
    foldSame("x = 'abcde'.substring(1, -2)");
    foldSame("x = 'abcde'.substring(1, 2, 3)");
    foldSame("x = 'a'.substring(0, 2)");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringCharAt
  public void testFoldStringCharAt() {
    fold("x = 'abcde'.charAt(0)", "x = 'a'");
    fold("x = 'abcde'.charAt(1)", "x = 'b'");
    fold("x = 'abcde'.charAt(2)", "x = 'c'");
    fold("x = 'abcde'.charAt(3)", "x = 'd'");
    fold("x = 'abcde'.charAt(4)", "x = 'e'");
    foldSame("x = 'abcde'.charAt(5)");  
    foldSame("x = 'abcde'.charAt(-1)");  
    foldSame("x = 'abcde'.charAt(y)");
    foldSame("x = 'abcde'.charAt()");  
    foldSame("x = 'abcde'.charAt(0, ++z)");  
    foldSame("x = 'abcde'.charAt(null)");  
    foldSame("x = 'abcde'.charAt(true)");  
    fold("x = '\\ud834\udd1e'.charAt(0)", "x = '\\ud834'");
    fold("x = '\\ud834\udd1e'.charAt(1)", "x = '\\udd1e'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringCharCodeAt
  public void testFoldStringCharCodeAt() {
    fold("x = 'abcde'.charCodeAt(0)", "x = 97");
    fold("x = 'abcde'.charCodeAt(1)", "x = 98");
    fold("x = 'abcde'.charCodeAt(2)", "x = 99");
    fold("x = 'abcde'.charCodeAt(3)", "x = 100");
    fold("x = 'abcde'.charCodeAt(4)", "x = 101");
    foldSame("x = 'abcde'.charCodeAt(5)");  
    foldSame("x = 'abcde'.charCodeAt(-1)");  
    foldSame("x = 'abcde'.charCodeAt(y)");
    foldSame("x = 'abcde'.charCodeAt()");  
    foldSame("x = 'abcde'.charCodeAt(0, ++z)");  
    foldSame("x = 'abcde'.charCodeAt(null)");  
    foldSame("x = 'abcde'.charCodeAt(true)");  
    fold("x = '\\ud834\udd1e'.charCodeAt(0)", "x = 55348");
    fold("x = '\\ud834\udd1e'.charCodeAt(1)", "x = 56606");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringSplit
  public void testFoldStringSplit() {
    late = false;
    fold("x = 'abcde'.split('foo')", "x = ['abcde']");
    fold("x = 'abcde'.split()", "x = ['abcde']");
    fold("x = 'abcde'.split(null)", "x = ['abcde']");
    fold("x = 'a b c d e'.split(' ')", "x = ['a','b','c','d','e']");
    fold("x = 'a b c d e'.split(' ', 0)", "x = []");
    fold("x = 'abcde'.split('cd')", "x = ['ab','e']");
    fold("x = 'a b c d e'.split(' ', 1)", "x = ['a']");
    fold("x = 'a b c d e'.split(' ', 3)", "x = ['a','b','c']");
    fold("x = 'a b c d e'.split(null, 1)", "x = ['a b c d e']");
    fold("x = 'aaaaa'.split('a')", "x = ['', '', '', '', '']");
    fold("x = 'abcde'.split('')", "x = ['a','b','c','d','e']");
    fold("x = 'abcde'.split('', 3)", "x = ['a','b','c']");

    foldSame("x = 'abcde'.split(/ /)");
    foldSame("x = 'abcde'.split(' ', -1)");

    late = true;
    foldSame("x = 'a b c d e'.split(' ')");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testJoinBug
  public void testJoinBug() {
    fold("var x = [].join();", "var x = '';");
    fold("var x = [x].join();", "var x = '' + x;");
    foldSame("var x = [x,y].join();");
    foldSame("var x = [x,y,z].join();");

    foldSame("shape['matrix'] = [\n" +
            "    Number(headingCos2).toFixed(4),\n" +
            "    Number(-headingSin2).toFixed(4),\n" +
            "    Number(headingSin2 * yScale).toFixed(4),\n" +
            "    Number(headingCos2 * yScale).toFixed(4),\n" +
            "    0,\n" +
            "    0\n" +
            "  ].join()");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testToUpper
  public void testToUpper() {
    fold("'a'.toUpperCase()", "'A'");
    fold("'A'.toUpperCase()", "'A'");
    fold("'aBcDe'.toUpperCase()", "'ABCDE'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testToLower
  public void testToLower() {
    fold("'A'.toLowerCase()", "'a'");
    fold("'a'.toLowerCase()", "'a'");
    fold("'aBcDe'.toLowerCase()", "'abcde'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldParseNumbers
  public void testFoldParseNumbers() {
    enableNormalize();
    enableEcmaScript5(true);

    fold("x = parseInt('123')", "x = 123");
    fold("x = parseInt(' 123')", "x = 123");
    fold("x = parseInt('123', 10)", "x = 123");
    fold("x = parseInt('0xA')", "x = 10");
    fold("x = parseInt('0xA', 16)", "x = 10");
    fold("x = parseInt('07', 8)", "x = 7");
    fold("x = parseInt('08')", "x = 8");
    fold("x = parseFloat('1.23')", "x = 1.23");
    fold("x = parseFloat('1.2300')", "x = 1.23");
    fold("x = parseFloat(' 0.3333')", "x = 0.3333");

    
    fold("x = parseInt(' 0xF', 16)", "x = 15");
    fold("x = parseInt(' F', 16)", "x = 15");
    fold("x = parseInt('17', 8)", "x = 15");
    fold("x = parseInt('015', 10)", "x = 15");
    fold("x = parseInt('1111', 2)", "x = 15");
    fold("x = parseInt('12', 13)", "x = 15");
    fold("x = parseInt(021, 8)", "x = 15");
    fold("x = parseInt(15.99, 10)", "x = 15");
    fold("x = parseFloat('3.14')", "x = 3.14");
    fold("x = parseFloat(3.14)", "x = 3.14");

    
    foldSame("x = parseInt('FXX123', 16)");
    foldSame("x = parseInt('15*3', 10)");
    foldSame("x = parseInt('15e2', 10)");
    foldSame("x = parseInt('15px', 10)");
    foldSame("x = parseInt('-0x08')");
    foldSame("x = parseInt('1', -1)");
    foldSame("x = parseFloat('3.14more non-digit characters')");
    foldSame("x = parseFloat('314e-2')");
    foldSame("x = parseFloat('0.0314E+2')");
    foldSame("x = parseFloat('3.333333333333333333333333')");

    
    foldSame("x = parseInt('0xa', 10)");

    enableEcmaScript5(false);
    foldSame("x = parseInt('08')");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldOneChildBlocks
  public void testFoldOneChildBlocks() {
    late = false;
    fold("function f(){if(x)a();x=3}",
        "function f(){x&&a();x=3}");
    fold("function f(){if(x){a()}x=3}",
        "function f(){x&&a();x=3}");
    fold("function f(){if(x){return 3}}",
        "function f(){if(x)return 3}");
    fold("function f(){if(x){a()}}",
        "function f(){x&&a()}");
    fold("function f(){if(x){throw 1}}", "function f(){if(x)throw 1;}");

    
    fold("function f(){if(x){foo()}}", "function f(){x&&foo()}");
    fold("function f(){if(x){foo()}else{bar()}}",
         "function f(){x?foo():bar()}");

    
    fold("function f(){if(x){a.b=1}}", "function f(){if(x)a.b=1}");
    fold("function f(){if(x){a.b*=1}}", "function f(){x&&(a.b*=1)}");
    fold("function f(){if(x){a.b+=1}}", "function f(){x&&(a.b+=1)}");
    fold("function f(){if(x){++a.b}}", "function f(){x&&++a.b}");
    fold("function f(){if(x){a.foo()}}", "function f(){x&&a.foo()}");

    
    fold("function f(){try{foo()}catch(e){bar(e)}finally{baz()}}",
         "function f(){try{foo()}catch(e){bar(e)}finally{baz()}}");

    
    fold("function f(){switch(x){case 1:break}}",
         "function f(){switch(x){case 1:break}}");

    
    fold("function f(){if(e1){do foo();while(e2)}else foo2()}",
         "function f(){if(e1){do foo();while(e2)}else foo2()}");
    
    fold("if(x){do{foo()}while(y)}else bar()",
         "if(x){do foo();while(y)}else bar()");

    
    fold("function f(){if(x){if(y)foo()}}",
         "function f(){x&&y&&foo()}");
    fold("function f(){if(x){if(y)foo();else bar()}}",
         "function f(){x&&(y?foo():bar())}");
    fold("function f(){if(x){if(y)foo()}else bar()}",
         "function f(){x?y&&foo():bar()}");
    fold("function f(){if(x){if(y)foo();else bar()}else{baz()}}",
         "function f(){x?y?foo():bar():baz()}");

    fold("if(e1){while(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)while(e2)e3&&foo();else bar()");

    fold("if(e1){with(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)with(e2)e3&&foo();else bar()");

    fold("if(a||b){if(c||d){var x;}}", "if(a||b)if(c||d)var x");
    fold("if(x){ if(y){var x;}else{var z;} }",
         "if(x)if(y)var x;else var z");

    
    
    
    fold("if(x){ if(y){var x;}else{var z;} }else{var w}",
         "if(x)if(y)var x;else var z;else var w");
    fold("if (x) {var x;}else { if (y) { var y;} }",
         "if(x)var x;else if(y)var y");

    
    fold("if(a){if(b){f1();f2();}else if(c){f3();}}else {if(d){f4();}}",
         "if(a)if(b){f1();f2()}else c&&f3();else d&&f4()");

    fold("function f(){foo()}", "function f(){foo()}");
    fold("switch(x){case y: foo()}", "switch(x){case y:foo()}");
    fold("try{foo()}catch(ex){bar()}finally{baz()}",
         "try{foo()}catch(ex){bar()}finally{baz()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldReturns
  public void testFoldReturns() {
    fold("function f(){if(x)return 1;else return 2}",
         "function f(){return x?1:2}");
    fold("function f(){if(x)return 1;return 2}",
         "function f(){return x?1:2}");
    fold("function f(){if(x)return;return 2}",
         "function f(){return x?void 0:2}");
    fold("function f(){if(x)return 1+x;else return 2-x}",
         "function f(){return x?1+x:2-x}");
    fold("function f(){if(x)return 1+x;return 2-x}",
         "function f(){return x?1+x:2-x}");
    fold("function f(){if(x)return y += 1;else return y += 2}",
         "function f(){return x?(y+=1):(y+=2)}");

    fold("function f(){if(x)return;else return 2-x}",
         "function f(){if(x);else return 2-x}");
    fold("function f(){if(x)return;return 2-x}",
         "function f(){return x?void 0:2-x}");
    fold("function f(){if(x)return x;else return}",
         "function f(){if(x)return x;{}}");
    fold("function f(){if(x)return x;return}",
         "function f(){if(x)return x}");

    foldSame("function f(){for(var x in y) { return x.y; } return k}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testCombineIfs1
  public void testCombineIfs1() {
    fold("function f() {if (x) return 1; if (y) return 1}",
         "function f() {if (x||y) return 1;}");
    fold("function f() {if (x) return 1; if (y) foo(); else return 1}",
         "function f() {if ((!x)&&y) foo(); else return 1;}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testCombineIfs2
  public void testCombineIfs2() {
    
    foldSame("function f() {if (x) throw 1; if (y) throw 1}");
    
    fold("function f(){ if (x) g(); if (y) g() }",
         "function f(){ x&&g(); y&&g() }");
    
    fold("function f(){ if (x) y = 0; if (y) y = 0; }",
         "function f(){ x&&(y = 0); y&&(y = 0); }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testCombineIfs3
  public void testCombineIfs3() {
    foldSame("function f() {if (x) return 1; if (y) {g();f()}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldAssignments
  public void testFoldAssignments() {
    fold("function f(){if(x)y=3;else y=4;}", "function f(){y=x?3:4}");
    fold("function f(){if(x)y=1+a;else y=2+a;}", "function f(){y=x?1+a:2+a}");

    
    fold("function f(){if(x)y+=1;else y+=2;}", "function f(){y+=x?1:2}");
    fold("function f(){if(x)y-=1;else y-=2;}", "function f(){y-=x?1:2}");
    fold("function f(){if(x)y%=1;else y%=2;}", "function f(){y%=x?1:2}");
    fold("function f(){if(x)y|=1;else y|=2;}", "function f(){y|=x?1:2}");

    
    foldSame("function f(){x ? y-=1 : y+=2}");

    
    foldSame("function f(){x ? y-=1 : z-=1}");

    
    foldSame("function f(){x ? y().a=3 : y().a=4}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateStatements
  public void testRemoveDuplicateStatements() {
    fold("if (a) { x = 1; x++ } else { x = 2; x++ }",
         "x=(a) ? 1 : 2; x++");
    fold("if (a) { x = 1; x++; y += 1; z = pi; }" +
         " else  { x = 2; x++; y += 1; z = pi; }",
         "x=(a) ? 1 : 2; x++; y += 1; z = pi;");
    fold("function z() {" +
         "if (a) { foo(); return !0 } else { goo(); return !0 }" +
         "}",
         "function z() {(a) ? foo() : goo(); return !0}");
    fold("function z() {if (a) { foo(); x = true; return true " +
         "} else { goo(); x = true; return true }}",
         "function z() {(a) ? foo() : goo(); x = !0; return !0}");

    fold("function z() {" +
         "  if (a) { bar(); foo(); return true }" +
         "    else { bar(); goo(); return true }" +
         "}",
         "function z() {" +
         "  if (a) { bar(); foo(); }" +
         "    else { bar(); goo(); }" +
         "  return !0;" +
         "}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testNotCond
  public void testNotCond() {
    fold("function f(){if(!x)foo()}", "function f(){x||foo()}");
    fold("function f(){if(!x)b=1}", "function f(){x||(b=1)}");
    fold("if(!x)z=1;else if(y)z=2", "x ? y&&(z=2) : z=1");
    foldSame("function f(){if(!(x=1))a.b=1}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testAndParenthesesCount
  public void testAndParenthesesCount() {
    fold("function f(){if(x||y)a.foo()}", "function f(){(x||y)&&a.foo()}");
    fold("function f(){if(x.a)x.a=0}",
         "function f(){x.a&&(x.a=0)}");
    foldSame("function f(){if(x()||y()){x()||y()}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLogicalOpStringCompare
  public void testFoldLogicalOpStringCompare() {
    
    
    assertResultString("if(foo() && false) z()", "foo()&&0&&z()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldNot
  public void testFoldNot() {
    fold("while(!(x==y)){a=b;}" , "while(x!=y){a=b;}");
    fold("while(!(x!=y)){a=b;}" , "while(x==y){a=b;}");
    fold("while(!(x===y)){a=b;}", "while(x!==y){a=b;}");
    fold("while(!(x!==y)){a=b;}", "while(x===y){a=b;}");
    
    foldSame("while(!(x>y)){a=b;}");
    foldSame("while(!(x>=y)){a=b;}");
    foldSame("while(!(x<y)){a=b;}");
    foldSame("while(!(x<=y)){a=b;}");
    foldSame("while(!(x<=NaN)){a=b;}");

    
    fold("x = !(y() && true)", "x = !y()");
    
    fold("x = !true", "x = !1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldRegExpConstructor
  public void testFoldRegExpConstructor() {
    enableNormalize();

    
    fold("x = new RegExp",                    "x = RegExp()");
    
    fold("x = new RegExp(\"\")",              "x = RegExp(\"\")");
    fold("x = new RegExp(\"\", \"i\")",       "x = RegExp(\"\",\"i\")");
    
    fold("x = new RegExp(\"foobar\", \"bogus\")",
         "x = RegExp(\"foobar\",\"bogus\")",
         PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS);
    
    fold("x = new RegExp(\"foobar\")",        "x = /foobar/");
    fold("x = RegExp(\"foobar\")",            "x = /foobar/");
    fold("x = new RegExp(\"foobar\", \"i\")", "x = /foobar/i");
    
    fold("x = new RegExp(\"\\\\.\", \"i\")",  "x = /\\./i");
    fold("x = new RegExp(\"/\", \"\")",       "x = /\\//");
    fold("x = new RegExp(\"[/]\", \"\")",     "x = /[/]/");
    fold("x = new RegExp(\"///\", \"\")",     "x = /\\/\\/\\//");
    fold("x = new RegExp(\"\\\\\\/\", \"\")", "x = /\\//");
    fold("x = new RegExp(\"\\n\")",           "x = /\\n/");
    fold("x = new RegExp('\\\\\\r')",         "x = /\\r/");

    
    
    String longRegexp = "";
    for (int i = 0; i < 200; i++) longRegexp += "x";
    foldSame("x = RegExp(\"" + longRegexp + "\")");

    
    
    disableNormalize();

    foldSame("x = new RegExp(\"foobar\")");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testVersionSpecificRegExpQuirks
  public void testVersionSpecificRegExpQuirks() {
    enableNormalize();

    
    enableEcmaScript5(false);
    fold("x = new RegExp(\"foobar\", \"g\")",
         "x = RegExp(\"foobar\",\"g\")");
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = RegExp(\"foobar\",\"ig\")");
    
    enableEcmaScript5(true);
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = /foobar/ig");
    
    
    enableEcmaScript5(false);
    fold("x = new RegExp(\"\\u2028\")", "x = RegExp(\"\\u2028\")");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");
    
    enableEcmaScript5(true);
    fold("x = new RegExp(\"\\u2028\\u2029\")", "x = /\\u2028\\u2029/");
    fold("x = new RegExp(\"\\\\u2028\")", "x = /\\u2028/");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldRegExpConstructorStringCompare
  public void testFoldRegExpConstructorStringCompare() {
    
    
    assertResultString("x=new RegExp(\"\\n\", \"i\")", "x=/\\n/i", true);
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testContainsUnicodeEscape
  public void testContainsUnicodeEscape() throws Exception {
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(""));
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("foo"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "\u2028"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "\\u2028"));
    assertTrue(
        PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("foo\\u2028"));
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "foo\\\\u2028"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
            "foo\\\\u2028bar\\u2028"));
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLiteralObjectConstructors
  public void testFoldLiteralObjectConstructors() {
    enableNormalize();

    
    fold("x = new Object", "x = ({})");
    fold("x = new Object()", "x = ({})");
    fold("x = Object()", "x = ({})");

    disableNormalize();
    
    foldSame("x = new Object");
    foldSame("x = new Object()");
    foldSame("x = Object()");

    enableNormalize();

    
    foldSame("x = " +
         "(function f(){function Object(){this.x=4};return new Object();})();");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLiteralArrayConstructors
  public void testFoldLiteralArrayConstructors() {
    enableNormalize();

    
    fold("x = new Array", "x = []");
    fold("x = new Array()", "x = []");
    fold("x = Array()", "x = []");

    
    fold("x = new Array(0)", "x = []");
    fold("x = Array(0)", "x = []");
    fold("x = new Array(\"a\")", "x = [\"a\"]");
    fold("x = Array(\"a\")", "x = [\"a\"]");

    
    fold("x = new Array(7)", "x = Array(7)");
    fold("x = Array(7)", "x = Array(7)");
    fold("x = new Array(y)", "x = Array(y)");
    fold("x = Array(y)", "x = Array(y)");
    fold("x = new Array(foo())", "x = Array(foo())");
    fold("x = Array(foo())", "x = Array(foo())");

    
    fold("x = new Array(1, 2, 3, 4)", "x = [1, 2, 3, 4]");
    fold("x = Array(1, 2, 3, 4)", "x = [1, 2, 3, 4]");
    fold("x = new Array('a', 1, 2, 'bc', 3, {}, 'abc')",
         "x = ['a', 1, 2, 'bc', 3, {}, 'abc']");
    fold("x = Array('a', 1, 2, 'bc', 3, {}, 'abc')",
         "x = ['a', 1, 2, 'bc', 3, {}, 'abc']");
    fold("x = new Array(Array(1, '2', 3, '4'))", "x = [[1, '2', 3, '4']]");
    fold("x = Array(Array(1, '2', 3, '4'))", "x = [[1, '2', 3, '4']]");
    fold("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))",
         "x = [{}, [\"abc\", {}, [[]]]]");
    fold("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))",
         "x = [{}, [\"abc\", {}, [[]]]]");

    disableNormalize();
    
    foldSame("x = new Array");
    foldSame("x = new Array()");
    foldSame("x = Array()");

    foldSame("x = new Array(0)");
    foldSame("x = Array(0)");
    foldSame("x = new Array(\"a\")");
    foldSame("x = Array(\"a\")");
    foldSame("x = new Array(7)");
    foldSame("x = Array(7)");
    foldSame("x = new Array(foo())");
    foldSame("x = Array(foo())");

    foldSame("x = new Array(1, 2, 3, 4)");
    foldSame("x = Array(1, 2, 3, 4)");
    foldSame("x = new Array('a', 1, 2, 'bc', 3, {}, 'abc')");
    foldSame("x = Array('a', 1, 2, 'bc', 3, {}, 'abc')");
    foldSame("x = new Array(Array(1, '2', 3, '4'))");
    foldSame("x = Array(Array(1, '2', 3, '4'))");
    foldSame("x = new Array(" +
        "Object(), Array(\"abc\", Object(), Array(Array())))");
    foldSame("x = new Array(" +
        "Object(), Array(\"abc\", Object(), Array(Array())))");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeExprCondition
  public void testMinimizeExprCondition() {
    fold("(x ? true : false) && y()", "x&&y()");
    fold("(x ? false : true) && y()", "(!x)&&y()");
    fold("(x ? true : y) && y()", "(x || y)&&y()");
    fold("(x ? y : false) && y()", "(x && y)&&y()");
    fold("(x && true) && y()", "x && y()");
    fold("(x && false) && y()", "0&&y()");
    fold("(x || true) && y()", "1&&y()");
    fold("(x || false) && y()", "x&&y()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeWhileCondition
  public void testMinimizeWhileCondition() {
    
    fold("while(!!true) foo()", "while(1) foo()");
    
    fold("while(!!x) foo()", "while(x) foo()");
    fold("while(!(!x&&!y)) foo()", "while(x||y) foo()");
    fold("while(x||!!y) foo()", "while(x||y) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!x||!y) foo()");
    fold("while(!(!x&&y)) foo()", "while(x||!y) foo()");
    fold("while(!(x||!y)) foo()", "while(!x&&y) foo()");
    fold("while(!(x||y)) foo()", "while(!x&&!y) foo()");
    fold("while(!(!x||y-z)) foo()", "while(x&&!(y-z)) foo()");
    fold("while(!(!(x/y)||z+w)) foo()", "while(x/y&&!(z+w)) foo()");
    foldSame("while(!(x+y||z)) foo()");
    foldSame("while(!(x&&y*z)) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!x||!y) foo()");
    fold("while(x&&!0) foo()", "while(x) foo()");
    fold("while(x||!1) foo()", "while(x) foo()");
    fold("while(!((x,y)&&z)) foo()", "while(!(x,y)||!z) foo()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeForCondition
  public void testMinimizeForCondition() {
    
    
    fold("for(;!!true;) foo()", "for(;1;) foo()");
    
    fold("for(!!true;;) foo()", "for(!0;;) foo()");

    
    fold("for(;!!x;) foo()", "for(;x;) foo()");

    
    foldSame("for(a in b) foo()");
    foldSame("for(a in {}) foo()");
    foldSame("for(a in []) foo()");
    fold("for(a in !!true) foo()", "for(a in !0) foo()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeCondition_example1
  public void testMinimizeCondition_example1() {
    
    fold("if(!!(f() > 20)) {foo();foo()}", "if(f() > 20){foo();foo()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLoopBreakLate
  public void testFoldLoopBreakLate() {
    late = true;
    fold("for(;;) if (a) break", "for(;!a;);");
    foldSame("for(;;) if (a) { f(); break }");
    fold("for(;;) if (a) break; else f()", "for(;!a;) { { f(); } }");
    fold("for(;a;) if (b) break", "for(;a && !b;);");
    fold("for(;a;) { if (b) break; if (c) break; }", "for(;(a && !b) && !c;);");

    
    enableNormalize(true);
    fold("while(true) if (a) break", "for(;1&&!a;);");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLoopBreakEarly
  public void testFoldLoopBreakEarly() {
    late = false;
    foldSame("for(;;) if (a) break");
    foldSame("for(;;) if (a) { f(); break }");
    foldSame("for(;;) if (a) break; else f()");
    foldSame("for(;a;) if (b) break");
    foldSame("for(;a;) { if (b) break; if (c) break; }");

    foldSame("while(1) if (a) break");
    enableNormalize(true);
    foldSame("while(1) if (a) break");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldConditionalVarDeclaration
  public void testFoldConditionalVarDeclaration() {
    fold("if(x) var y=1;else y=2", "var y=x?1:2");
    fold("if(x) y=1;else var y=2", "var y=x?1:2");

    foldSame("if(x) var y = 1; z = 2");
    foldSame("if(x||y) y = 1; var z = 2");

    foldSame("if(x) { var y = 1; print(y)} else y = 2 ");
    foldSame("if(x) var y = 1; else {y = 2; print(y)}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldReturnResult
  public void testFoldReturnResult() {
    fold("function f(){return false;}", "function f(){return !1}");
    foldSame("function f(){return null;}");
    fold("function f(){return void 0;}",
         "function f(){}");
    foldSame("function f(){return void foo();}");
    fold("function f(){return undefined;}",
         "function f(){}");
    fold("function f(){if(a()){return undefined;}}",
         "function f(){if(a()){}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldStandardConstructors
  public void testFoldStandardConstructors() {
    foldSame("new Foo('a')");
    foldSame("var x = new goog.Foo(1)");
    foldSame("var x = new String(1)");
    foldSame("var x = new Number(1)");
    foldSame("var x = new Boolean(1)");

    enableNormalize();

    fold("var x = new Object('a')", "var x = Object('a')");
    fold("var x = new RegExp('')", "var x = RegExp('')");
    fold("var x = new Error('20')", "var x = Error(\"20\")");
    fold("var x = new Array(20)", "var x = Array(20)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSubsituteReturn
  public void testSubsituteReturn() {

    fold("function f() { while(x) { return }}",
         "function f() { while(x) { break }}");

    foldSame("function f() { while(x) { return 5 } }");

    foldSame("function f() { a: { return 5 } }");

    fold("function f() { while(x) { return 5}  return 5}",
         "function f() { while(x) { break }    return 5}");

    fold("function f() { while(x) { return x}  return x}",
         "function f() { while(x) { break }    return x}");

    fold("function f() { while(x) { if (y) { return }}}",
         "function f() { while(x) { if (y) { break  }}}");

    fold("function f() { while(x) { if (y) { return }} return}",
         "function f() { while(x) { if (y) { break  }}}");

    fold("function f() { while(x) { if (y) { return 5 }} return 5}",
         "function f() { while(x) { if (y) { break    }} return 5}");

    
    
    fold("function f() { while(x) { if (y) { return x } x = 1} return x}",
         "function f() { while(x) { if (y) { break    } x = 1} return x}");

    
    fold("function f() { while(x) { if (y) { return x } return x} return x}",
         "function f() { while(x) { if (y) {} break }return x}");

    
    foldSame("function f() { while(x) { while (y) { return } } }");

    foldSame("function f() { while(1) { return 7}  return 5}");

    foldSame("function f() {" +
             "  try { while(x) {return f()}} catch (e) { } return f()}");

    foldSame("function f() {" +
             "  try { while(x) {return f()}} finally {alert(1)} return f()}");

    
    fold("function f() {" +
         "  try { while(x) { return f() } return f() } catch (e) { } }",
         "function f() {" +
         "  try { while(x) { break } return f() } catch (e) { } }");

    
    foldSame("function f() {" +
             "  try { while(x) { return foo() } } finally { alert(1) } "  +
             "  return foo()}");

    
    fold("function f() {" +
         "  try { while(x) { return 1 } } finally { alert(1) } return 1}",
         "function f() {" +
         "  try { while(x) { break    } } finally { alert(1) } return 1}"
         );

    foldSame("function f() { try{ return a } finally { a = 2 } return a; }");

    fold(
      "function f() { switch(a){ case 1: return a; default: g();} return a;}",
      "function f() { switch(a){ case 1: break; default: g();} return a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSubsituteBreakForThrow
  public void testSubsituteBreakForThrow() {

    foldSame("function f() { while(x) { throw Error }}");

    fold("function f() { while(x) { throw Error } throw Error }",
         "function f() { while(x) { break } throw Error}");
    foldSame("function f() { while(x) { throw Error(1) } throw Error(2)}");
    foldSame("function f() { while(x) { throw Error(1) } return Error(2)}");

    foldSame("function f() { while(x) { throw 5 } }");

    foldSame("function f() { a: { throw 5 } }");

    fold("function f() { while(x) { throw 5}  throw 5}",
         "function f() { while(x) { break }   throw 5}");

    fold("function f() { while(x) { throw x}  throw x}",
         "function f() { while(x) { break }   throw x}");

    foldSame("function f() { while(x) { if (y) { throw Error }}}");

    fold("function f() { while(x) { if (y) { throw Error }} throw Error}",
         "function f() { while(x) { if (y) { break }} throw Error}");

    fold("function f() { while(x) { if (y) { throw 5 }} throw 5}",
         "function f() { while(x) { if (y) { break    }} throw 5}");

    
    
    fold("function f() { while(x) { if (y) { throw x } x = 1} throw x}",
         "function f() { while(x) { if (y) { break    } x = 1} throw x}");

    
    fold("function f() { while(x) { if (y) { throw x } throw x} throw x}",
         "function f() { while(x) { if (y) {} break }throw x}");

    
    foldSame("function f() { while(x) { while (y) { throw Error } } }");

    foldSame("function f() { while(1) { throw 7}  throw 5}");

    foldSame("function f() {" +
             "  try { while(x) {throw f()}} catch (e) { } throw f()}");

    foldSame("function f() {" +
             "  try { while(x) {throw f()}} finally {alert(1)} throw f()}");

    
    fold("function f() {" +
         "  try { while(x) { throw f() } throw f() } catch (e) { } }",
         "function f() {" +
         "  try { while(x) { break } throw f() } catch (e) { } }");

    
    foldSame("function f() {" +
             "  try { while(x) { throw foo() } } finally { alert(1) } "  +
             "  throw foo()}");

    
    fold("function f() {" +
         "  try { while(x) { throw 1 } } finally { alert(1) } throw 1}",
         "function f() {" +
         "  try { while(x) { break    } } finally { alert(1) } throw 1}"
         );

    foldSame("function f() { try{ throw a } finally { a = 2 } throw a; }");

    fold(
      "function f() { switch(a){ case 1: throw a; default: g();} throw a;}",
      "function f() { switch(a){ case 1: break; default: g();} throw a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateReturn
  public void testRemoveDuplicateReturn() {
    fold("function f() { return; }",
         "function f(){}");
    foldSame("function f() { return a; }");
    fold("function f() { if (x) { return a } return a; }",
         "function f() { if (x) {} return a; }");
    foldSame(
      "function f() { try { if (x) { return a } } catch(e) {} return a; }");
    foldSame(
      "function f() { try { if (x) {} } catch(e) {} return 1; }");

    
    foldSame(
      "function f() { try { if (x) { return a } } finally { a++ } return a; }");
    
    
    fold("function f() { try { if (x) { return 1 } } finally {} return 1; }",
         "function f() { try { if (x) {} } finally {} return 1; }");

    fold("function f() { switch(a){ case 1: return a; } return a; }",
         "function f() { switch(a){ case 1: } return a; }");

    fold("function f() { switch(a){ " +
         "  case 1: return a; case 2: return a; } return a; }",
         "function f() { switch(a){ " +
         "  case 1: break; case 2: } return a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateThrow
  public void testRemoveDuplicateThrow() {
    foldSame("function f() { throw a; }");
    fold("function f() { if (x) { throw a } throw a; }",
         "function f() { if (x) {} throw a; }");
    foldSame(
      "function f() { try { if (x) {throw a} } catch(e) {} throw a; }");
    foldSame(
      "function f() { try { if (x) {throw 1} } catch(e) {f()} throw 1; }");
    foldSame(
      "function f() { try { if (x) {throw 1} } catch(e) {f()} throw 1; }");
    foldSame(
      "function f() { try { if (x) {throw 1} } catch(e) {throw 1}}");
    fold(
      "function f() { try { if (x) {throw 1} } catch(e) {throw 1} throw 1; }",
      "function f() { try { if (x) {throw 1} } catch(e) {} throw 1; }");

    
    foldSame(
      "function f() { try { if (x) { throw a } } finally { a++ } throw a; }");
    
    
    fold("function f() { try { if (x) { throw 1 } } finally {} throw 1; }",
         "function f() { try { if (x) {} } finally {} throw 1; }");

    fold("function f() { switch(a){ case 1: throw a; } throw a; }",
         "function f() { switch(a){ case 1: } throw a; }");

    fold("function f() { switch(a){ " +
             "case 1: throw a; case 2: throw a; } throw a; }",
         "function f() { switch(a){ case 1: break; case 2: } throw a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testNestedIfCombine
  public void testNestedIfCombine() {
    fold("if(x)if(y){while(1){}}", "if(x&&y){while(1){}}");
    fold("if(x||z)if(y){while(1){}}", "if((x||z)&&y){while(1){}}");
    fold("if(x)if(y||z){while(1){}}", "if((x)&&(y||z)){while(1){}}");
    foldSame("if(x||z)if(y||z){while(1){}}");
    fold("if(x)if(y){if(z){while(1){}}}", "if(x&&y&&z){while(1){}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldTrueFalse
  public void testFoldTrueFalse() {
    fold("x = true", "x = !0");
    fold("x = false", "x = !1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testIssue291
  public void testIssue291() {
    fold("if (true) { f.onchange(); }", "if (1) f.onchange();");
    foldSame("if (f) { f.onchange(); }");
    foldSame("if (f) { f.bar(); } else { f.onchange(); }");
    fold("if (f) { f.bonchange(); }", "f && f.bonchange();");
    foldSame("if (f) { f['x'](); }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testUndefined
  public void testUndefined() {
    foldSame("var x = undefined");
    foldSame("function f(f) {var undefined=2;var x = undefined;}");
    this.enableNormalize();
    fold("var x = undefined", "var x=void 0");
    foldSame(
        "var undefined = 1;" +
        "function f() {var undefined=2;var x = undefined;}");
    foldSame("function f(undefined) {}");
    foldSame("try {} catch(undefined) {}");
    foldSame("for (undefined in {}) {}");
    foldSame("undefined++;");
    fold("undefined += undefined;", "undefined += void 0;");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSplitCommaExpressions
  public void testSplitCommaExpressions() {
    
    foldSame("while (foo(), !0) boo()");
    foldSame("var a = (foo(), !0);");
    foldSame("a = (foo(), !0);");

    
    foldSame("a:a(),b()");

    fold("(x=2), foo()", "x=2; foo()");
    fold("foo(), boo();", "foo(); boo()");
    fold("(a(), b()), (c(), d());", "a(); b(); c(); d();");
    fold("foo(), true", "foo();1");
    fold("function x(){foo(), !0}", "function x(){foo(); 1}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma1
  public void testComma1() {
    fold("1, 2", "1; 1");
    late = false;
    foldSame("1, 2");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma2
  public void testComma2() {
    test("1, a()", "1; a()");
    late = false;
    foldSame("1, a()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma3
  public void testComma3() {
    test("1, a(), b()", "1; a(); b()");
    late = false;
    foldSame("1, a(), b()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma4
  public void testComma4() {
    test("a(), b()", "a();b()");
    late = false;
    foldSame("a(), b()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma5
  public void testComma5() {
    test("a(), b(), 1", "a();b();1");
    late = false;
    foldSame("a(), b(), 1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testObjectLiteral
  public void testObjectLiteral() {
    test("({})", "1");
    test("({a:1})", "1");
    testSame("({a:foo()})");
    testSame("({'a':foo()})");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testArrayLiteral
  public void testArrayLiteral() {
    test("([])", "1");
    test("([1])", "1");
    test("([a])", "1");
    testSame("([foo()])");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testStringArraySplitting
  public void testStringArraySplitting() {
    testSame("var x=['1','2','3','4']");
    testSame("var x=['1','2','3','4','5']");
    test("var x=['1','2','3','4','5','6']",
         "var x='1,2,3,4,5,6'.split(',')");
    test("var x=['1','2','3','4','5','6','7']",
         "var x='1,2,3,4,5,6,7'.split(',')");
    test("var x=[',',',',',',',',',',',']",
         "var x=', , , , , ,'.split(' ')");
    test("var x=[',',' ',',',',',',',',']",
         "var x=',; ;,;,;,;,'.split(';')");
    test("var x=[',',' ',',',',',',',',']",
         "var x=',; ;,;,;,;,'.split(';')");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause
  public void testRemoveElseCause() {
    test("function f() {" +
         " if(x) return 1;" +
         " else if(x) return 2;" +
         " else if(x) return 3 }",
         "function f() {" +
         " if(x) return 1;" +
         "{ if(x) return 2;" +
         "{ if(x) return 3 } } }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause1
  public void testRemoveElseCause1() {
    test("function f() { if (x) throw 1; else f() }",
         "function f() { if (x) throw 1; { f() } }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause2
  public void testRemoveElseCause2() {
    test("function f() { if (x) return 1; else f() }",
         "function f() { if (x) return 1; { f() } }");
    test("function f() { if (x) return; else f() }",
         "function f() { if (x) {} else { f() } }");
    
    testSame("function f() { if (x) return; f() }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause3
  public void testRemoveElseCause3() {
    testSame("function f() { a:{if (x) break a; else f() } }");
    testSame("function f() { if (x) { a:{ break a } } else f() }");
    testSame("function f() { if (x) a:{ break a } else f() }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause4
  public void testRemoveElseCause4() {
    testSame("function f() { if (x) { if (y) { return 1; } } else f() }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall1
  public void testBindToCall1() {
    test("(goog.bind(f))()", "f()");
    test("(goog.bind(f,a))()", "f.call(a)");
    test("(goog.bind(f,a,b))()", "f.call(a,b)");

    test("(goog.bind(f))(a)", "f(a)");
    test("(goog.bind(f,a))(b)", "f.call(a,b)");
    test("(goog.bind(f,a,b))(c)", "f.call(a,b,c)");

    test("(goog.partial(f))()", "f()");
    test("(goog.partial(f,a))()", "f(a)");
    test("(goog.partial(f,a,b))()", "f(a,b)");

    test("(goog.partial(f))(a)", "f(a)");
    test("(goog.partial(f,a))(b)", "f(a,b)");
    test("(goog.partial(f,a,b))(c)", "f(a,b,c)");

    test("((function(){}).bind())()", "((function(){}))()");
    test("((function(){}).bind(a))()", "((function(){})).call(a)");
    test("((function(){}).bind(a,b))()", "((function(){})).call(a,b)");

    test("((function(){}).bind())(a)", "((function(){}))(a)");
    test("((function(){}).bind(a))(b)", "((function(){})).call(a,b)");
    test("((function(){}).bind(a,b))(c)", "((function(){})).call(a,b,c)");

    
    testSame("(f.bind())()");
    testSame("(f.bind(a))()");
    testSame("(f.bind())(a)");
    testSame("(f.bind(a))(b)");

    
    testSame("(goog.bind(f)).call(g)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall2
  public void testBindToCall2() {
    test("(goog$bind(f))()", "f()");
    test("(goog$bind(f,a))()", "f.call(a)");
    test("(goog$bind(f,a,b))()", "f.call(a,b)");

    test("(goog$bind(f))(a)", "f(a)");
    test("(goog$bind(f,a))(b)", "f.call(a,b)");
    test("(goog$bind(f,a,b))(c)", "f.call(a,b,c)");

    test("(goog$partial(f))()", "f()");
    test("(goog$partial(f,a))()", "f(a)");
    test("(goog$partial(f,a,b))()", "f(a,b)");

    test("(goog$partial(f))(a)", "f(a)");
    test("(goog$partial(f,a))(b)", "f(a,b)");
    test("(goog$partial(f,a,b))(c)", "f(a,b,c)");

    
    testSame("(goog$bind(f)).call(g)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall3
  public void testBindToCall3() {
    
    
    
    
    
    
    new StringCompareTestCase().testBindToCall3();
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSimpleFunctionCall
  public void testSimpleFunctionCall() {
    test("var a = String(23)", "var a = '' + 23");
    test("var a = String('hello')", "var a = '' + 'hello'");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall3
    public void testBindToCall3() {
      test("(goog.bind(f.m))()", "(0,f.m)()");
      test("(goog.bind(f.m,a))()", "f.m.call(a)");

      test("(goog.bind(f.m))(a)", "(0,f.m)(a)");
      test("(goog.bind(f.m,a))(b)", "f.m.call(a,b)");

      test("(goog.partial(f.m))()", "(0,f.m)()");
      test("(goog.partial(f.m,a))()", "(0,f.m)(a)");

      test("(goog.partial(f.m))(a)", "(0,f.m)(a)");
      test("(goog.partial(f.m,a))(b)", "(0,f.m)(a,b)");

      
      testSame("f.m.bind()()");
      testSame("f.m.bind(a)()");
      testSame("f.m.bind()(a)");
      testSame("f.m.bind(a)(b)");

      
      testSame("goog.bind(f.m).call(g)");
    }

// com.google.javascript.jscomp.PhaseOptimizerTest::testOneRun
  public void testOneRun() {
    addOneTimePass("x");
    assertPasses("x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testTwoRuns
  public void testTwoRuns() {
    addOneTimePass("x");
    optimizer.process(null, null);
    try {
      optimizer.process(null, null);
      fail();
    } catch (IllegalStateException e) {
      assertEquals(
          "One-time passes cannot be run multiple times: x", e.getMessage());
    }
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop1
  public void testLoop1() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 0);
    assertPasses("x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop2
  public void testLoop2() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    assertPasses("x", "x", "x", "x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop3
  public void testLoop3() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    addLoopedPass(loop, "y", 1);
    assertPasses("x", "y", "x", "y", "x", "y", "x", "y");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testNotInfiniteLoop
  public void testNotInfiniteLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", PhaseOptimizer.MAX_LOOPS);
    optimizer.process(null, null);
    assertEquals("There should be no errors.", 0, compiler.getErrorCount());
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testInfiniteLoop
  public void testInfiniteLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", PhaseOptimizer.MAX_LOOPS + 1);
    try {
      optimizer.process(null, null);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(PhaseOptimizer.OPTIMIZE_LOOP_ERROR));
    }
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testCombined
  public void testCombined() {
    addOneTimePass("a");
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    addLoopedPass(loop, "y", 1);
    addOneTimePass("z");
    assertPasses("a", "x", "y", "x", "y", "x", "y", "x", "y", "z");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testSanityCheck
  public void testSanityCheck() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    addOneTimePass("z");
    optimizer.setSanityCheck(
        createPassFactory("sanity", createPass("sanity", 0), false));
    assertPasses("x", "sanity", "x", "sanity", "z", "sanity");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption1
  public void testConsumption1() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 0, true),
            createPassFactory("b", 1, false),
            createPassFactory("c", 2, false),
            createPassFactory("d", 1, false),
            createPassFactory("e", 1, true),
            createPassFactory("f", 0, true)));
    assertPasses("a", "b", "c", "d", "b", "c", "d", "b", "c", "d", "e", "f");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption2
  public void testConsumption2() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, false),
            createPassFactory("b", 1, true),
            createPassFactory("c", 1, false)));
    assertPasses("a", "a", "a", "b", "c", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption3
  public void testConsumption3() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, true),
            createPassFactory("b", 0, false),
            createPassFactory("c", 0, false)));
    assertPasses("a", "b", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testDuplicateLoop
  public void testDuplicateLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    try {
      addLoopedPass(loop, "x", 1);
      fail("Expected exception");
    } catch (IllegalArgumentException e) {}
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testPassOrdering
  public void testPassOrdering() {
    Loop loop = optimizer.addFixedPointLoop();
    List<String> optimalOrder = Lists.newArrayList(
        PhaseOptimizer.OPTIMAL_ORDER);
    Random random = new Random();
    while (optimalOrder.size() > 0) {
      addLoopedPass(
          loop, optimalOrder.remove(random.nextInt(optimalOrder.size())), 0);
    }
    optimizer.process(null, null);
    assertEquals(PhaseOptimizer.OPTIMAL_ORDER, passesRun);
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
