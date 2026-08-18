// buggy code
  private void makeLocalNamesUnique(Node fnNode, boolean isCallInLoop) {
    Supplier<String> idSupplier = compiler.getUniqueNameIdSupplier();
    // Make variable names unique to this instance.
    NodeTraversal.traverse(
        compiler, fnNode, new MakeDeclaredNamesUnique(
            new InlineRenamer(
                idSupplier,
                "inline_",
                isCallInLoop)));
    // Make label names unique to this instance.
  }

    private void visitLabel(Node node, Node parent) {
      Node nameNode = node.getFirstChild();
      Preconditions.checkState(nameNode != null);
      String name = nameNode.getString();
      LabelInfo li = getLabelInfo(name);
      // This is a label...
      if (li.referenced) {
        String newName = getNameForId(li.id);
        if (!name.equals(newName)) {
          // ... and it is used, give it the short name.
          nameNode.setString(newName);
          compiler.reportCodeChange();
        }
      } else {
        // ... and it is not referenced, just remove it.
        Node newChild = node.getLastChild();
        node.removeChild(newChild);
        parent.replaceChild(node, newChild);
        if (newChild.getType() == Token.BLOCK) {
          NodeUtil.tryMergeBlock(newChild);
        }
        compiler.reportCodeChange();
      }

      // Remove the label from the current stack of labels.
      namespaceStack.peek().renameMap.remove(name);
    }

// relevant test
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
        "{var JSCompiler_inline_result$$1; a=1;" +
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

// com.google.javascript.jscomp.InlineFunctionsTest::testReplacePropertyFunction
  public void testReplacePropertyFunction() {
    
    
    test("function f(x) {return x} " +
         "foo(window, f); f(1)",
         "function f(x) {return x} " +
         "foo(window, f); 1");
    
    
    testSame("function f(x) {return x} " +
             "new JSCompiler_ObjectPropertyString(window, f); f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue423
  public void testIssue423() {
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

// com.google.javascript.jscomp.RenameLabelsTest::testRenameInFunction
  public void testRenameInFunction() {
    test("function x(){ Foo:a(); }",
         "function x(){ a(); }");
    test("function x(){ Foo:{ a(); break Foo; } }",
         "function x(){ a:{ a(); break a; } }");
    test("function x() { " +
            "Foo:{ " +
              "function goo() {" +
                "Foo: {" +
                  "a(); " +
                  "break Foo; " +
                "}" +
              "}" +
            "}" +
          "}",
          "function x(){function goo(){a:{ a(); break a; }}}");
    test("function x() { " +
          "Foo:{ " +
            "function goo() {" +
              "Foo: {" +
                "a(); " +
                "break Foo; " +
              "}" +
            "}" +
            "break Foo;" +
          "}" +
        "}",
        "function x(){a:{function goo(){a:{ a(); break a; }} break a;}}");
  }

// com.google.javascript.jscomp.RenameLabelsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("Foo:{a();}",
         "a();");
    test("Foo:{a(); break Foo;}",
         "a:{a(); break a;}");
    test("Foo:{Goo:a(); break Foo;}",
         "a:{a(); break a;}");
    test("Foo:{Goo:while(1){a(); continue Goo; break Foo;}}",
         "a:{b:while(1){a(); continue b;break a;}}");
    test("Foo:Goo:while(1){a(); continue Goo; break Foo;}",
         "a:b:while(1){a(); continue b;break a;}");

    test("Foo:Bar:X:{ break Bar; }",
         "a:{ break a; }");
    test("Foo:Bar:X:{ break Bar; break X; }",
         "a:b:{ break a; break b;}");
    test("Foo:Bar:X:{ break Bar; break Foo; }",
         "a:b:{ break b; break a;}");

    test("Foo:while (1){a(); break;}",
         "while (1){a(); break;}");

    
    test("Foo:{a(); while (1) break;}",
         "a(); while (1) break;");
  }

// com.google.javascript.jscomp.RenameLabelsTest::testRenameReused
  public void testRenameReused() {
    test("foo:{break foo}; foo:{break foo}", "a:{break a};a:{break a}");
  }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
