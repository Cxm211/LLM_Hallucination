// buggy code
    Node processFunctionNode(FunctionNode functionNode) {
      Name name = functionNode.getFunctionName();
      Boolean isUnnamedFunction = false;
      if (name == null) {
        name = new Name();
        name.setIdentifier("");
        isUnnamedFunction = true;
      }
      Node node = newNode(Token.FUNCTION);
      Node newName = transform(name);
      if (isUnnamedFunction) {
        // Old Rhino tagged the empty name node with the line number of the
        // declaration.
        newName.setLineno(functionNode.getLineno());
        // TODO(bowdidge) Mark line number of paren correctly.
        // Same problem as below - the left paren might not be on the
        // same line as the function keyword.
        int lpColumn = functionNode.getAbsolutePosition() +
            functionNode.getLp();
        newName.setCharno(position2charno(lpColumn));
      }

      node.addChildToBack(newName);
      Node lp = newNode(Token.LP);
      // The left paren's complicated because it's not represented by an
      // AstNode, so there's nothing that has the actual line number that it
      // appeared on.  We know the paren has to appear on the same line as the
      // function name (or else a semicolon will be inserted.)  If there's no
      // function name, assume the paren was on the same line as the function.
      // TODO(bowdidge): Mark line number of paren correctly.
      Name fnName = functionNode.getFunctionName();
      if (fnName != null) {
        lp.setLineno(fnName.getLineno());
      } else {
        lp.setLineno(functionNode.getLineno());
      }
      int lparenCharno = functionNode.getLp() +
          functionNode.getAbsolutePosition();

      lp.setCharno(position2charno(lparenCharno));
      for (AstNode param : functionNode.getParams()) {
        lp.addChildToBack(transform(param));
      }
      node.addChildToBack(lp);

      Node bodyNode = transform(functionNode.getBody());
      parseDirectives(bodyNode);
      node.addChildToBack(bodyNode);
     return node;
    }

// relevant test
// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions25
  public void testInlineFunctions25() {
    testSame("function foo(){return arguments[0]}foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions26
  public void testInlineFunctions26() {
    
    testSame("function _foo(x){return x}_foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions27
  public void testInlineFunctions27() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: 1, z: foo(2)};",
        "var window={};" +
        "{" +
        "  var JSCompiler_inline_result$$0;" +
        "  window.bar++;" +
        "  JSCompiler_inline_result$$0 = 3;" +
        "}" +
        "var x = {y: 1, z: JSCompiler_inline_result$$0};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions28
  public void testInlineFunctions28() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: alert(), z: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$0 = alert();" +
        "{" +
        " var JSCompiler_inline_result$$1;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$1 = 3;}" +
        "var x = {" +
        "  y: JSCompiler_temp_const$$0," +
        "  z: JSCompiler_inline_result$$1" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions29
  public void testInlineFunctions29() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {a: alert(), b: alert2(), c: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$1 = alert();" +
        "var JSCompiler_temp_const$$0 = alert2();" +
        "{" +
        " var JSCompiler_inline_result$$2;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$2 = 3;}" +
        "var x = {" +
        "  a: JSCompiler_temp_const$$1," +
        "  b: JSCompiler_temp_const$$0," +
        "  c: JSCompiler_inline_result$$2" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining1
  public void testMixedModeInlining1() {
    
    test("function foo(){return 1}" +
        "foo();",
        "1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining2
  public void testMixedModeInlining2() {
    
    
    test("function foo(){return 1}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining3
  public void testMixedModeInlining3() {
    
    test("function foo(){return 1}" +
        "foo();foo(x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining4
  public void testMixedModeInlining4() {
    
    
    test("function foo(){return 1}" +
        "foo();foo(x());" +
        "foo(1);foo(1,x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}" +
        "1;{var JSCompiler_inline_anon_param_4=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting1
  public void testMixedModeInliningCosting1() {
    

    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3)",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+4+5;" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+4+5");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting2
  public void testMixedModeInliningCosting2() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting3
  public void testMixedModeInliningCosting3() {
    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+10}" +
        "foo(1,2);" +
        "foo(2,3,x())",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+10;" +
        "{var JSCompiler_inline_anon_param_4=x();" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+10}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting4
  public void testMixedModeInliningCosting4() {
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+101}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified1
  public void testNoInlineIfParametersModified1() {
    
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified2
  public void testNoInlineIfParametersModified2() {
    test("function f(x){return (x)=1;}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified3
  public void testNoInlineIfParametersModified3() {
    
    test("function f(x){return x*=2}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1*=2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified4
  public void testNoInlineIfParametersModified4() {
    
    test("function f(x){return x?(x=2):0}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1?(" +
         "x$$inline_1=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified5
  public void testNoInlineIfParametersModified5() {
    
    test("function f(x,y){return x?(y=2):0}f(2,undefined)",
         "{var y$$inline_3=undefined;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified6
  public void testNoInlineIfParametersModified6() {
    test("function f(x,y){return x?(y=2):0}f(2)",
         "{var y$$inline_3=void 0;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified7
  public void testNoInlineIfParametersModified7() {
    
    test("function f(a){return++a<++a}f(1)",
         "{var a$$inline_1=1;" +
         "++a$$inline_1<" +
         "++a$$inline_1}");
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
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverOverrideNewValues
  public void testInlineNeverOverrideNewValues() {
    test("function f(a){return++a<++a}f(1)",
        "{var a$$inline_1=1;" +
        "++a$$inline_1<++a$$inline_1}");
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
         "{var x$$inline_1=[];" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs2
  public void testInlineBlockMutableArgs2() {
    test("function foo(x){x+x}foo(new Date)",
         "{var x$$inline_1=new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs3
  public void testInlineBlockMutableArgs3() {
    test("function foo(x){x+x}foo(true&&new Date)",
         "{var x$$inline_1=true&&new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs4
  public void testInlineBlockMutableArgs4() {
    test("function foo(x){x+x}foo({})",
         "{var x$$inline_1={};" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables1
  public void testShadowVariables1() {
    
    

    
    
    test("var a=0;" +
         "function foo(a){return 3+a}" +
         "function bar(){var a=foo(4)}" +
         "bar();",

         "var a=0;" +
         "{var a$$inline_1=3+4}");
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
        "function _bar(){{var a$$inline_1=2;" +
        "a=3+a$$inline_1}}");
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
        "var a$$inline_1=4;" +
        "a$$2=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables7
  public void testShadowVariables7() {
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
        "{var a$$inline_1=3;x=a+a}}");
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
         "{var a$$inline_1=1+1+1;" +
         "a=1+a$$inline_1+a$$inline_1}");
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
         "{var a$$inline_1=1+1;" +
         "a=a$$inline_1+a$$inline_1}");
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
         "function _foo(){{a();b();var z$$inline_1=1+1}}");

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
         "var b=1;{JSCompiler_inline_label_f_4:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_4}else{z=true;" +
         "break JSCompiler_inline_label_f_4}z=void 0}}");
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
         "{JSCompiler_inline_label_f_4:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_4" +
         "}else{" +
         "z=true;break JSCompiler_inline_label_f_4}z=void 0}}");
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
         "{var JSCompiler_inline_result$$0;a();}" +
         "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions3
  public void testComplexInlineInExpresssions3() {
    test("function f(){a()}c=z=f()",
        "{var JSCompiler_inline_result$$0;a();}" +
        "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions4
  public void testComplexInlineInExpresssions4() {
    test("function f(){a()}if(z=f());",
        "{var JSCompiler_inline_result$$0;a();}" +
        "if(z=JSCompiler_inline_result$$0);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions5
  public void testComplexInlineInExpresssions5() {
    test("function f(){a()}if(z.y=f());",
         "var JSCompiler_temp_const$$0=z;" +
         "{var JSCompiler_inline_result$$1;a()}" +
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
      "var styleSheet$$inline_9=null;" +
      "if(goog$userAgent$IE)" +
        "styleSheet$$inline_9=0;" +
      "else " +
        "var head$$inline_10=0;" +
      "{" +
        "var element$$inline_11=" +
            "styleSheet$$inline_9;" +
        "var stylesString$$inline_12=a;" +
        "if(goog$userAgent$IE)" +
          "element$$inline_11.cssText=" +
              "stylesString$$inline_12;" +
        "else " +
        "{" +
          "var propToSet$$inline_13=" +
              "\"innerText\";" +
          "element$$inline_11[" +
              "propToSet$$inline_13]=" +
                  "stylesString$$inline_12" +
        "}" +
      "}" +
      "styleSheet$$inline_9" +
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
    
    String result =
    "foo=function(stylesString,opt_element){" +
        "var styleSheet=null;" +
        "if(goog$userAgent$IE){" +
          "styleSheet=0" +
        "}else{" +
          "var head=0" +
         "}" +
         "{var JSCompiler_inline_element_0=styleSheet;" +
         "var JSCompiler_inline_stylesString_1=stylesString;" +
         "if(goog$userAgent$IE){" +
           "JSCompiler_inline_element_0.cssText=" +
           "JSCompiler_inline_stylesString_1" +
         "}else{" +
           "var propToSet=goog$userAgent$WEBKIT?\"innerText\":\"innerHTML\";" +
           "JSCompiler_inline_element_0[propToSet]=" +
           "JSCompiler_inline_stylesString_1" +
         "}}" +
        "return styleSheet" +
     "}";

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
    
    testSame("function f(a){call(function(){return})}f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition3
  public void testComplexFunctionWithFunctionDefinition3() {
    
    testSame("function f(){var a; call(function(){return})}f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposePlusEquals
  public void testDecomposePlusEquals() {
    test("function f(){a=1;return 1} var x = 1; x += f()",
        "var x = 1;" +
        "var JSCompiler_temp_const$$0 = x;" +
        "{var JSCompiler_inline_result$$1; a=1; JSCompiler_inline_result$$1=1}" +
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
        "{" +
        "var JSCompiler_inline_result$$0;" +
        "var ret$$inline_2={};\n" +
        "ret$$inline_2[ONE]='a';\n" +
        "ret$$inline_2[TWO]='b';\n" +
        "JSCompiler_inline_result$$0 = ret$$inline_2;\n" +
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
    
    testSame("((function(){var a; return function(){foo()}})())();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11c
  public void testFunctionExpressionCallInlining11c() {
    
    testSame("function _x() {" +
                "((function(){return function(){foo()}})())();" +
                "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining12
  public void testFunctionExpressionCallInlining12() {
    
    testSame("(function foo(){foo()})()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionOmega
  public void testFunctionExpressionOmega() {
    
    test("(function (f){f(f)})(function(f){f(f)})",
         "{var f$$inline_1=function(f$$1){f$$1(f$$1)};" +
          "{{f$$inline_1(f$$inline_1)}}}");
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

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionYCombinator
  public void testFunctionExpressionYCombinator() {
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
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("function JSCompiler_renameProperty(x) {return x} " +
             "JSCompiler_renameProperty('foo')");
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

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleInline1
  public void testSimpleInline1() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "var x=(new Foo).bar();var y=(new Foo).bar();",
        "var x=(new Foo).baz;var y=(new Foo).baz");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleInline2
  public void testSimpleInline2() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype={bar:function(){return this.baz}};",
        "var x=(new Foo).bar();var y=(new Foo).bar();",
        "var x=(new Foo).baz;var y=(new Foo).baz");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleGetterInline1
  public void testSimpleGetterInline1() {
    
    testSame("function Foo(){}" +
      "Foo.prototype={get bar(){return this.baz}};" +
      "var x=(new Foo).bar;var y=(new Foo).bar");
    
    
    testSame("function Foo(){}" +
      "Foo.prototype={get bar(){return this.baz}};" +
      "var x=(new Foo).bar();var y=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleSetterInline1
  public void testSimpleSetterInline1() {
    
    testSame("function Foo(){}" +
      "Foo.prototype={set bar(a){return this.baz}};" +
      "var x=(new Foo).bar;var y=(new Foo).bar");
    testSame("function Foo(){}" +
      "Foo.prototype={set bar(a){return this.baz}};" +
      "var x=(new Foo).bar();var y=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSelfInline
  public void testSelfInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "Foo.prototype.meth=function(){this.bar();}",
        "Foo.prototype.meth=function(){this.baz}");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testCallWithArgs
  public void testCallWithArgs() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "var x=(new Foo).bar(3,new Foo)",
        "var x=(new Foo).bar(3,new Foo)");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testCallWithConstArgs
  public void testCallWithConstArgs() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(a){return this.baz};",
        "var x=(new Foo).bar(3, 4)",
        "var x=(new Foo).baz");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNestedProperties
  public void testNestedProperties() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz.ooka};",
        "(new Foo).bar()",
        "(new Foo).baz.ooka");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSkipComplexMethods
  public void testSkipComplexMethods() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};" +
        "Foo.prototype.condy=function(){return this.baz?this.baz:1};",
        "var x=(new Foo).argy()",
        "var x=(new Foo).argy()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSkipConflictingMethods
  public void testSkipConflictingMethods() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};" +
        "Foo.prototype.bar=function(){return this.bazz};",
        "var x=(new Foo).bar()",
        "var x=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSameNamesDifferentDefinitions
  public void testSameNamesDifferentDefinitions() {
    testWithPrefix("function A(){}" +
        "A.prototype.g=function(){return this.a};" +
        "function B(){}" +
        "B.prototype.g=function(){return this.b};",
        "var x=(new A).g();" +
        "var y=(new B).g();" +
        "var a=new A;" +
        "var ag=a.g();",
        "var x=(new A).g();" +
        "var y=(new B).g();" +
        "var a=new A;" +
        "var ag=a.g()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSameNamesSameDefinitions
  public void testSameNamesSameDefinitions() {
    testWithPrefix("function A(){}" +
        "A.prototype.g=function(){return this.a};" +
        "function B(){}" +
        "B.prototype.g=function(){return this.a};",
        "var x=(new A).g();" +
        "var y=(new B).g();" +
        "var a=new A;" +
        "var ag=a.g();",
        "var x=(new A).a;" +
        "var y=(new B).a;" +
        "var a=new A;" +
        "var ag=a.a");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConfusingNames
  public void testConfusingNames() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "function bar(){var bar=function(){};bar()}",
        "function bar(){var bar=function(){};bar()}");
  }

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
