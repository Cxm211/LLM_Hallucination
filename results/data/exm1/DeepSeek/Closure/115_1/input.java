// buggy code
  private CanInlineResult canInlineReferenceDirectly(
      Node callNode, Node fnNode) {
    if (!isDirectCallNodeReplacementPossible(fnNode)) {
      return CanInlineResult.NO;
    }

    Node block = fnNode.getLastChild();

    boolean hasSideEffects = false;
    if (block.hasChildren()) {
      Preconditions.checkState(block.hasOneChild());
      Node stmt = block.getFirstChild();
      if (stmt.isReturn()) {
        hasSideEffects = NodeUtil.mayHaveSideEffects(stmt.getFirstChild(), compiler);
      }
    }
    // CALL NODE: [ NAME, ARG1, ARG2, ... ]
    Node cArg = callNode.getFirstChild().getNext();

    // Functions called via 'call' and 'apply' have a this-object as
    // the first parameter, but this is not part of the called function's
    // parameter list.
    if (!callNode.getFirstChild().isName()) {
      if (NodeUtil.isFunctionObjectCall(callNode)) {
        // TODO(johnlenz): Support replace this with a value.
        if (cArg == null || !cArg.isThis()) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      } else {
        // ".apply" call should be filtered before this.
        Preconditions.checkState(!NodeUtil.isFunctionObjectApply(callNode));
      }
    }

    // FUNCTION NODE -> LP NODE: [ ARG1, ARG2, ... ]
    Node fnParam = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    while (cArg != null || fnParam != null) {
      // For each named parameter check if a mutable argument use more than one.
      if (fnParam != null) {
        if (cArg != null) {
          if (hasSideEffects && NodeUtil.canBeSideEffected(cArg)) {
            return CanInlineResult.NO;
          }
          // Check for arguments that are evaluated more than once.
          // Note: Unlike block inlining, there it is not possible that a
          // parameter reference will be in a loop.
          if (NodeUtil.mayEffectMutableState(cArg, compiler)
              && NodeUtil.getNameReferenceCount(
                  block, fnParam.getString()) > 1) {
            return CanInlineResult.NO;
          }
        }

        // Move to the next name.
        fnParam = fnParam.getNext();
      }

      // For every call argument check for side-effects, even if there
      // isn't a named parameter to match.
      if (cArg != null) {
        if (NodeUtil.mayHaveSideEffects(cArg, compiler)) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      }
    }

    return CanInlineResult.YES;
  }

// relevant test
// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified2
  public void testNoInlineIfParametersModified2() {
    test("function f(x){return (x)=1;}f(2)",
         "{var x$$inline_0=2;" +
         "x$$inline_0=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified3
  public void testNoInlineIfParametersModified3() {
    
    test("function f(x){return x*=2}f(2)",
         "{var x$$inline_0=2;" +
         "x$$inline_0*=2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified4
  public void testNoInlineIfParametersModified4() {
    
    test("function f(x){return x?(x=2):0}f(2)",
         "{var x$$inline_0=2;" +
         "x$$inline_0?(" +
         "x$$inline_0=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified5
  public void testNoInlineIfParametersModified5() {
    
    test("function f(x,y){return x?(y=2):0}f(2,undefined)",
         "{var y$$inline_1=undefined;2?(" +
         "y$$inline_1=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified6
  public void testNoInlineIfParametersModified6() {
    test("function f(x,y){return x?(y=2):0}f(2)",
         "{var y$$inline_1=void 0;2?(" +
         "y$$inline_1=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified7
  public void testNoInlineIfParametersModified7() {
    
    test("function f(a){return++a<++a}f(1)",
         "{var a$$inline_0=1;" +
         "++a$$inline_0<" +
         "++a$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified8
  public void testNoInlineIfParametersModified8() {
    
    test("function f(a){return a.x=2}f(o)", "o.x=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified9
  public void testNoInlineIfParametersModified9() {
    
    test("function f(a){return a[2]=2}f(o)", "o[2]=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution1
  public void testInlineNeverPartialSubtitution1() {
    test("function f(z){return x.y.z;}f(1)",
         "x.y.z");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution2
  public void testInlineNeverPartialSubtitution2() {
    test("function f(z){return x.y[z];}f(a)",
         "x.y[a]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverMutateConstants
  public void testInlineNeverMutateConstants() {
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_0=undefined;" +
         "x$$inline_0=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverOverrideNewValues
  public void testInlineNeverOverrideNewValues() {
    test("function f(a){return++a<++a}f(1)",
        "{var a$$inline_0=1;" +
        "++a$$inline_0<++a$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineMutableArgsReferencedOnce
  public void testInlineMutableArgsReferencedOnce() {
    test("function foo(x){return x;}foo([])", "[]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs1
  public void testNoInlineMutableArgs1() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo([])");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs2
  public void testNoInlineMutableArgs2() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs3
  public void testNoInlineMutableArgs3() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(true&&new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs4
  public void testNoInlineMutableArgs4() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo({})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs1
  public void testInlineBlockMutableArgs1() {
    test("function foo(x){x+x}foo([])",
         "{var x$$inline_0=[];" +
         "x$$inline_0+x$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs2
  public void testInlineBlockMutableArgs2() {
    test("function foo(x){x+x}foo(new Date)",
         "{var x$$inline_0=new Date;" +
         "x$$inline_0+x$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs3
  public void testInlineBlockMutableArgs3() {
    test("function foo(x){x+x}foo(true&&new Date)",
         "{var x$$inline_0=true&&new Date;" +
         "x$$inline_0+x$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs4
  public void testInlineBlockMutableArgs4() {
    test("function foo(x){x+x}foo({})",
         "{var x$$inline_0={};" +
         "x$$inline_0+x$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables1
  public void testShadowVariables1() {
    
    

    
    
    test("var a=0;" +
         "function foo(a){return 3+a}" +
         "function bar(){var a=foo(4)}" +
         "bar();",

         "var a=0;" +
         "{var a$$inline_0=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables2
  public void testShadowVariables2() {
    
    
    
    test("var a=0;" +
        "function foo(a){return 3+a}" +
        "function bar(){a=foo(4)}" +
        "bar()",

        "var a=0;" +
        "{a=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables3
  public void testShadowVariables3() {
    
    test("var a=0;" +
        "function foo(){var a=2;return 3+a}" +
        "function _bar(){a=foo()}",

        "var a=0;" +
        "function _bar(){{var a$$inline_0=2;" +
        "a=3+a$$inline_0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables4
  public void testShadowVariables4() {
    
    
    test("var a=0;" +
         "function foo(){return 3+a}" +
         "function _bar(a){a=foo(4)+a}",

         "var a=0;function _bar(a$$1){" +
         "a$$1=" +
         "3+a+a$$1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables5
  public void testShadowVariables5() {
    
    
    allowBlockInlining = false;
    testSame("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables6
  public void testShadowVariables6() {
    test("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)}",

        "var a=0;function _bar(a$$2){{" +
        "var a$$inline_0=4;" +
        "a$$2=3+a$$inline_0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables7
  public void testShadowVariables7() {
    assumeMinimumCapture = false;
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_0=5;{a}}"
         );

    assumeMinimumCapture = true;
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables8
  public void testShadowVariables8() {
    
    test("var a=0;" +
         "function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "var a=0;" +
         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables9
  public void testShadowVariables9() {
    
    test("function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables10
  public void testShadowVariables10() {
    
    test("var a;function foo(){return a}" +
         "function _bar(){var a=foo()}",
         "var a;function _bar(){var a$$1=a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables11
  public void testShadowVariables11() {
    
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var a=foo();alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+a;" +
         "alert(a$$1)}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables12
  public void testShadowVariables12() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var a=foo(),b;alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+b," +
         "b$$1;" +
         "alert(a$$1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables13
  public void testShadowVariables13() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables14
  public void testShadowVariables14() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var c=foo(),b;alert(c)}",
         "var a=0;var b=1;" +
         "function _bar(){var c=a+b," +
         "b$$1;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables15
  public void testShadowVariables15() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c+a)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c+a)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables16
  public void testShadowVariables16() {
    assumeMinimumCapture = false;
    
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_0=5;{a}}"
         );

    assumeMinimumCapture = true;
    
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables17
  public void testShadowVariables17() {
    test("var a=0;" +
         "function bar(){return a+a}" +
         "function foo(){return bar()}" +
         "function _goo(){var a=2;var x=foo();}",

         "var a=0;" +
         "function _goo(){var a$$1=2;var x=a+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables18
  public void testShadowVariables18() {
    test("var a=0;" +
        "function bar(){return a+a}" +
        "function foo(){var a=3;return bar()}" +
        "function _goo(){var a=2;var x=foo();}",

        "var a=0;" +
        "function _goo(){var a$$2=2;var x;" +
        "{var a$$inline_0=3;x=a+a}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining1
  public void testCostBasedInlining1() {
    testSame(
        "function foo(a){return a}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining2
  public void testCostBasedInlining2() {
    
    
    test(
        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return foo(1)}",

        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return 1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining3
  public void testCostBasedInlining3() {
    
    test(
        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return 1+2}" +
        "function _t2(){return 2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining4
  public void testCostBasedInlining4() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining5
  public void testCostBasedInlining5() {
    
    test(
        "function foo(a,b){return a+b+a+b}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2}" +
        "function _t2(){return 2+3+2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining6
  public void testCostBasedInlining6() {
    
    
    test(
        "function foo(a,b){return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2+1+2+1+2+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t2(){return 2+3+2+3+2+3+2+3+4+5+6+7+8+9+1+2+3+4+5}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining7
  public void testCostBasedInlining7() {
    
    testSame(
        "function foo(a,b){" +
        "    return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5+6}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining8
  public void testCostBasedInlining8() {
    
    
    
    
    
    
    
    allowBlockInlining = false;
    testSame("function f(a){return 1 + a + a;}" +
        "var a = f(f(1));");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining9
  public void testCostBasedInlining9() {
    
    
    
    test("function f(a){return 1 + a + a;}" +
         "var a = f(f(1));",
         "var a;" +
         "{var a$$inline_0=1+1+1;" +
         "a=1+a$$inline_0+a$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining10
  public void testCostBasedInlining10() {
    
    
    
    allowBlockInlining = false;
    test("function f(a){return a + a;}" +
        "var a = f(f(1));",
        "var a= 1+1+(1+1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining11
  public void testCostBasedInlining11() {
    
    test("function f(a){return a + a;}" +
         "var a = f(f(1))",
         "var a;" +
         "{var a$$inline_0=1+1;" +
         "a=a$$inline_0+a$$inline_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining12
  public void testCostBasedInlining12() {
    test("function f(a){return 1 + a + a;}" +
         "var a = f(1) + f(2);",

         "var a=1+1+1+(1+2+2)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex1
  public void testCostBasedInliningComplex1() {
    testSame(
        "function foo(a){a()}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex2
  public void testCostBasedInliningComplex2() {
    
    
    test(
        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){foo(x)}",

        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){{x()}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex3
  public void testCostBasedInliningComplex3() {
    
    test(
        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){{1+2}}" +
        "function _t2(){{2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex4
  public void testCostBasedInliningComplex4() {
    
    
    testSame(
        "function foo(a,b){a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex5
  public void testCostBasedInliningComplex5() {
    
    test(
        "function foo(a,b){a+b+a+b}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2}}" +
        "function _t2(){{2+3+2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex6
  public void testCostBasedInliningComplex6() {
    
    
    test(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2+1+2+1+2+4+5+6+7+8+9+1}}" +
        "function _t2(){{2+3+2+3+2+3+2+3+4+5+6+7+8+9+1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex7
  public void testCostBasedInliningComplex7() {
    
    testSame(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex8
  public void testCostBasedInliningComplex8() {
    
    testSame("function _f(a){1+a+a}" +
             "a=_f(1)+_f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex9
  public void testCostBasedInliningComplex9() {
    test("function f(a){1 + a + a;}" +
         "f(1);f(2);",
         "{1+1+1}{1+2+2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining1
  public void testDoubleInlining1() {
    allowBlockInlining = false;
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "getWindow(x)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining2
  public void testDoubleInlining2() {
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "{getWindow(x)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction1
  public void testNoInlineOfNonGlobalFunction1() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction2
  public void testNoInlineOfNonGlobalFunction2() {
    test("var g;function _f(){var g=function(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction3
  public void testNoInlineOfNonGlobalFunction3() {
    test("var g;function _f(){var g=function(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction4
  public void testNoInlineOfNonGlobalFunction4() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMaskedFunction
  public void testNoInlineMaskedFunction() {
    
    
    test("var g=function(){return 0};" +
         "function _f(g){return g()}",
         "function _f(g$$1){return g$$1()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineNonFunction
  public void testNoInlineNonFunction() {
    testSame("var g=3;function _f(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineCall
  public void testInlineCall() {
    test("function f(g) { return g.h(); } f('x');",
         "\"x\".h()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch1
  public void testInlineFunctionWithArgsMismatch1() {
    test("function f(g) { return g; } f();",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch2
  public void testInlineFunctionWithArgsMismatch2() {
    test("function f() { return 0; } f(1);",
         "0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch3
  public void testInlineFunctionWithArgsMismatch3() {
    test("function f(one, two, three) { return one + two + three; } f(1);",
         "1+void 0+void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch4
  public void testInlineFunctionWithArgsMismatch4() {
    test("function f(one, two, three) { return one + two + three; }" +
         "f(1,2,3,4,5);",
         "1+2+3");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testArgumentsWithSideEffectsNeverInlined1
  public void testArgumentsWithSideEffectsNeverInlined1() {
    allowBlockInlining = false;
    testSame("function f(){return 0} f(new goo());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testArgumentsWithSideEffectsNeverInlined2
  public void testArgumentsWithSideEffectsNeverInlined2() {
    allowBlockInlining = false;
    testSame("function f(g,h){return h+g}f(g(),h());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testOneSideEffectCallDoesNotRuinOthers
  public void testOneSideEffectCallDoesNotRuinOthers() {
    allowBlockInlining = false;
    test("function f(){return 0}f(new goo());f()",
         "function f(){return 0}f(new goo());0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall1
  public void testComplexInlineNoResultNoParamCall1() {
    test("function f(){a()}f()",
         "{a()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall2
  public void testComplexInlineNoResultNoParamCall2() {
   test("function f(){if (true){return;}else;} f();",
         "{JSCompiler_inline_label_f_0:{" +
             "if(true)break JSCompiler_inline_label_f_0;else;}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall3
  public void testComplexInlineNoResultNoParamCall3() {
    
    
    

    
    test("function f(){a();b();var z=1+1}function _foo(){f()}",
         "function _foo(){{a();b();var z$$inline_0=1+1}}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall1
  public void testComplexInlineNoResultWithParamCall1() {
    test("function f(x){a(x)}f(1)",
         "{a(1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall2
  public void testComplexInlineNoResultWithParamCall2() {
    test("function f(x,y){a(x)}var b=1;f(1,b)",
         "var b=1;{a(1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall3
  public void testComplexInlineNoResultWithParamCall3() {
    test("function f(x,y){if (x) y(); return true;}var b=1;f(1,b)",
         "var b=1;{if(1)b();true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline1
  public void testComplexInline1() {
    test("function f(){if (true){return;}else;} z=f();",
         "{JSCompiler_inline_label_f_0:" +
         "{if(true){z=void 0;" +
         "break JSCompiler_inline_label_f_0}else;z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline2
  public void testComplexInline2() {
    test("function f(){if (true){return;}else return;} z=f();",
         "{JSCompiler_inline_label_f_0:{if(true){z=void 0;" +
         "break JSCompiler_inline_label_f_0}else{z=void 0;" +
         "break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline3
  public void testComplexInline3() {
    test("function f(){if (true){return 1;}else return 0;} z=f();",
         "{JSCompiler_inline_label_f_0:{if(true){z=1;" +
         "break JSCompiler_inline_label_f_0}else{z=0;" +
         "break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline4
  public void testComplexInline4() {
    test("function f(x){a(x)} z = f(1)",
         "{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline5
  public void testComplexInline5() {
    test("function f(x,y){a(x)}var b=1;z=f(1,b)",
         "var b=1;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline6
  public void testComplexInline6() {
    test("function f(x,y){if (x) y(); return true;}var b=1;z=f(1,b)",
         "var b=1;{if(1)b();z=true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline7
  public void testComplexInline7() {
    test("function f(x,y){if (x) return y(); else return true;}" +
         "var b=1;z=f(1,b)",
         "var b=1;{JSCompiler_inline_label_f_2:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_2}else{z=true;" +
         "break JSCompiler_inline_label_f_2}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline8
  public void testComplexInline8() {
    test("function f(x){a(x)}var z=f(1)",
         "var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars1
  public void testComplexInlineVars1() {
    test("function f(){if (true){return;}else;}var z=f();",
         "var z;{JSCompiler_inline_label_f_0:{" +
         "if(true){z=void 0;break JSCompiler_inline_label_f_0}else;z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars2
  public void testComplexInlineVars2() {
    test("function f(){if (true){return;}else return;}var z=f();",
        "var z;{JSCompiler_inline_label_f_0:{" +
        "if(true){z=void 0;break JSCompiler_inline_label_f_0" +
        "}else{" +
        "z=void 0;break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars3
  public void testComplexInlineVars3() {
    test("function f(){if (true){return 1;}else return 0;}var z=f();",
         "var z;{JSCompiler_inline_label_f_0:{if(true){" +
         "z=1;break JSCompiler_inline_label_f_0" +
         "}else{" +
         "z=0;break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars4
  public void testComplexInlineVars4() {
    test("function f(x){a(x)}var z = f(1)",
         "var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars5
  public void testComplexInlineVars5() {
    test("function f(x,y){a(x)}var b=1;var z=f(1,b)",
         "var b=1;var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars6
  public void testComplexInlineVars6() {
    test("function f(x,y){if (x) y(); return true;}var b=1;var z=f(1,b)",
         "var b=1;var z;{if(1)b();z=true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars7
  public void testComplexInlineVars7() {
    test("function f(x,y){if (x) return y(); else return true;}" +
         "var b=1;var z=f(1,b)",
         "var b=1;var z;" +
         "{JSCompiler_inline_label_f_2:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_2" +
         "}else{" +
         "z=true;break JSCompiler_inline_label_f_2}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars8
  public void testComplexInlineVars8() {
    test("function f(x){a(x)}var x;var z=f(1)",
         "var x;var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars9
  public void testComplexInlineVars9() {
    test("function f(x){a(x)}var x;var z=f(1);var y",
         "var x;var z;{a(1);z=void 0}var y");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars10
  public void testComplexInlineVars10() {
    test("function f(x){a(x)}var x=blah();var z=f(1);var y=blah();",
          "var x=blah();var z;{a(1);z=void 0}var y=blah()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars11
  public void testComplexInlineVars11() {
    test("function f(x){a(x)}var x=blah();var z=f(1);var y;",
         "var x=blah();var z;{a(1);z=void 0}var y");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars12
  public void testComplexInlineVars12() {
    test("function f(x){a(x)}var x;var z=f(1);var y=blah();",
         "var x;var z;{a(1);z=void 0}var y=blah()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions1
  public void testComplexInlineInExpresssions1() {
    test("function f(){a()}var z=f()",
         "var z;{a();z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions2
  public void testComplexInlineInExpresssions2() {
    test("function f(){a()}c=z=f()",
         "var JSCompiler_inline_result$$0;" +
         "{a();JSCompiler_inline_result$$0=void 0;}" +
         "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions3
  public void testComplexInlineInExpresssions3() {
    test("function f(){a()}c=z=f()",
        "var JSCompiler_inline_result$$0;" +
        "{a();JSCompiler_inline_result$$0=void 0;}" +
        "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions4
  public void testComplexInlineInExpresssions4() {
    test("function f(){a()}if(z=f());",
        "var JSCompiler_inline_result$$0;" +
        "{a();JSCompiler_inline_result$$0=void 0;}" +
        "if(z=JSCompiler_inline_result$$0);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions5
  public void testComplexInlineInExpresssions5() {
    test("function f(){a()}if(z.y=f());",
         "var JSCompiler_temp_const$$0=z;" +
         "var JSCompiler_inline_result$$1;" +
         "{a();JSCompiler_inline_result$$1=void 0;}" +
         "if(JSCompiler_temp_const$$0.y=JSCompiler_inline_result$$1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoInline1
  public void testComplexNoInline1() {
    testSame("function f(){a()}while(z=f())continue");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoInline2
  public void testComplexNoInline2() {
    testSame("function f(){a()}do;while(z=f())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexSample
  public void testComplexSample() {
    String result = "" +
      "{{" +
      "var styleSheet$$inline_2=null;" +
      "if(goog$userAgent$IE)" +
        "styleSheet$$inline_2=0;" +
      "else " +
        "var head$$inline_3=0;" +
      "{" +
        "var element$$inline_4=" +
            "styleSheet$$inline_2;" +
        "var stylesString$$inline_5=a;" +
        "if(goog$userAgent$IE)" +
          "element$$inline_4.cssText=" +
              "stylesString$$inline_5;" +
        "else " +
        "{" +
          "var propToSet$$inline_6=" +
              "\"innerText\";" +
          "element$$inline_4[" +
              "propToSet$$inline_6]=" +
                  "stylesString$$inline_5" +
        "}" +
      "}" +
      "styleSheet$$inline_2" +
      "}}";

    test("var foo = function(stylesString, opt_element) { " +
        "var styleSheet = null;" +
        "if (goog$userAgent$IE)" +
          "styleSheet = 0;" +
        "else " +
          "var head = 0;" +
        "" +
        "goo$zoo(styleSheet, stylesString);" +
        "return styleSheet;" +
     " };\n " +

     "var goo$zoo = function(element, stylesString) {" +
        "if (goog$userAgent$IE)" +
          "element.cssText = stylesString;" +
        "else {" +
          "var propToSet = 'innerText';" +
          "element[propToSet] = stylesString;" +
        "}" +
      "};" +
      "(function(){foo(a,b);})();",
     result);
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexSampleNoInline
  public void testComplexSampleNoInline() {
    testSame(
      "foo=function(stylesString,opt_element){" +
        "var styleSheet=null;" +
        "if(goog$userAgent$IE)" +
          "styleSheet=0;" +
        "else " +
          "var head=0;" +
        "" +
        "goo$zoo(styleSheet,stylesString);" +
        "return styleSheet" +
     "};" +
     "goo$zoo=function(element,stylesString){" +
        "if(goog$userAgent$IE)" +
          "element.cssText=stylesString;" +
        "else{" +
          "var propToSet=goog$userAgent$WEBKIT?\"innerText\":\"innerHTML\";" +
          "element[propToSet]=stylesString" +
        "}" +
      "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoVarSub
  public void testComplexNoVarSub() {
    test(
        "function foo(x){" +
          "var x;" +
          "y=x" +
        "}" +
        "foo(1)",

        "{y=1}"
        );
   }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition1
  public void testComplexFunctionWithFunctionDefinition1() {
    test("function f(){call(function(){return})}f()",
         "{call(function(){return})}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition2
  public void testComplexFunctionWithFunctionDefinition2() {
    assumeMinimumCapture = false;

    
    testSame("function f(a){call(function(){return})}f()");

    assumeMinimumCapture = true;

    test("(function(){" +
         "var f = function(a){call(function(){return a})};f()})()",
         "{{var a$$inline_0=void 0;call(function(){return a$$inline_0})}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition2a
  public void testComplexFunctionWithFunctionDefinition2a() {
    assumeMinimumCapture = false;

    
    testSame("(function(){" +
        "var f = function(a){call(function(){return a})};f()})()");

    assumeMinimumCapture = true;

    test("(function(){" +
         "var f = function(a){call(function(){return a})};f()})()",
         "{{var a$$inline_0=void 0;call(function(){return a$$inline_0})}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition3
  public void testComplexFunctionWithFunctionDefinition3() {
    assumeMinimumCapture = false;

    
    testSame("function f(){var a; call(function(){return a})}f()");

    assumeMinimumCapture = true;

    test("function f(){var a; call(function(){return a})}f()",
         "{var a$$inline_0;call(function(){return a$$inline_0})}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposePlusEquals
  public void testDecomposePlusEquals() {
    test("function f(){a=1;return 1} var x = 1; x += f()",
        "var x = 1;" +
        "var JSCompiler_temp_const$$0 = x;" +
        "var JSCompiler_inline_result$$1;" +
        "{a=1;" +
        " JSCompiler_inline_result$$1=1}" +
        "x = JSCompiler_temp_const$$0 + JSCompiler_inline_result$$1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposeFunctionExpressionInCall
  public void testDecomposeFunctionExpressionInCall() {
    test(
        "(function(map){descriptions_=map})(\n" +
           "function(){\n" +
              "var ret={};\n" +
              "ret[ONE]='a';\n" +
              "ret[TWO]='b';\n" +
              "return ret\n" +
           "}()\n" +
        ");",
        "var JSCompiler_inline_result$$0;" +
        "{" +
        "var ret$$inline_1={};\n" +
        "ret$$inline_1[ONE]='a';\n" +
        "ret$$inline_1[TWO]='b';\n" +
        "JSCompiler_inline_result$$0 = ret$$inline_1;\n" +
        "}" +
        "{" +
        "descriptions_=JSCompiler_inline_result$$0;" +
        "}"
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor1
  public void testInlineConstructor1() {
    test("function f() {} function _g() {f.call(this)}",
         "function _g() {void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor2
  public void testInlineConstructor2() {
    test("function f() {} f.prototype.a = 0; function _g() {f.call(this)}",
         "function f() {} f.prototype.a = 0; function _g() {void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor3
  public void testInlineConstructor3() {
    test("function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {f.call(this)}",
         "function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {{x.call(this)}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor4
  public void testInlineConstructor4() {
    test("function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {var t = f.call(this)}",
         "function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {var t; {x.call(this); t = void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining1
  public void testFunctionExpressionInlining1() {
    test("(function(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining2
  public void testFunctionExpressionInlining2() {
    test("(function(){foo()})()",
         "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining3
  public void testFunctionExpressionInlining3() {
    test("var a = (function(){return foo()})()",
         "var a = foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining4
  public void testFunctionExpressionInlining4() {
    test("var a; a = 1 + (function(){return foo()})()",
         "var a; a = 1 + foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining1
  public void testFunctionExpressionCallInlining1() {
    test("(function(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining2
  public void testFunctionExpressionCallInlining2() {
    test("(function(){foo(this)}).call(this)",
         "{foo(this)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining3
  public void testFunctionExpressionCallInlining3() {
    test("var a = (function(){return foo(this)}).call(this)",
         "var a = foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining4
  public void testFunctionExpressionCallInlining4() {
    test("var a; a = 1 + (function(){return foo(this)}).call(this)",
         "var a; a = 1 + foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining5
  public void testFunctionExpressionCallInlining5() {
    test("a:(function(){return foo()})()",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining6
  public void testFunctionExpressionCallInlining6() {
    test("a:(function(){return foo()}).call(this)",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining7
  public void testFunctionExpressionCallInlining7() {
    test("a:(function(){})()",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining8
  public void testFunctionExpressionCallInlining8() {
    test("a:(function(){}).call(this)",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining9
  public void testFunctionExpressionCallInlining9() {
    
    test("(function foo(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining10
  public void testFunctionExpressionCallInlining10() {
    
    test("(function foo(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11a
  public void testFunctionExpressionCallInlining11a() {
    
    test("((function(){return function(){foo()}})())();", "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11b
  public void testFunctionExpressionCallInlining11b() {
    assumeMinimumCapture = false;
    
    testSame("((function(){var a; return function(){foo()}})())();");

    assumeMinimumCapture = true;
    test(
        "((function(){var a; return function(){foo()}})())();",

        "var JSCompiler_inline_result$$0;" +
        "{var a$$inline_1;" +
        "JSCompiler_inline_result$$0=function(){foo()};}" +
        "JSCompiler_inline_result$$0()");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11c
  public void testFunctionExpressionCallInlining11c() {
    
    assumeMinimumCapture = false;
    testSame("function _x() {" +
         "  ((function(){return function(){foo()}})())();" +
         "}");

    assumeMinimumCapture = true;
    test(
        "function _x() {" +
        "  ((function(){return function(){foo()}})())();" +
        "}",
        "function _x() {" +
        "  {foo()}" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11d
  public void testFunctionExpressionCallInlining11d() {
    
    
    assumeMinimumCapture = false;
    testSame("function _x() {" +
         "  eval();" +
         "  ((function(){return function(){foo()}})())();" +
         "}");

    assumeMinimumCapture = true;
    test(
        "function _x() {" +
        "  eval();" +
        "  ((function(){return function(){foo()}})())();" +
        "}",
        "function _x() {" +
        "  eval();" +
        "  {foo()}" +
        "}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11e
  public void testFunctionExpressionCallInlining11e() {
    
    
    assumeMinimumCapture = false;
    testSame("function _x() {" +
         "  eval();" +
         "  ((function(a){return function(){foo()}})())();" +
         "}");

    assumeMinimumCapture = true;
    test("function _x() {" +
        "  eval();" +
        "  ((function(a){return function(){foo()}})())();" +
        "}",
        "function _x() {" +
        "  eval();" +
        "  {foo();}" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining12
  public void testFunctionExpressionCallInlining12() {
    
    testSame("(function foo(){foo()})()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionOmega
  public void testFunctionExpressionOmega() {
    
    test("(function (f){f(f)})(function(f){f(f)})",
         "{var f$$inline_0=function(f$$1){f$$1(f$$1)};" +
          "{{f$$inline_0(f$$inline_0)}}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining1
  public void testLocalFunctionInlining1() {
    test("function _f(){ function g() {} g() }",
         "function _f(){ void 0 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining2
  public void testLocalFunctionInlining2() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining3
  public void testLocalFunctionInlining3() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining4
  public void testLocalFunctionInlining4() {
    test("function _f(){ function g() {return 1} return g() }",
         "function _f(){ return 1 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining5
  public void testLocalFunctionInlining5() {
    testSame("function _f(){ function g() {this;} g() }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining6
  public void testLocalFunctionInlining6() {
    testSame("function _f(){ function g() {this;} return g; }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly1
  public void testLocalFunctionInliningOnly1() {
    this.allowGlobalFunctionInlining = true;
    test("function f(){} f()", "void 0;");
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly2
  public void testLocalFunctionInliningOnly2() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("function f(){ function g() {return 1} return g() }; f();",
         "function f(){ return 1 }; f();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly3
  public void testLocalFunctionInliningOnly3() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ function g() {return 1} return g() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly4
  public void testLocalFunctionInliningOnly4() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ return (function() {return 1})() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis1
  public void testInlineWithThis1() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call();");
    testSame("function f(){this} f.call();");

    assumeStrictThis = true;
    
    test("function f(){} f.call();", "{}");
    test("function f(){this} f.call();",
         "{void 0;}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis2
  public void testInlineWithThis2() {
    
    assumeStrictThis = false;
    test("function f(){} f.call(this);", "void 0");

    assumeStrictThis = true;
    test("function f(){} f.call(this);", "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis3
  public void testInlineWithThis3() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call([]);");

    assumeStrictThis = true;
    
    test("function f(){} f.call([]);", "{}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis4
  public void testInlineWithThis4() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call(new g);");

    assumeStrictThis = true;
    
    test("function f(){} f.call(new g);",
         "{var JSCompiler_inline_this_0=new g}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis5
  public void testInlineWithThis5() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call(g());");

    assumeStrictThis = true;
    
    test("function f(){} f.call(g());",
         "{var JSCompiler_inline_this_0=g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis6
  public void testInlineWithThis6() {
    assumeStrictThis = false;
    
    
    testSame("function f(){this} f.call(new g);");

    assumeStrictThis = true;
    
    test("function f(){this} f.call(new g);",
         "{var JSCompiler_inline_this_0=new g;JSCompiler_inline_this_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis7
  public void testInlineWithThis7() {
    assumeStrictThis = true;
    
    test("function f(a){a=1;this} f.call();",
         "{var a$$inline_0=void 0; a$$inline_0=1; void 0;}");
    test("function f(a){a=1;this} f.call(x, x);",
         "{var a$$inline_0=x; a$$inline_0=1; x;}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionYCombinator
  public void testFunctionExpressionYCombinator() {
    assumeMinimumCapture = false;
    testSame(
        "var factorial = ((function(M) {\n" +
        "      return ((function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                            })\n" +
        "               })\n" +
        "              (function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                           })\n" +
        "                 }));\n" +
        "     })\n" +
        "    (function(f) {\n" +
        "       return function(n) {\n" +
        "        if (n === 0)\n" +
        "          return 1;\n" +
        "        else\n" +
        "          return n * f(n - 1);\n" +
        "       };\n" +
        "     }));\n" +
        "\n" +
        "factorial(5)\n");

    assumeMinimumCapture = true;
    test(
        "var factorial = ((function(M) {\n" +
        "      return ((function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                            })\n" +
        "               })\n" +
        "              (function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                           })\n" +
        "                 }));\n" +
        "     })\n" +
        "    (function(f) {\n" +
        "       return function(n) {\n" +
        "        if (n === 0)\n" +
        "          return 1;\n" +
        "        else\n" +
        "          return n * f(n - 1);\n" +
        "       };\n" +
        "     }));\n" +
        "\n" +
        "factorial(5)\n",
        "var factorial;\n" +
        "{\n" +
        "var M$$inline_4 = function(f$$2) {\n" +
        "  return function(n){if(n===0)return 1;else return n*f$$2(n-1)}\n" +
        "};\n" +
        "{\n" +
        "var f$$inline_0=function(f$$inline_7){\n" +
        "  return M$$inline_4(\n" +
        "    function(arg$$inline_8){\n" +
        "      return f$$inline_7(f$$inline_7)(arg$$inline_8)\n" +
        "     })\n" +
        "};\n" +
        "factorial=M$$inline_4(\n" +
        "  function(arg$$inline_1){\n" +
        "    return f$$inline_0(f$$inline_0)(arg$$inline_1)\n" +
        "});\n" +
        "}\n" +
        "}" +
        "factorial(5)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("function JSCompiler_renameProperty(x) {return x} " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testReplacePropertyFunction
  public void testReplacePropertyFunction() {
    
    
    test("function f(x) {return x} " +
         "foo(window, f); f(1)",
         "function f(x) {return x} " +
         "foo(window, f); 1");
    
    
    testSame("function f(x) {return x} " +
             "new JSCompiler_ObjectPropertyString(window, f); f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithClosureContainingThis
  public void testInlineWithClosureContainingThis() {
    test("(function (){return f(function(){return this})})();",
         "f(function(){return this})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue5159924a
  public void testIssue5159924a() {
    test("function f() { if (x()) return y() }\n" +
         "while(1){ var m = f() || z() }",
         "for(;1;) {" +
         "  var JSCompiler_inline_result$$0;" +
         "  {" +
         "    JSCompiler_inline_label_f_1: {" +
         "      if(x()) {" +
         "        JSCompiler_inline_result$$0 = y();" +
         "        break JSCompiler_inline_label_f_1" +
         "      }" +
         "      JSCompiler_inline_result$$0 = void 0;" +
         "    }" +
         "  }" +
         "  var m=JSCompiler_inline_result$$0 || z()" +
         "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue5159924b
  public void testIssue5159924b() {
    test("function f() { if (x()) return y() }\n" +
         "while(1){ var m = f() }",
         "for(;1;){" +
         "  var m;" +
         "  {" +
         "    JSCompiler_inline_label_f_0: { " +
         "      if(x()) {" +
         "        m = y();" +
         "        break JSCompiler_inline_label_f_0" +
         "      }" +
         "      m = void 0" +
         "    }" +
         "  }" +
         "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineObject
  public void testInlineObject() {
    new StringCompare().testInlineObject();
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineObject
    public void testInlineObject() {
      allowGlobalFunctionInlining = false;
      
      
      
      
      
      test("function inner(){function f(){return g.a}(f())()}",
           "function inner(){(0,g.a)()}");
    }

// com.google.javascript.jscomp.InlineFunctionsTest::testBug4944818
  public void testBug4944818() {
    test(
        "var getDomServices_ = function(self) {\n" +
        "  if (!self.domServices_) {\n" +
        "    self.domServices_ = goog$component$DomServices.get(" +
        "        self.appContext_);\n" +
        "  }\n" +
        "\n" +
        "  return self.domServices_;\n" +
        "};\n" +
        "\n" +
        "var getOwnerWin_ = function(self) {\n" +
        "  return getDomServices_(self).getDomHelper().getWindow();\n" +
        "};\n" +
        "\n" +
        "HangoutStarter.prototype.launchHangout = function() {\n" +
        "  var self = a.b;\n" +
        "  var myUrl = new goog.Uri(getOwnerWin_(self).location.href);\n" +
        "};",
        "HangoutStarter.prototype.launchHangout = function() { " +
        "  var self$$2 = a.b;" +
        "  var JSCompiler_temp_const$$0 = goog.Uri;" +
        "  var JSCompiler_inline_result$$1;" +
        "  {" +
        "  var self$$inline_2 = self$$2;" +
        "  if (!self$$inline_2.domServices_) {" +
        "    self$$inline_2.domServices_ = goog$component$DomServices.get(" +
        "        self$$inline_2.appContext_);" +
        "  }" +
        "  JSCompiler_inline_result$$1=self$$inline_2.domServices_;" +
        "  }" +
        "  var myUrl = new JSCompiler_temp_const$$0(" +
        "      JSCompiler_inline_result$$1.getDomHelper()." +
        "          getWindow().location.href)" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue423
  public void testIssue423() {
    assumeMinimumCapture = false;
    test(
        "(function($) {\n" +
        "  $.fn.multicheck = function(options) {\n" +
        "    initialize.call(this, options);\n" +
        "  };\n" +
        "\n" +
        "  function initialize(options) {\n" +
        "    options.checkboxes = $(this).siblings(':checkbox');\n" +
        "    preload_check_all.call(this);\n" +
        "  }\n" +
        "\n" +
        "  function preload_check_all() {\n" +
        "    $(this).data('checkboxes');\n" +
        "  }\n" +
        "})(jQuery)",
        "(function($){" +
        "  $.fn.multicheck=function(options$$1){" +
        "    {" +
        "     options$$1.checkboxes=$(this).siblings(\":checkbox\");" +
        "     {" +
        "       $(this).data(\"checkboxes\")" +
        "     }" +
        "    }" +
        "  }" +
        "})(jQuery)");

    assumeMinimumCapture = true;
    test(
        "(function($) {\n" +
        "  $.fn.multicheck = function(options) {\n" +
        "    initialize.call(this, options);\n" +
        "  };\n" +
        "\n" +
        "  function initialize(options) {\n" +
        "    options.checkboxes = $(this).siblings(':checkbox');\n" +
        "    preload_check_all.call(this);\n" +
        "  }\n" +
        "\n" +
        "  function preload_check_all() {\n" +
        "    $(this).data('checkboxes');\n" +
        "  }\n" +
        "})(jQuery)",
        "{var $$$inline_0=jQuery;\n" +
        "$$$inline_0.fn.multicheck=function(options$$inline_4){\n" +
        "  {options$$inline_4.checkboxes=" +
            "$$$inline_0(this).siblings(\":checkbox\");\n" +
        "  {$$$inline_0(this).data(\"checkboxes\")}" +
        "  }\n" +
        "}\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue728
  public void testIssue728() {
    String f = "var f = function() { return false; };";
    StringBuilder calls = new StringBuilder();
    StringBuilder folded = new StringBuilder();
    for (int i = 0; i < 30; i++) {
      calls.append("if (!f()) alert('x');");
      folded.append("if (!false) alert('x');");
    }

    test(f + calls, folded.toString());
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testAnonymous1
  public void testAnonymous1() {
    assumeMinimumCapture = false;
    test("(function(){var a=10;(function(){var b=a;a++;alert(b)})()})();",
         "{var a$$inline_0=10;" +
         "{var b$$inline_1=a$$inline_0;" +
         "a$$inline_0++;alert(b$$inline_1)}}");

    assumeMinimumCapture = true;
    test("(function(){var a=10;(function(){var b=a;a++;alert(b)})()})();",
        "{var a$$inline_2=10;" +
        "{var b$$inline_0=a$$inline_2;" +
        "a$$inline_2++;alert(b$$inline_0)}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testAnonymous2
  public void testAnonymous2() {
    testSame("(function(){eval();(function(){var b=a;a++;alert(b)})()})();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testAnonymous3
  public void testAnonymous3() {
    
    assumeMinimumCapture = false;
    testSame("(function(){var a=10;(function(){arguments;})()})();");

    assumeMinimumCapture = true;
    test("(function(){var a=10;(function(){arguments;})()})();",
         "{var a$$inline_0=10;(function(){arguments;})();}");

    test("(function(){(function(){arguments;})()})();",
        "{(function(){arguments;})()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLoopWithFunctionWithFunction
  public void testLoopWithFunctionWithFunction() {
    assumeMinimumCapture = true;
    test("function _testLocalVariableInLoop_() {\n" +
        "  var result = 0;\n" +
        "  function foo() {\n" +
        "    var arr = [1, 2, 3, 4, 5];\n" +
        "    for (var i = 0, l = arr.length; i < l; i++) {\n" +
        "      var j = arr[i];\n" +
        
        
        "      (function() {\n" +
        "        var k = j;\n" +
        "        setTimeout(function() { result += k; }, 5 * i);\n" +
        "      })();\n" +
        "    }\n" +
        "  }\n" +
        "  foo();\n" +
        "}",
        "function _testLocalVariableInLoop_(){\n" +
        "  var result=0;\n" +
        "  {" +
        "  var arr$$inline_0=[1,2,3,4,5];\n" +
        "  var i$$inline_1=0;\n" +
        "  var l$$inline_2=arr$$inline_0.length;\n" +
        "  for(;i$$inline_1<l$$inline_2;i$$inline_1++){\n" +
        "    var j$$inline_3=arr$$inline_0[i$$inline_1];\n" +
        "    (function(){\n" +
        "       var k$$inline_4=j$$inline_3;\n" +
        "       setTimeout(function(){result+=k$$inline_4},5*i$$inline_1)\n" +
        "     })()\n" +
        "  }\n" +
        "  }\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMethodWithFunctionWithFunction
  public void testMethodWithFunctionWithFunction() {
    assumeMinimumCapture = true;
    test("function _testLocalVariable_() {\n" +
        "  var result = 0;\n" +
        "  function foo() {\n" +
        "      var j = [i];\n" +
        "      (function(j) {\n" +
        "        setTimeout(function() { result += j; }, 5 * i);\n" +
        "      })(j);\n" +
        "      j = null;" +
        "  }\n" +
        "  foo();\n" +
        "}",
        "function _testLocalVariable_(){\n" +
        "  var result=0;\n" +
        "  {\n" +
        "  var j$$inline_2=[i];\n" +
        "  {\n" +
        "  var j$$inline_0=j$$inline_2;\n" +  
        "  setTimeout(function(){result+=j$$inline_0},5*i);\n" +
        "  }\n" +
        "  j$$inline_2=null\n" + 
        "  }\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining1
  public void testCrossModuleInlining1() {
    test(createModuleChain(
             
             "function foo(){return f(1)+g(2)+h(3);}",
             
             "foo()"
             ),
         new String[] {
             
             "",
             
             "f(1)+g(2)+h(3);"
            }
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining2
  public void testCrossModuleInlining2() {
    testSame(createModuleChain(
                
                "foo()",
                
                "function foo(){return f(1)+g(2)+h(3);}"
                )
            );

    test(createModuleChain(
             
             "foo()",
             
             "function foo(){return f();}"
             ),
         new String[] {
             
             "f();",
             
             ""
            }
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining3
  public void testCrossModuleInlining3() {
    testSame(createModuleChain(
                
                "foo()",
                
                "function foo(){return f(1)+g(2)+h(3);}",
                
                "foo()"
                )
            );

    test(createModuleChain(
             
             "foo()",
             
             "function foo(){return f();}",
             
             "foo()"
             ),
         new String[] {
             
             "f();",
             
             "",
             
             "f();"
            }
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::test6671158
  public void test6671158() {
    test(
        "function f() {return g()}" +
        "function Y(a){a.loader_()}" +
        "function _Z(){}" +
        "function _X() { new _Z(a,b, Y(singleton), f()) }",

        "function _Z(){}" +
        "function _X(){" +
        "  var JSCompiler_temp_const$$2=_Z;" +
        "  var JSCompiler_temp_const$$1=a;" +
        "  var JSCompiler_temp_const$$0=b;" +
        "  var JSCompiler_inline_result$$3;" +
        "  {" +
        "    singleton.loader_();" +
        "    JSCompiler_inline_result$$3=void 0;" +
        "  }" +
        "  new JSCompiler_temp_const$$2(" +
        "    JSCompiler_temp_const$$1," +
        "    JSCompiler_temp_const$$0," +
        "    JSCompiler_inline_result$$3," +
        "    g())}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::test8609285a
  public void test8609285a() {
   test(
       "function f(x){ for(x in y){} } f()",
       "{var x$$inline_0=void 0;for(x$$inline_0 in y);}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::test8609285b
  public void test8609285b() {
    test(
        "function f(x){ for(var x in y){} } f()",
        "{var x$$inline_0=void 0;for(x$$inline_0 in y);}");
   }

// com.google.javascript.jscomp.IntegrationTest::testConstructorCycle
  public void testConstructorCycle() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options,
        " var AsyncTestCase = function() {};\n" +
        " Foo =  (AyncTestCase());",
        RhinoErrorReporter.PARSE_ERROR);
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

// com.google.javascript.jscomp.IntegrationTest::testUnresolvedDefine
  public void testUnresolvedDefine() {
    CompilerOptions options = new CompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    DiagnosticType[] warnings = { ProcessDefines.INVALID_DEFINE_TYPE_ERROR,
                                  RhinoErrorReporter.TYPE_PARSE_ERROR };
    String[] input = { "var goog = {};" +
                       "goog.provide('foo.bar');" +
                       " foo.bar = {};" };
    String[] output = { "var goog = {};" +
                        "var foo = {};" +
                        " foo.bar = {};" };
    test(options, input, output, warnings);
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

// com.google.javascript.jscomp.IntegrationTest::testCheckEventfulDisposalWarningLevels
  public void testCheckEventfulDisposalWarningLevels() {
    CompilerOptions options = createCompilerOptions();
    options.setCheckEventfulObjectDisposalPolicy(
        CheckEventfulObjectDisposal.DisposalCheckingPolicy.ON);
    String js = "var goog = {};" + "goog.inherits = function(x, y) {};"
      + "goog.dispose = function(x) {};"
      + "goog.disposeAll = function(var_args) {};"
      + " goog.asserts.assert = function(x) { return x; };"
      + "goog.disposable = {};"
      + "\n"
      + "goog.disposable.IDisposable = function() {};"
      + "goog.disposable.IDisposable.prototype.dispose;"
      + "\n"
      + "goog.Disposable = goog.abstractMethod;"
      + ""
      + "goog.Disposable.prototype.dispose = goog.abstractMethod;"
      + ""
      + "goog.Disposable.prototype.registerDisposable = goog.abstractMethod;"
      + "goog.events = {};"
      + ""
      + "goog.events.EventHandler = function() {};"
      + ""
      + "var test = function() { this.eh = new goog.events.EventHandler(); };"
      + "goog.inherits(test, goog.Disposable);"
      + "var testObj = new test();";

    test(options, js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_NOT_DISPOSED);

    options.setWarningLevel(DiagnosticGroups.CHECK_EVENTFUL_OBJECT_DISPOSAL,
        CheckLevel.OFF);
    testSame(options, js);
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

// com.google.javascript.jscomp.IntegrationTest::testInstrumentMemoryAllocationPassOff
  public void testInstrumentMemoryAllocationPassOff() {
    testSame(createCompilerOptions(),
        "var obj = new Object(); " +
        "var o = {}; " +
        "var a = []; " +
        "var f = function() {};" +
        "var s = 'a' + 'b'");
  }

// com.google.javascript.jscomp.IntegrationTest::testInstrumentMemoryAllocationPassOn
  public void testInstrumentMemoryAllocationPassOn() {}

// com.google.javascript.jscomp.IntegrationTest::testAngularPassOff
  public void testAngularPassOff() {
    testSame(createCompilerOptions(),
        " function f() {} " +
        " function g(a){} " +
        " var b = function f(a) {} ");
  }

// com.google.javascript.jscomp.IntegrationTest::testAngularPassOn
  public void testAngularPassOn() {
    CompilerOptions options = createCompilerOptions();
    options.angularPass = true;
    test(options,
        " function f() {} " +
        " function g(a){} " +
        " var b = function f(a, b, c) {} ",

        "function f() {} " +
        "function g(a) {} g['$inject']=['a'];" +
        "var b = function f(a, b, c) {}; b['$inject']=['a', 'b', 'c']");
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

// com.google.javascript.jscomp.IntegrationTest::testExpose
  public void testExpose() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
         "var x = {eeny: 1,  meeny: 2};" +
         " var Foo = function() {};" +
         "  Foo.prototype.miny = 3;" +
         "Foo.prototype.moe = 4;" +
         "  Foo.prototype.tiger;" +
         "function moe(a, b) { return a.meeny + b.miny + a.tiger; }" +
         "window['x'] = x;" +
         "window['Foo'] = Foo;" +
         "window['moe'] = moe;",
         "function a(){}" +
         "a.prototype.miny=3;" +
         "window.x={a:1,meeny:2};" +
         "window.Foo=a;" +
         "window.moe=function(b,c){" +
         "  return b.meeny+c.miny+b.tiger" +
         "}");
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
    options.checkSymbols = true;
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
        + "  function Bar() {}\n"
        + "  Foo.prototype.a = function(a, b) {};\n"
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

// com.google.javascript.jscomp.IntegrationTest::testReplaceIdGeneratorsTest
  public void testReplaceIdGeneratorsTest() {
    CompilerOptions options = createCompilerOptions();
    options.replaceIdGenerators = true;

    options.setIdGenerators(ImmutableMap.<String, RenamingMap>of(
        "xid", new RenamingMap() {
      @Override
      public String get(String value) {
        return ":" + value + ":";
      }
    }));

    test(options, ""
         + "var xid = function() {};\n"
         + "function f() {\n"
         + "  return xid('foo');\n"
         + "}",
         "var xid = function() {};\n"
         + "function f() {\n"
         + "  return ':foo:';\n"
         + "}");
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

    options.setIdGenerators(Sets.newHashSet("f"));
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

// com.google.javascript.jscomp.IntegrationTest::testGoogDefine1
  public void testGoogDefine1() {
    String code = CLOSURE_BOILERPLATE +
        " goog.define('FLAG', true);";

    CompilerOptions options = createCompilerOptions();

    options.closurePass = true;
    options.collapseProperties = true;
    options.setDefineToBooleanLiteral("FLAG", false);

    test(options, code, CLOSURE_COMPILED + " var FLAG = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefine2
  public void testGoogDefine2() {
    String code = CLOSURE_BOILERPLATE +
        "goog.provide('ns');" +
        " goog.define('ns.FLAG', true);";

    CompilerOptions options = createCompilerOptions();

    options.closurePass = true;
    options.collapseProperties = true;
    options.setDefineToBooleanLiteral("ns.FLAG", false);
    test(options, code, CLOSURE_COMPILED + "var ns$FLAG = false;");
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
         "var foo = {}; foo = {}; foo.Bar = {};");
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

// com.google.javascript.jscomp.IntegrationTest::testNameAnonymousFunctionsWithVarRemoval
  public void testNameAnonymousFunctionsWithVarRemoval() {
    CompilerOptions options = createCompilerOptions();
    options.setRemoveUnusedVariables(CompilerOptions.Reach.LOCAL_ONLY);
    options.setInlineVariables(true);
    String code = "var f = function longName() {}; var g = function() {};" +
        "function longerName() {} var i = longerName;";
    test(options, code,
         "var f = function() {}; var g = function() {}; " +
         "var i = function() {};");

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.MAPPED;
    test(options, code,
         "var f = function longName() {}; var g = function $() {};" +
         "var i = function longerName(){};");
    assertNotNull(lastCompiler.getResult().namedAnonFunctionMap);

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code,
         "var f = function longName() {}; var g = function $g$() {};" +
         "var i = function longerName(){};");
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

      
      if (letter >= 'i') {
        letter++;
      }
      if (letter >= 'j') {
        letter++;
      }
      if (letter >= 'o') {
        letter++;
      }

      expected += "a.argz = function() {arguments};";
      expected += "function " + letter + "(c){}";
    }

    code += "var F = new f(); F.argz();";
    expected += "var q = new b(); q.argz();";

    for (int i = 0; i < 10; i++) {
      code += "F.devir" + i + "();";

      char letter = (char) ('d' + i);

      
      if (letter >= 'i') {
        letter++;
      }
      if (letter >= 'j') {
        letter++;
      }
      if (letter >= 'o') {
        letter++;
      }

      expected += letter + "(q);";
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
    String code = "longLabel: for(;true;) { break longLabel; }";
    String expected = "a: for(;true;) { break a; }";
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
