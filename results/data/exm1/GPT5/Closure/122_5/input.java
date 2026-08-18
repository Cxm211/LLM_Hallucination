// buggy code
  private void handleBlockComment(Comment comment) {
    if (comment.getValue().indexOf("/* @") != -1 || comment.getValue().indexOf("\n * @") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }

// relevant test
// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testWithDevirtualization
  public void testWithDevirtualization() {
    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.c = 1;" +
        "x.prototype.d = 1;" +
        "x.prototype.e = 1;" +
        "x.prototype.f = 1;" +
        "x.prototype.g = 1;",

        loadPrototype("x") +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        "function devirtualize1() { }" +
        TMP + ".c = 1;" +
        TMP + ".d = 1;" +
        TMP + ".e = 1;" +
        TMP + ".f = 1;" +
        TMP + ".g = 1;");

    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.c = 1;" +
        "x.prototype.d = 1;" +
        "function devirtualize2() { }" +
        "x.prototype.e = 1;" +
        "x.prototype.f = 1;" +
        "function devirtualize3() { }" +
        "x.prototype.g = 1;",

        loadPrototype("x") +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        "function devirtualize1() { }" +
        TMP + ".c = 1;" +
        TMP + ".d = 1;" +
        "function devirtualize2() { }" +
        TMP + ".e = 1;" +
        TMP + ".f = 1;" +
        "function devirtualize3() { }" +
        TMP + ".g = 1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testAnonSimple
  public void testAnonSimple() {
    pattern = Pattern.USE_ANON_FUNCTION;

    extract(
        generatePrototypeDeclarations("x", 3),
        generateExtractedDeclarations(3) +
        loadPrototype("x"));

    testSame(generatePrototypeDeclarations("x", 1));
    testSame(generatePrototypeDeclarations("x", 2));

    extract(
        generatePrototypeDeclarations("x", 7),
        generateExtractedDeclarations(7) +
        loadPrototype("x"));

  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testAnonWithDevirtualization
  public void testAnonWithDevirtualization() {
    pattern = Pattern.USE_ANON_FUNCTION;

    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize() { }" +
        "x.prototype.c = 1;",

        "(function(" + TMP + "){" +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        TMP + ".c = 1;" +
        loadPrototype("x") +
        "function devirtualize() { }");

    extract(
        "x.prototype.a = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.b = 1;" +
        "function devirtualize2() { }" +
        "x.prototype.c = 1;" +
        "function devirtualize3() { }",

        "(function(" + TMP + "){" +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        TMP + ".c = 1;" +
        loadPrototype("x") +
        "function devirtualize1() { }" +
        "function devirtualize2() { }" +
        "function devirtualize3() { }");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testAnonWithSideFx
  public void testAnonWithSideFx() {
    pattern = Pattern.USE_ANON_FUNCTION;
    testSame(
        "function foo() {};" +
        "foo.prototype.a1 = 1;" +
        "bar();;" +
        "foo.prototype.a2 = 2;" +
        "bar();;" +
        "foo.prototype.a3 = 3;" +
        "bar();;" +
        "foo.prototype.a4 = 4;" +
        "bar();;" +
        "foo.prototype.a5 = 5;" +
        "bar();;" +
        "foo.prototype.a6 = 6;" +
        "bar();;" +
        "foo.prototype.a7 = 7;" +
        "bar();");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleAssign
  public void testSimpleAssign() {
    inline("var x; x = 1; print(x)", "var x; print(1)");
    inline("var x; x = 1; x", "var x; 1");
    inline("var x; x = 1; var a = x", "var x; var a = 1");
    inline("var x; x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleVar
  public void testSimpleVar() {
    inline("var x = 1; print(x)", "var x; print(1)");
    inline("var x = 1; x", "var x; 1");
    inline("var x = 1; var a = x", "var x; var a = 1");
    inline("var x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleForIn
  public void testSimpleForIn() {
    inline("var a,b,x = a in b; x",
           "var a,b,x; a in b");
    noInline("var a, b; var x = a in b; print(1); x");
    noInline("var a,b,x = a in b; delete a[b]; x");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testExported
  public void testExported() {
    noInline("var _x = 1; print(_x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    noInline("var x = 1; x++;");
    noInline("var x = 1; x--;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineAssignmentOp
  public void testDoNotInlineAssignmentOp() {
    noInline("var x = 1; x += 1;");
    noInline("var x = 1; x -= 1;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    noInline("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUse
  public void testMultiUse() {
    noInline("var x; x = 1; print(x); print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInSameCfgNode
  public void testMultiUseInSameCfgNode() {
    noInline("var x; x = 1; print(x) || print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInTwoDifferentPath
  public void testMultiUseInTwoDifferentPath() {
    noInline("var x = 1; if (print) { print(x) } else { alert(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testAssignmentBeforeDefinition
  public void testAssignmentBeforeDefinition() {
    inline("x = 1; var x = 0; print(x)","x = 1; var x; print(0)" );
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testVarInConditionPath
  public void testVarInConditionPath() {
    noInline("if (foo) { var x = 0 } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsBeforeUse
  public void testMultiDefinitionsBeforeUse() {
    inline("var x = 0; x = 1; print(x)", "var x = 0; print(1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsInSameCfgNode
  public void testMultiDefinitionsInSameCfgNode() {
    noInline("var x; (x = 1) || (x = 2); print(x)");
    noInline("var x; x = (1 || (x = 2)); print(x)");
    noInline("var x;(x = 1) && (x = 2); print(x)");
    noInline("var x;x = (1 && (x = 2)); print(x)");
    noInline("var x; x = 1 , x = 2; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotReachingDefinitions
  public void testNotReachingDefinitions() {
    noInline("var x; if (foo) { x = 0 } print (x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLoopCarriedDefinition
  public void testNoInlineLoopCarriedDefinition() {
    
    noInline("var x; while(true) { print(x); x = 1; }");

    
    noInline("var x = 0; while(true) { print(x); x = 1; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    noInline("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineWithinLoop
  public void testDoNotInlineWithinLoop() {
    noInline("var y = noSFX(); do { var z = y.foo(); } while (true);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression1
  public void testDoNotInlineCatchExpression1() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression1a
  public void testDoNotInlineCatchExpression1a() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err + 1;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression2
  public void testDoNotInlineCatchExpression2() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  if (x) {throw Error(\"\");}\n" +
        "}catch(err) {" +
        "   a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression3
  public void testDoNotInlineCatchExpression3() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "} catch(err) {" +
        "  err = x;\n" +
        "  a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression4
  public void testDoNotInlineCatchExpression4() {
    
    noInline(
        "try {\n" +
        " stuff();\n" +
        "} catch (e) {\n" +
        " x = e;\n" +
        " print(x);\n" +
        "}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDefinitionAfterUse
  public void testDefinitionAfterUse() {
    inline("var x = 0; print(x); x = 1", "var x; print(0); x = 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineSameVariableInStraightLine
  public void testInlineSameVariableInStraightLine() {
    inline("var x; x = 1; print(x); x = 2; print(x)",
        "var x; print(1); print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineInDifferentPaths
  public void testInlineInDifferentPaths() {
    inline("var x; if (print) {x = 1; print(x)} else {x = 2; print(x)}",
        "var x; if (print) {print(1)} else {print(2)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineInMergedPath
  public void testNoInlineInMergedPath() {
    noInline(
        "var x,y;x = 1;while(y) { if(y){ print(x) } else { x = 1 } } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIntoExpressions
  public void testInlineIntoExpressions() {
    inline("var x = 1; print(x + 1);", "var x; print(1 + 1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions1
  public void testInlineExpressions1() {
    inline("var a, b; var x = a+b; print(x)", "var a, b; var x; print(a+b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions2
  public void testInlineExpressions2() {
    
    noInline("var a, b; var x = a + b; a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions3
  public void testInlineExpressions3() {
    inline("var a,b,x; x=a+b; x=a-b ; print(x)",
           "var a,b,x; x=a+b; print(a-b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions4
  public void testInlineExpressions4() {
    
    noInline("var a,b,x; x=a+b, x=a-b; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions5
  public void testInlineExpressions5() {
    noInline("var a; var x = a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions6
  public void testInlineExpressions6() {
    noInline("var a, x; a = 1 + (x = 1); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression7
  public void testInlineExpression7() {
    
    noInline("var x = foo() + 1; bar(); print(x)");

    
    
    
    noInline("var x = foo() + 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression8
  public void testInlineExpression8() {
    
    inline(
        "var a,b;" +
        "var x = a + b; print(x);      x = a - b; print(x)",
        "var a,b;" +
        "var x;         print(a + b);             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression9
  public void testInlineExpression9() {
    
    inline(
        "var a,b;" +
        "var x; if (g) { x= a + b; print(x)    }  x = a - b; print(x)",
        "var a,b;" +
        "var x; if (g) {           print(a + b)}             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression10
  public void testInlineExpression10() {
    
    noInline("var x, y; x = ((y = 1), print(y))");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions11
  public void testInlineExpressions11() {
    inline("var x; x = x + 1; print(x)", "var x; print(x + 1)");
    noInline("var x; x = x + 1; print(x); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions12
  public void testInlineExpressions12() {
    
    
    noInline("var x = 10; x = c++; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions13
  public void testInlineExpressions13() {
    inline("var a = 1, b = 2;" +
           "var x = a;" +
           "var y = b;" +
           "var z = x + y;" +
           "var i = z;" +
           "var j = z + y;" +
           "var k = i;",

           "var a, b;" +
           "var x;" +
           "var y = 2;" +
           "var z = 1 + y;" +
           "var i;" +
           "var j = z + y;" +
           "var k = z;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineIfDefinitionMayNotReach
  public void testNoInlineIfDefinitionMayNotReach() {
    noInline("var x; if (x=1) {} x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineEscapedToInnerFunction
  public void testNoInlineEscapedToInnerFunction() {
    noInline("var x = 1; function foo() { x = 2 }; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLValue
  public void testNoInlineLValue() {
    noInline("var x; if (x = 1) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSwitchCase
  public void testSwitchCase() {
    inline("var x = 1; switch(x) { }", "var x; switch(1) { }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testShadowedVariableInnerFunction
  public void testShadowedVariableInnerFunction() {
    inline("var x = 1; print(x) || (function() {  var x; x = 1; print(x)})()",
        "var x; print(1) || (function() {  var x; print(1)})()");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCatch
  public void testCatch() {
    noInline("var x = 0; try { } catch (x) { }");
    noInline("try { } catch (x) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp
  public void testNoInlineGetProp() {
    
    noInline("var x = a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp2
  public void testNoInlineGetProp2() {
    noInline("var x = 1 * a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp3
  public void testNoInlineGetProp3() {
    
    inline("var x = function(){1 * a.b.c}; print(x);",
           "var x; print(function(){1 * a.b.c});");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetEle
  public void testNoInlineGetEle() {
    
    noInline("var x = a[i]; a[j] = 2; print(x); ");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineConstructors
  public void testNoInlineConstructors() {
    noInline("var x = new Iterator(); x.next();");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineArrayLits
  public void testNoInlineArrayLits() {
    noInline("var x = []; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineObjectLits
  public void testNoInlineObjectLits() {
    noInline("var x = {}; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineRegExpLits
  public void testNoInlineRegExpLits() {
    noInline("var x = /y/; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineConstructorCallsIntoLoop
  public void testInlineConstructorCallsIntoLoop() {
    
    noInline("var x = new Iterator();" +
             "for(i = 0; i < 10; i++) {j = x.next()}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testRemoveWithLabels
  public void testRemoveWithLabels() {
    inline("var x = 1; L: x = 2; print(x)", "var x = 1; L:{} print(2)");
    inline("var x = 1; L: M: x = 2; print(x)", "var x = 1; L:M:{} print(2)");
    inline("var x = 1; L: M: N: x = 2; print(x)",
           "var x = 1; L:M:N:{} print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect1
  public void testInlineAcrossSideEffect1() {
    
    
    
    
    noInline("var y; var x = noSFX(y); print(x)");
    
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect2
  public void testInlineAcrossSideEffect2() {
    
    
    

    
    noInline("var y; var x = noSFX(y), z = hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y), z = new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y), z = new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect3
  public void testInlineAcrossSideEffect3() {
    
    noInline("var y; var x = noSFX(y); hasSFX(y), print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y), print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y), print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect4
  public void testInlineAcrossSideEffect4() {
    
    
    noInline("var y; var x = noSFX(y); hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCanInlineAcrossNoSideEffect
  public void testCanInlineAcrossNoSideEffect() {
    
    
    
    noInline(
        "var y; var x = noSFX(y), z = noSFX(); noSFX(); noSFX(), print(x)");
    
    
    
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDependOnOuterScopeVariables
  public void testDependOnOuterScopeVariables() {
    noInline("var x; function foo() { var y = x; x = 0; print(y) }");
    noInline("var x; function foo() { var y = x; x++; print(y) }");

    
    
    
    noInline("var x; function foo() { var y = x; print(y) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIfNameIsLeftSideOfAssign
  public void testInlineIfNameIsLeftSideOfAssign() {
    inline("var x = 1; x = print(x) + 1", "var x; x = print(1) + 1");
    inline("var x = 1; L: x = x + 2", "var x; L: x = 1 + 2");
    inline("var x = 1; x = (x = x + 1)", "var x; x = (x = 1 + 1)");

    noInline("var x = 1; x = (x = (x = 10) + x)");
    noInline("var x = 1; x = (f(x) + (x = 10) + x);");
    noInline("var x = 1; x=-1,foo(x)");
    noInline("var x = 1; x-=1,foo(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineArguments
  public void testInlineArguments() {
    testSame("function _func(x) { print(x) }");
    testSame("function _func(x,y) { if(y) { x = 1 }; print(x) }");

    test("function f(x, y) { x = 1; print(x) }",
         "function f(x, y) { print(1) }");

    test("function f(x, y) { if (y) { x = 1; print(x) }}",
         "function f(x, y) { if (y) { print(1) }}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments1
  public void testInvalidInlineArguments1() {
    testSame("function f(x, y) { x = 1; arguments[0] = 2; print(x) }");
    testSame("function f(x, y) { x = 1; var z = arguments;" +
        "z[0] = 2; z[1] = 3; print(x)}");
    testSame("function g(a){a[0]=2} function f(x){x=1;g(arguments);print(x)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments2
  public void testInvalidInlineArguments2() {
    testSame("function f(c) {var f = c; arguments[0] = this;" +
             "f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testForIn
  public void testForIn() {
    noInline("var x; var y = {}; for(x in y){}");
    noInline("var x; var y = {}; var z; for(x in z = y){print(z)}");
    noInline("var x; var y = {}; var z; for(x in y){print(z)}");

  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotOkToSkipCheckPathBetweenNodes
  public void testNotOkToSkipCheckPathBetweenNodes() {
    noInline("var x; for(x = 1; foo(x);) {}");
    noInline("var x; for(; x = 1;foo(x)) {}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue698
  public void testIssue698() {
    
    
    
    inline(
        "var x = ''; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;",
        "var x; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = '' + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue777
  public void testIssue777() {
    test(
        "function f(cmd, ta) {" +
        "  var temp = cmd;" +
        "  var temp2 = temp >> 2;" +
        "  cmd = STACKTOP;" +
        "  for (var src = temp2, dest = cmd >> 2, stop = src + 37;" +
        "       src < stop;" +
        "       src++, dest++) {" +
        "    HEAP32[dest] = HEAP32[src];" +
        "  }" +
        "  temp = ta;" +
        "  temp2 = temp >> 2;" +
        "  ta = STACKTOP;" +
        "  STACKTOP += 8;" +
        "  HEAP32[ta >> 2] = HEAP32[temp2];" +
        "  HEAP32[ta + 4 >> 2] = HEAP32[temp2 + 1];" +
        "}",
        "function f(cmd, ta){" +
        "  var temp;" +
        "  var temp2 = cmd >> 2;" +
        "  cmd = STACKTOP;" +
        "  var src = temp2;" +
        "  var dest = cmd >> 2;" +
        "  var stop = src + 37;" +
        "  for(;src<stop;src++,dest++)HEAP32[dest]=HEAP32[src];" +
        "  temp2 = ta >> 2;" +
        "  ta = STACKTOP;" +
        "  STACKTOP += 8;" +
        "  HEAP32[ta>>2] = HEAP32[temp2];" +
        "  HEAP32[ta+4>>2] = HEAP32[temp2+1];" +
        "}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testTransitiveDependencies1
  public void testTransitiveDependencies1() {
    test(
        "function f(x) { var a = x; var b = a; x = 3; return b; }",
        "function f(x) { var a;     var b = x; x = 3; return b; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testTransitiveDependencies2
  public void testTransitiveDependencies2() {
    test(
        "function f(x) { var a = x; var b = a; var c = b; x = 3; return c; }",
        "function f(x) { var a    ; var b = x; var c    ; x = 3; return b; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue794a
  public void testIssue794a() {
    noInline(
        "var x = 1; " +
        "try { x += someFunction(); } catch (e) {}" +
        "x += 1;" +
        "try { x += someFunction(); } catch (e) {}" +
        "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue794b
  public void testIssue794b() {
    noInline(
        "var x = 1; " +
        "try { x = x + someFunction(); } catch (e) {}" +
        "x = x + 1;" +
        "try { x = x + someFunction(); } catch (e) {}" +
        "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testVarAssinInsideHookIssue965
  public void testVarAssinInsideHookIssue965() {
    noInline("var i = 0; return 1 ? (i = 5) : 0, i;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) ? i : 0;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) || i;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) * i;");
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters1
  public void testFindModifiedParameters1() {
    assertEquals(Sets.newHashSet(),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a){ return a==0; }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters2
  public void testFindModifiedParameters2() {
    assertEquals(Sets.newHashSet(),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a){ b=a }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters3
  public void testFindModifiedParameters3() {
    assertEquals(Sets.newHashSet("a"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a){ a=0 }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters4
  public void testFindModifiedParameters4() {
    assertEquals(Sets.newHashSet("a", "b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a,b){ a=0;b=0 }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters5
  public void testFindModifiedParameters5() {
    assertEquals(Sets.newHashSet("b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a,b){ a; if (a) b=0 }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters6
  public void testFindModifiedParameters6() {
    assertEquals(Sets.newHashSet("a", "b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a,b){ function f(){ a;b; } }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters7
  public void testFindModifiedParameters7() {
    assertEquals(Sets.newHashSet("b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a,b){ a; function f(){ b; } }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters8
  public void testFindModifiedParameters8() {
    assertEquals(Sets.newHashSet("b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction(
                "function f(a,b){ "+
                "a; function f(){ function g() { b; } } }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters9
  public void testFindModifiedParameters9() {
    assertEquals(Sets.newHashSet("a", "b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a,b){ (function(){ a;b; }) }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters10
  public void testFindModifiedParameters10() {
    assertEquals(Sets.newHashSet("b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function f(a,b){ a; (function (){ b; }) }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters11
  public void testFindModifiedParameters11() {
    assertEquals(Sets.newHashSet("b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction(
                "function f(a,b){ "+
                "a; (function(){ (function () { b; }) }) }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments1
  public void testMaybeAddTempsForCallArguments1() {
    
    
    testNeededTemps(
        "function foo(a,b){}; foo(goo(),goo());",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments2
  public void testMaybeAddTempsForCallArguments2() {
    
    
    testNeededTemps(
        "function foo(a,b){}; foo(1,2);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments3
  public void testMaybeAddTempsForCallArguments3() {
    
    
    testNeededTemps(
        "function foo(a,b){a;b;}; foo(x,y);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments4
  public void testMaybeAddTempsForCallArguments4() {
    
    
    testNeededTemps(
        "function foo(a,b){a;goo();b;}; foo(x,y);",
        "foo",
        Sets.newHashSet("b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments5
  public void testMaybeAddTempsForCallArguments5() {
    
    
    testNeededTemps(
        "function foo(a,b){x = b; y = a;}; foo(x,y);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments6
  public void testMaybeAddTempsForCallArguments6() {
    
    
    testNeededTemps(
        "function foo(a){x++;a;}; foo(x);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments7
  public void testMaybeAddTempsForCallArguments7() {
    
    testNeededTemps(
        "function foo(a){var c; c=0; a;}; foo(x);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments8
  public void testMaybeAddTempsForCallArguments8() {
    
    testNeededTemps(
        "function foo(a){var c = {}; c.goo=0; a;}; foo(x);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments9
  public void testMaybeAddTempsForCallArguments9() {
    
    
    testNeededTemps(
        "function foo(a,b){while(true){a;goo();b;}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments10
  public void testMaybeAddTempsForCallArguments10() {
    
    testNeededTemps(
        "function foo(a,b){while(true){a;true;b;}}; foo(x,y);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments11
  public void testMaybeAddTempsForCallArguments11() {
    
    
    testNeededTemps(
        "function foo(a,b){do{a;b;}while(goo());}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments12
  public void testMaybeAddTempsForCallArguments12() {
    
    
    testNeededTemps(
        "function foo(a,b){for(;;){a;b;goo();}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments13
  public void testMaybeAddTempsForCallArguments13() {
    
    
    testNeededTemps(
        "function foo(a,b){for(;;){for(;;){a;b;}goo();}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments14
  public void testMaybeAddTempsForCallArguments14() {
    
    
    testNeededTemps(
        "function foo(a,b){goo();for(;;){a;b;}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments20
  public void testMaybeAddTempsForCallArguments20() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(\"blah blah\");",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments21
  public void testMaybeAddTempsForCallArguments21() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(\"\");",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments22
  public void testMaybeAddTempsForCallArguments22() {
    
    testNeededTemps(
        "function foo(a){}; foo({x:1});",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo({x:1});",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo({x:1});",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments23
  public void testMaybeAddTempsForCallArguments23() {
    
    testNeededTemps(
        "function foo(a){}; foo([1,2]);",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo([1,2]);",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo([1,2]);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments24
  public void testMaybeAddTempsForCallArguments24() {
    
    testNeededTemps(
        "function foo(a){}; foo(/mac/);",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo(/mac/);",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(/mac/);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments25
  public void testMaybeAddTempsForCallArguments25() {
    
    testNeededTemps(
        "function foo(a){}; foo(new Date());",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo(new Date());",
        "foo",
        Sets.newHashSet("a"));
    
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(new Date());",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments26
  public void testMaybeAddTempsForCallArguments26() {
    
    testNeededTemps(
        "function foo(a){}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments27
  public void testMaybeAddTempsForCallArguments27() {
    
    
    testNeededTemps(
        "function foo(a,b,c){}; foo.call(this,1,goo(),2);",
        "foo",
        Sets.newHashSet("b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments28
  public void testMaybeAddTempsForCallArguments28() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(true);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments29
  public void testMaybeAddTempsForCallArguments29() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(false);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments30
  public void testMaybeAddTempsForCallArguments30() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(!0);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments31
  public void testMaybeAddTempsForCallArguments31() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(!1);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments32
  public void testMaybeAddTempsForCallArguments32() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(void 0);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArgumentsInLoops
  public void testMaybeAddTempsForCallArgumentsInLoops() {
    
    
    testNeededTemps(
        "function foo(a){for(;;)a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));

    testNeededTemps(
        "function foo(a){while(true)a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));

    testNeededTemps(
        "function foo(a){do{a;}while(true)}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction1
  public void testIsSimpleFunction1() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction2
  public void testIsSimpleFunction2() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction3
  public void testIsSimpleFunction3() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction4
  public void testIsSimpleFunction4() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction5
  public void testIsSimpleFunction5() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0; return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction6
  public void testIsSimpleFunction6() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){var x=true;return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction7
  public void testIsSimpleFunction7() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){if (x) return 0; else return 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction1
  public void testCanInlineReferenceToFunction1() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction2
  public void testCanInlineReferenceToFunction2() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction3
  public void testCanInlineReferenceToFunction3() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction4
  public void testCanInlineReferenceToFunction4() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction5
  public void testCanInlineReferenceToFunction5() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction6
  public void testCanInlineReferenceToFunction6() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction7
  public void testCanInlineReferenceToFunction7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction8
  public void testCanInlineReferenceToFunction8() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction9
  public void testCanInlineReferenceToFunction9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction10
  public void testCanInlineReferenceToFunction10() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction11
  public void testCanInlineReferenceToFunction11() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=x+foo();", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12
  public void testCanInlineReferenceToFunction12() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; var x; x=x+foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12b
  public void testCanInlineReferenceToFunction12b() {
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(){return true;}; var x; x=x+foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction14
  public void testCanInlineReferenceToFunction14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction15
  public void testCanInlineReferenceToFunction15() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction16
  public void testCanInlineReferenceToFunction16() {
    
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){var b;return a;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction17
  public void testCanInlineReferenceToFunction17() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction18
  public void testCanInlineReferenceToFunction18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a;} foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction19
  public void testCanInlineReferenceToFunction19() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo([]);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction20
  public void testCanInlineReferenceToFunction20() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo({});", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction21
  public void testCanInlineReferenceToFunction21() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(new Date);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction22
  public void testCanInlineReferenceToFunction22() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(true && new Date);", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction23
  public void testCanInlineReferenceToFunction23() {
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction24
  public void testCanInlineReferenceToFunction24() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction25
  public void testCanInlineReferenceToFunction25() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction26
  public void testCanInlineReferenceToFunction26() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction27
  public void testCanInlineReferenceToFunction27() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a+a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction28
  public void testCanInlineReferenceToFunction28() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction29
  public void testCanInlineReferenceToFunction29() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction30
  public void testCanInlineReferenceToFunction30() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction31
  public void testCanInlineReferenceToFunction31() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a) {return true;}; " +
        "function x() {foo.call(this, 1);}",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction32
  public void testCanInlineReferenceToFunction32() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, [1]); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction33
  public void testCanInlineReferenceToFunction33() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction34
  public void testCanInlineReferenceToFunction34() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction35
  public void testCanInlineReferenceToFunction35() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction36
  public void testCanInlineReferenceToFunction36() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction37
  public void testCanInlineReferenceToFunction37() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction38
  public void testCanInlineReferenceToFunction38() {
    assumeStrictThis = false;

    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;

    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction39
  public void testCanInlineReferenceToFunction39() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction40
  public void testCanInlineReferenceToFunction40() {
    assumeStrictThis = false;
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction41
  public void testCanInlineReferenceToFunction41() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction42
  public void testCanInlineReferenceToFunction42() {
    assumeStrictThis = false;
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction43
  public void testCanInlineReferenceToFunction43() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction44
  public void testCanInlineReferenceToFunction44() {
    assumeStrictThis = false;
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);

    assumeStrictThis = true;
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction45
  public void testCanInlineReferenceToFunction45() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction46
  public void testCanInlineReferenceToFunction46() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction47
  public void testCanInlineReferenceToFunction47() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction48
  public void testCanInlineReferenceToFunction48() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction49
  public void testCanInlineReferenceToFunction49() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction50
  public void testCanInlineReferenceToFunction50() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction51
  public void testCanInlineReferenceToFunction51() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression1
  public void testCanInlineReferenceToFunctionInExpression1() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression2
  public void testCanInlineReferenceToFunctionInExpression2() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression3
  public void testCanInlineReferenceToFunctionInExpression3() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression4
  public void testCanInlineReferenceToFunctionInExpression4() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5
  public void testCanInlineReferenceToFunctionInExpression5() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5a
 public void testCanInlineReferenceToFunctionInExpression5a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression6
  public void testCanInlineReferenceToFunctionInExpression6() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7
  public void testCanInlineReferenceToFunctionInExpression7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7a
  public void testCanInlineReferenceToFunctionInExpression7a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression8
  public void testCanInlineReferenceToFunctionInExpression8() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression9
  public void testCanInlineReferenceToFunctionInExpression9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10
  public void testCanInlineReferenceToFunctionInExpression10() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10a
  public void testCanInlineReferenceToFunctionInExpression10a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression12
  public void testCanInlineReferenceToFunctionInExpression12() {
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression13
  public void testCanInlineReferenceToFunctionInExpression13() {
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = 1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14
  public void testCanInlineReferenceToFunctionInExpression14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14a
  public void testCanInlineReferenceToFunctionInExpression14a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression18
  public void testCanInlineReferenceToFunctionInExpression18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.AFTER_PREPARATION,
        "function foo(){return _g();}; " +
        "function x() {1 + foo()() }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19
  public void testCanInlineReferenceToFunctionInExpression19() {
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19a
  public void testCanInlineReferenceToFunctionInExpression19a() {
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21
  public void testCanInlineReferenceToFunctionInExpression21() {
    
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21a
  public void testCanInlineReferenceToFunctionInExpression21a() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22
  public void testCanInlineReferenceToFunctionInExpression22() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22a
  public void testCanInlineReferenceToFunctionInExpression22a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23
  public void testCanInlineReferenceToFunctionInExpression23() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23a
  public void testCanInlineReferenceToFunctionInExpression23a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInLoop1
  public void testCanInlineReferenceToFunctionInLoop1() {
    helperCanInlineReferenceToFunction(
        CanInlineResult.YES,
        "function foo(){return a;}; " +
        "while(1) { foo(); }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInLoop2
  public void testCanInlineReferenceToFunctionInLoop2() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.NO,
        "function foo(){return function() {};}; " +
        "while(1) { foo(); }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline1
  public void testInline1() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; void 0",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline2
  public void testInline2() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline3
  public void testInline3() {
    helperInlineReferenceToFunction(
        "function foo(){return;}; foo();",
        "function foo(){return;}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline4
  public void testInline4() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline5
  public void testInline5() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; {true;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline6
  public void testInline6() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline7
  public void testInline7() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x;" +
            "{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline8
  public void testInline8() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x; x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline9
  public void testInline9() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x;{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline10
  public void testInline10() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=x+foo();",
        "function foo(){return true;}; var x; x=x+true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline11
  public void testInline11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline12
  public void testInline12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; {true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline13
  public void testInline13() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "function foo(a){return a;}; " +
        "function x() {{var a$$inline_0=x++;" +
            "a$$inline_0}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline14
  public void testInline14() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(x++);",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_0=x++;" +
            " a$$inline_0+" +
            "a$$inline_0;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline15
  public void testInline15() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(new Date());",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_0=new Date();" +
            " a$$inline_0+" +
            "a$$inline_0;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline16
  public void testInline16() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(function(){});",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_0=function(){};" +
            " a$$inline_0+" +
            "a$$inline_0;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline17
  public void testInline17() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(goo());",
        "function foo(a){return true;};" +
            "{var a$$inline_0=goo();true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline18
  public void testInline18() {
    
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
            "function x() { foo(goo()); }",
            "function foo(a){var b;return a;}; " +
            "function x() {{var a$$inline_0=goo();" +
                "var b$$inline_1;a$$inline_0}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19
  public void testInline19() {
    
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() {" +
           "{var a$$inline_0=x;" +
            "x = y;" +
            "y = a$$inline_0;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19b
  public void testInline19b() {
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() {" +
           "{var b$$inline_1=y;" +
            "y = x;" +
            "x = b$$inline_1;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineIntoLoop
  public void testInlineIntoLoop() {
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;return a;}; " +
        "for(;1;){ {" +
            "var b$$inline_1=void 0;1}}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "do{ foo(1); } while(1)",
        "function foo(a){var b;return a;}; " +
        "do{ {" +
            "var b$$inline_1=void 0;1}}while(1)",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){for(var b in c)return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;for(b in c)return a;}; " +
        "for(;1;){ {JSCompiler_inline_label_foo_2:{" +
            "var b$$inline_1=void 0;for(b$$inline_1 in c){" +
              "1;break JSCompiler_inline_label_foo_2" +
            "}}}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction1
  public void testInlineFunctionWithInnerFunction1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "(function() {return true;})",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction2
  public void testInlineFunctionWithInnerFunction2() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "{(function() {return true;})}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction3
  public void testInlineFunctionWithInnerFunction3() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "(function() {var a; return true;});",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction4
  public void testInlineFunctionWithInnerFunction4() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "{(function() {var a$$inline_0; return true;});}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction5
  public void testInlineFunctionWithInnerFunction5() {
    
    helperInlineReferenceToFunction(
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "function foo(){function x(){var a;return true}return x};" +
            "{var x$$inline_0 = function(){" +
            "var a$$inline_1;return true};x$$inline_0}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression1
  public void testInlineReferenceInExpression1() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "if (JSCompiler_inline_result$$0) throw 'test'; }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression2
  public void testInlineReferenceInExpression2() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "return JSCompiler_inline_result$$0; }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression3
  public void testInlineReferenceInExpression3() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "switch(JSCompiler_inline_result$$0) { default:break; } }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression4
  public void testInlineReferenceInExpression4() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression5
  public void testInlineReferenceInExpression5() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)&&1 }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression6
  public void testInlineReferenceInExpression6() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression7
  public void testInlineReferenceInExpression7() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0; " +
        "{JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression8
  public void testInlineReferenceInExpression8() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_inline_result$$0;" +
        "{JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression9
  public void testInlineReferenceInExpression9() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "function foo(a){return true;}; " +
        "function x() { " +
        "var JSCompiler_inline_result$$0;" +
        "{JSCompiler_inline_result$$0=true;}" +
        "var b = 1 + JSCompiler_inline_result$$0 " +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression11
  public void testInlineReferenceInExpression11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {a:foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() {" +
        "  a:{" +
        "    var JSCompiler_inline_result$$0; " +
        "    {JSCompiler_inline_result$$0=true;}" +
        "    JSCompiler_inline_result$$0?0:1 " +
        "  }" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression12
  public void testInlineReferenceInExpression12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}" +
        "function x() { 1?foo(1):1; }",
        "function foo(a){return true}" +
        "function x() {" +
        "  if(1) {" +
        "    {true;}" +
        "  } else {" +
        "    1;" +
        "  }" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression13
  public void testInlineReferenceInExpression13() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { goo() + (1?foo(1):1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_temp_const$$0=goo();" +
            "var JSCompiler_temp$$1;" +
            "if(1) {" +
            "  {JSCompiler_temp$$1=true;} " +
            "} else {" +
            "  JSCompiler_temp$$1=1;" +
            "}" +
            "JSCompiler_temp_const$$0 + JSCompiler_temp$$1" +
            "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression14
  public void testInlineReferenceInExpression14() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "var JSCompiler_inline_result$$1;" +
            "{" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression15
  public void testInlineReferenceInExpression15() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo.call(this,1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "var JSCompiler_inline_result$$1;" +
            "{" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression16
  public void testInlineReferenceInExpression16() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z[bar()] = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$1=z;" +
            "var JSCompiler_temp_const$$0=bar();" +
            "var JSCompiler_inline_result$$2;" +
            "{" +
             "z= {};" +
             "JSCompiler_inline_result$$2 = true;" +
            "}" +
            "JSCompiler_temp_const$$1[JSCompiler_temp_const$$0] = " +
                "JSCompiler_inline_result$$2;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression17
  public void testInlineReferenceInExpression17() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.y.x.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z.y.x;" +
            "var JSCompiler_inline_result$$1;" +
            "{" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineWithinCalls1
  public void testInlineWithinCalls1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x() {1 + foo()() }",
        "function foo(){return _g;}; " +
        "function x() { var JSCompiler_inline_result$$0;" +
        "{JSCompiler_inline_result$$0=_g;}" +
        "1 + JSCompiler_inline_result$$0() }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineAssignmentToConstant
  public void testInlineAssignmentToConstant() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x(){var CONSTANT_RESULT = foo(); }",

        "function foo(){return _g;}; " +
        "function x() {" +
        "  var JSCompiler_inline_result$$0;" +
        "  {JSCompiler_inline_result$$0=_g;}" +
        "  var CONSTANT_RESULT = JSCompiler_inline_result$$0;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testBug1897706
  public void testBug1897706() {
    helperInlineReferenceToFunction(
        "function foo(a){}; foo(x())",
        "function foo(a){}; {var a$$inline_0=x()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){bar()}; foo(x())",
        "function foo(a){bar()}; {var a$$inline_0=x();bar()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a,b){bar()}; foo(x(),y())",
        "function foo(a,b){bar()};" +
        "{var a$$inline_0=x();var b$$inline_1=y();bar()}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionNamesTest::testFunctionsNamesAndIds
  public void testFunctionsNamesAndIds() {
    final String jsSource =
        "goog.widget = function(str) {\n" +
        "  this.member_fn = function() {};\n" +
        "  local_fn = function() {};\n" +
        "  (function(a){})(1);\n" +
        "}\n" +
        "function foo() {\n" +
        "  function bar() {}\n" +
        "}\n" +
        "literal = {f1 : function(){}, f2 : function(){}};\n" +
        "goog.array.map(arr, function named(){});\n" +
        "goog.array.map(arr, function(){});\n" +
        "named_twice = function quax(){};\n" +
        "recliteral = {l1 : {l2 : function(){}}};\n" +
        "namedliteral = {n1 : function litnamed(){}};\n" +
        "namedrecliteral = {n1 : {n2 : function reclitnamed(){}}};\n" +
        "numliteral = {1 : function(){}};\n" +
        "recnumliteral = {1 : {a : function(){}}};\n";

    testSame(jsSource);

    final Map<Integer, String> idNameMap = Maps.newLinkedHashMap();
    int count = 0;
    for (Node f : functionNames.getFunctionNodeList()) {
      int id = functionNames.getFunctionId(f);
      String name = functionNames.getFunctionName(f);
      idNameMap.put(id, name);
      count++;
    }

    assertEquals("Unexpected number of functions", 16, count);

    final Map<Integer, String> expectedMap = Maps.newLinkedHashMap();

    expectedMap.put(0, "goog.widget.member_fn");
    expectedMap.put(1, "goog.widget::local_fn");
    expectedMap.put(2, "goog.widget::<anonymous>");
    expectedMap.put(3, "goog.widget");
    expectedMap.put(4, "foo::bar");
    expectedMap.put(5, "foo");
    expectedMap.put(6, "literal.f1");
    expectedMap.put(7, "literal.f2");
    expectedMap.put(8, "named");
    expectedMap.put(9, "<anonymous>");
    expectedMap.put(10, "quax");
    expectedMap.put(11, "recliteral.l1.l2");
    expectedMap.put(12, "litnamed");
    expectedMap.put(13, "reclitnamed");
    expectedMap.put(14, "numliteral.__2");
    expectedMap.put(15, "recnumliteral.__3.a");
    assertEquals("Function id/name mismatch",
                 expectedMap, idNameMap);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst1
  public void testReplaceReturnConst1() {
    String source = "a.prototype.foo = function() {return \"foobar\"}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(\"foobar\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst2
  public void testReplaceReturnConst2() {
    checkCompilesToSame("a.prototype.foo = function() {return foobar}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst3
  public void testReplaceReturnConst3() {
    String source = "a.prototype.foo = function() {return void 0;}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(void 0)",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter1
  public void testReplaceGetter1() {
    String source = "a.prototype.foo = function() {return this.foo_}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    GET_HELPER,
                    "a.prototype.foo = JSCompiler_get(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter2
  public void testReplaceGetter2() {
    checkCompilesToSame("a.prototype.foo = function() {return}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter1
  public void testReplaceSetter1() {
    String source = "a.prototype.foo = function(v) {this.foo_ = v}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter2
  public void testReplaceSetter2() {
    String source = "a.prototype.foo = function(v, v2) {this.foo_ = v}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter3
  public void testReplaceSetter3() {
    checkCompilesToSame("a.prototype.foo = function() {this.foo_ = v}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter4
  public void testReplaceSetter4() {
    checkCompilesToSame(
        "a.prototype.foo = function(v, v2) {this.foo_ = v2}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction1
  public void testReplaceEmptyFunction1() {
    String source = "a.prototype.foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "a.prototype.foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction2
  public void testReplaceEmptyFunction2() {
    checkCompilesToSame("function foo() {}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction3
  public void testReplaceEmptyFunction3() {
    String source = "var foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "var foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction1
  public void testReplaceIdentityFunction1() {
    String source = "a.prototype.foo = function(a) {return a}";
    checkCompilesToSame(source, 2);
    checkCompilesTo(source,
                    IDENTITY_HELPER,
                    "a.prototype.foo = JSCompiler_identityFn()",
                    3);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction2
  public void testReplaceIdentityFunction2() {
    checkCompilesToSame("a.prototype.foo = function(a) {return a + 1}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testIssue538
  public void testIssue538() {
    checkCompilesToSame(      "\n" +
        "WebInspector.Setting = function() {}\n" +
        "WebInspector.Setting.prototype = {\n" +
        "    get name0(){return this._name;},\n" +
        "    get name1(){return this._name;},\n" +
        "    get name2(){return this._name;},\n" +
        "    get name3(){return this._name;},\n" +
        "    get name4(){return this._name;},\n" +
        "    get name5(){return this._name;},\n" +
        "    get name6(){return this._name;},\n" +
        "    get name7(){return this._name;},\n" +
        "    get name8(){return this._name;},\n" +
        "    get name9(){return this._name;},\n" +
        "}", 1);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithoutResultAssignment
  public void testMutateNoReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){}; foo();",
        "{}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithResultAssignment
  public void testMutateNoReturnWithResultAssignment() {
    helperMutate(
        "function foo(){}; var result = foo();",
        "{result = void 0}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithoutResultAssignment
  public void testMutateNoValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return;}; foo();",
        "{}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithResultAssignment
  public void testMutateNoValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return;}; var result = foo();",
        "{result = void 0}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithoutResultAssignment
  public void testMutateValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return true;}; foo();",
        "{true;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithResultAssignment
  public void testMutateValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return true;}; var x=foo();",
        "{x=true}",
        "foo", "x", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithMultipleReturns
  public void testMutateWithMultipleReturns() {
    helperMutate(
        "function foo(){ if (0) {return 0} else {return 1} };" +
          "var result=foo();",
        "{" +
          "JSCompiler_inline_label_foo_0:{" +
            "if(0) {" +
              "result=0; break JSCompiler_inline_label_foo_0" +
            "} else {" +
              "result=1; break JSCompiler_inline_label_foo_0" +
            "} result=void 0" +
          "}" +
        "}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters1
  public void testMutateWithParameters1() {
    
    helperMutate(
        "function foo(a){return true;}; foo(x);",
        "{true}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters2
  public void testMutateWithParameters2() {
    
    helperMutate(
        "function foo(a){return x;}; foo(x);",
        "{x}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters3
  public void testMutateWithParameters3() {
    
    helperMutate(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "{var a$$inline_0 = x++; a$$inline_0}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutate8
  public void testMutate8() {
    
    helperMutate(
        "function foo(a){return a+a;}; foo(x++);",
        "{var a$$inline_0 = x++;" +
            "a$$inline_0 + a$$inline_0;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars1
  public void testMutateInitializeUninitializedVars1() {
    helperMutate(
        "function foo(a){var b;return a;}; foo(1);",
        "{var b$$inline_1=void 0;1}",
        "foo", null, false, true);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars2
  public void testMutateInitializeUninitializedVars2() {
    helperMutate(
        "function foo(a){for(var b in c)return a;}; foo(1);",
        "{JSCompiler_inline_label_foo_2:" +
          "{" +
            "for(var b$$inline_1 in c){" +
                "1;break JSCompiler_inline_label_foo_2" +
             "}" +
          "}" +
        "}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateCallInLoopVars1
  public void testMutateCallInLoopVars1() {
    
    boolean callInLoop = false;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_1=bar(); 1;}",
        "foo", null, false, callInLoop);
    
    
    callInLoop = true;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_1 = bar(); 1;}",
        "foo", null, false, callInLoop);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateFunctionDefinition
  public void testMutateFunctionDefinition() {
     
     
     helperMutate(
        "function foo(a){function g(){}}; foo(1);",
        "{var g$$inline_1=function(){};}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testValidBuiltInTypeRedefinition
  public void testValidBuiltInTypeRedefinition() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentReturnType
  public void testBuiltInTypeDifferentReturnType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, *=): number\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String): string\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?=, ?=): string\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?=): string\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (new:Function, ?=): ?\n"
        + "expected: function (new:Function, ...[*]): ?");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testInlineJsDoc
  public void testInlineJsDoc() throws Exception {
    testSame(
        " function f( x) { return x; }", "",
        TypeValidator.TYPE_MISMATCH_WARNING,
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testInlineJsDoc2
  public void testInlineJsDoc2() throws Exception {
    testSame(
        " " +
        "function f( x) { return x; }" +
        " var x = f(1);", "",
        TypeValidator.TYPE_MISMATCH_WARNING,
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testExternSubTypes
  public void testExternSubTypes() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);

    List<FunctionType> subtypes = ((ObjectType) getLastCompiler()
        .getTypeRegistry().getType("Error")).getConstructor().getSubTypes();
    for (FunctionType type : subtypes) {
      String typeName = type.getInstanceType().toString();
      FunctionType typeInRegistry = ((ObjectType) getLastCompiler()
          .getTypeRegistry().getType(typeName)).getConstructor();
      assertTrue(typeInRegistry == type);
    }
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGathering
  public void testGathering() {
    testSame("function j() { return j()}");
    generator.restartNaming();
    assertEquals("n", generator.generateNextName());
    assertEquals("r", generator.generateNextName());
    assertEquals("t", generator.generateNextName());
    assertEquals("u", generator.generateNextName());
    assertEquals("c", generator.generateNextName());
    assertEquals("e", generator.generateNextName());
    assertEquals("f", generator.generateNextName());
    assertEquals("i", generator.generateNextName());
    assertEquals("o", generator.generateNextName());
    assertEquals("a", generator.generateNextName());
    assertEquals("b", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGathering2
  public void testGathering2() {
    testSame("if(a){}else{}");
    generator.restartNaming();
    assertEquals("e", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGathering3
  public void testGathering3() {
    testSame("switch(a){default:}");
    generator.restartNaming();
    assertEquals("t", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGathering4
  public void testGathering4() {
    testSame("a instanceof b");
    generator.restartNaming();
    assertEquals("n", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGathering5
  public void testGathering5() {
    testSame("a['zzzz']");
    generator.restartNaming();
    assertEquals("z", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGathering6
  public void testGathering6() {
    testSame("this");
    generator.restartNaming();
    assertEquals("h", generator.generateNextName());
    assertEquals("i", generator.generateNextName());
    assertEquals("s", generator.generateNextName());
    assertEquals("t", generator.generateNextName());
    assertEquals("a", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGatheringGetterSetter
  public void testGatheringGetterSetter() {
    testSame("var x = { get y(){}, set y(val){} }");
    generator.restartNaming();
    assertEquals("e", generator.generateNextName()); 
    assertEquals("t", generator.generateNextName());

    assertEquals("a", generator.generateNextName()); 
    assertEquals("g", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGatheringDebugger
  public void testGatheringDebugger() {
    testSame("debugger;");
    generator.restartNaming();
    assertEquals("e", generator.generateNextName());
    assertEquals("g", generator.generateNextName());
    assertEquals("b", generator.generateNextName());
    assertEquals("d", generator.generateNextName());
    assertEquals("r", generator.generateNextName());
    assertEquals("u", generator.generateNextName());
    assertEquals("a", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGatheringNames1
  public void testGatheringNames1() {
    testSame("var MMMMMM");
    generator.restartNaming();
    assertEquals("a", generator.generateNextName());
  }

// com.google.javascript.jscomp.GatherCharacterEncodingBiasTest::testGatheringNames2
  public void testGatheringNames2() {
    renameGlobalVars = false;
    testSame("var MMMMMM");
    generator.restartNaming();
    assertEquals("M", generator.generateNextName());
    assertEquals("a", generator.generateNextName());
  }
