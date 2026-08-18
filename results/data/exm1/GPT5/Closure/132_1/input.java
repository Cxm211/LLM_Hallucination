// buggy code
  private Node tryMinimizeIf(Node n) {

    Node parent = n.getParent();

    Node cond = n.getFirstChild();

    /* If the condition is a literal, we'll let other
     * optimizations try to remove useless code.
     */
    if (NodeUtil.isLiteralValue(cond, true)) {
      return n;
    }

    Node thenBranch = cond.getNext();
    Node elseBranch = thenBranch.getNext();

    if (elseBranch == null) {
      if (isFoldableExpressBlock(thenBranch)) {
        Node expr = getBlockExpression(thenBranch);
        if (!late && isPropertyAssignmentInExpression(expr)) {
          // Keep opportunities for CollapseProperties such as
          // a.longIdentifier || a.longIdentifier = ... -> var a = ...;
          // until CollapseProperties has been run.
          return n;
        }

        if (cond.isNot()) {
          // if(!x)bar(); -> x||bar();
          if (isLowerPrecedenceInExpression(cond, OR_PRECEDENCE) &&
              isLowerPrecedenceInExpression(expr.getFirstChild(),
                  OR_PRECEDENCE)) {
            // It's not okay to add two sets of parentheses.
            return n;
          }

          Node or = IR.or(
              cond.removeFirstChild(),
              expr.removeFirstChild()).srcref(n);
          Node newExpr = NodeUtil.newExpr(or);
          parent.replaceChild(n, newExpr);
          reportCodeChange();

          return newExpr;
        }

        // if(x)foo(); -> x&&foo();
        if (isLowerPrecedenceInExpression(cond, AND_PRECEDENCE) &&
            isLowerPrecedenceInExpression(expr.getFirstChild(),
                AND_PRECEDENCE)) {
          // One additional set of parentheses is worth the change even if
          // there is no immediate code size win. However, two extra pair of
          // {}, we would have to think twice. (unless we know for sure the
          // we can further optimize its parent.
          return n;
        }

        n.removeChild(cond);
        Node and = IR.and(cond, expr.removeFirstChild()).srcref(n);
        Node newExpr = NodeUtil.newExpr(and);
        parent.replaceChild(n, newExpr);
        reportCodeChange();

        return newExpr;
      } else {

        // Try to combine two IF-ELSE
        if (NodeUtil.isStatementBlock(thenBranch) &&
            thenBranch.hasOneChild()) {
          Node innerIf = thenBranch.getFirstChild();

          if (innerIf.isIf()) {
            Node innerCond = innerIf.getFirstChild();
            Node innerThenBranch = innerCond.getNext();
            Node innerElseBranch = innerThenBranch.getNext();

            if (innerElseBranch == null &&
                 !(isLowerPrecedenceInExpression(cond, AND_PRECEDENCE) &&
                   isLowerPrecedenceInExpression(innerCond, AND_PRECEDENCE))) {
              n.detachChildren();
              n.addChildToBack(
                  IR.and(
                      cond,
                      innerCond.detachFromParent())
                      .srcref(cond));
              n.addChildrenToBack(innerThenBranch.detachFromParent());
              reportCodeChange();
              // Not worth trying to fold the current IF-ELSE into && because
              // the inner IF-ELSE wasn't able to be folded into && anyways.
              return n;
            }
          }
        }
      }

      return n;
    }

    /* TODO(dcc) This modifies the siblings of n, which is undesirable for a
     * peephole optimization. This should probably get moved to another pass.
     */
    tryRemoveRepeatedStatements(n);

    // if(!x)foo();else bar(); -> if(x)bar();else foo();
    // An additional set of curly braces isn't worth it.
    if (cond.isNot() && !consumesDanglingElse(elseBranch)) {
      n.replaceChild(cond, cond.removeFirstChild());
      n.removeChild(thenBranch);
      n.addChildToBack(thenBranch);
      reportCodeChange();
      return n;
    }

    // if(x)return 1;else return 2; -> return x?1:2;
    if (isReturnExpressBlock(thenBranch) && isReturnExpressBlock(elseBranch)) {
      Node thenExpr = getBlockReturnExpression(thenBranch);
      Node elseExpr = getBlockReturnExpression(elseBranch);
      n.removeChild(cond);
      thenExpr.detachFromParent();
      elseExpr.detachFromParent();

      // note - we ignore any cases with "return;", technically this
      // can be converted to "return undefined;" or some variant, but
      // that does not help code size.
      Node returnNode = IR.returnNode(
                            IR.hook(cond, thenExpr, elseExpr)
                                .srcref(n));
      parent.replaceChild(n, returnNode);
      reportCodeChange();
      return returnNode;
    }

    boolean thenBranchIsExpressionBlock = isFoldableExpressBlock(thenBranch);
    boolean elseBranchIsExpressionBlock = isFoldableExpressBlock(elseBranch);

    if (thenBranchIsExpressionBlock && elseBranchIsExpressionBlock) {
      Node thenOp = getBlockExpression(thenBranch).getFirstChild();
      Node elseOp = getBlockExpression(elseBranch).getFirstChild();
      if (thenOp.getType() == elseOp.getType()) {
        // if(x)a=1;else a=2; -> a=x?1:2;
        if (NodeUtil.isAssignmentOp(thenOp)) {
          Node lhs = thenOp.getFirstChild();
          if (areNodesEqualForInlining(lhs, elseOp.getFirstChild()) &&
              // if LHS has side effects, don't proceed [since the optimization
              // evaluates LHS before cond]
              // NOTE - there are some circumstances where we can
              // proceed even if there are side effects...
              !mayEffectMutableState(lhs)) {

            n.removeChild(cond);
            Node assignName = thenOp.removeFirstChild();
            Node thenExpr = thenOp.removeFirstChild();
            Node elseExpr = elseOp.getLastChild();
            elseOp.removeChild(elseExpr);

            Node hookNode = IR.hook(cond, thenExpr, elseExpr).srcref(n);
            Node assign = new Node(thenOp.getType(), assignName, hookNode)
                              .srcref(thenOp);
            Node expr = NodeUtil.newExpr(assign);
            parent.replaceChild(n, expr);
            reportCodeChange();

            return expr;
          }
        }
      }
      // if(x)foo();else bar(); -> x?foo():bar()
      n.removeChild(cond);
      thenOp.detachFromParent();
      elseOp.detachFromParent();
      Node expr = IR.exprResult(
          IR.hook(cond, thenOp, elseOp).srcref(n));
      parent.replaceChild(n, expr);
      reportCodeChange();
      return expr;
    }

    boolean thenBranchIsVar = isVarBlock(thenBranch);
    boolean elseBranchIsVar = isVarBlock(elseBranch);

    // if(x)var y=1;else y=2  ->  var y=x?1:2
    if (thenBranchIsVar && elseBranchIsExpressionBlock &&
        getBlockExpression(elseBranch).getFirstChild().isAssign()) {

      Node var = getBlockVar(thenBranch);
      Node elseAssign = getBlockExpression(elseBranch).getFirstChild();

      Node name1 = var.getFirstChild();
      Node maybeName2 = elseAssign.getFirstChild();

      if (name1.hasChildren()
          && maybeName2.isName()
          && name1.getString().equals(maybeName2.getString())) {
        Node thenExpr = name1.removeChildren();
        Node elseExpr = elseAssign.getLastChild().detachFromParent();
        cond.detachFromParent();
        Node hookNode = IR.hook(cond, thenExpr, elseExpr)
                            .srcref(n);
        var.detachFromParent();
        name1.addChildrenToBack(hookNode);
        parent.replaceChild(n, var);
        reportCodeChange();
        return var;
      }

    // if(x)y=1;else var y=2  ->  var y=x?1:2
    } else if (elseBranchIsVar && thenBranchIsExpressionBlock &&
        getBlockExpression(thenBranch).getFirstChild().isAssign()) {

      Node var = getBlockVar(elseBranch);
      Node thenAssign = getBlockExpression(thenBranch).getFirstChild();

      Node maybeName1 = thenAssign.getFirstChild();
      Node name2 = var.getFirstChild();

      if (name2.hasChildren()
          && maybeName1.isName()
          && maybeName1.getString().equals(name2.getString())) {
        Node thenExpr = thenAssign.getLastChild().detachFromParent();
        Node elseExpr = name2.removeChildren();
        cond.detachFromParent();
        Node hookNode = IR.hook(cond, thenExpr, elseExpr)
                            .srcref(n);
        var.detachFromParent();
        name2.addChildrenToBack(hookNode);
        parent.replaceChild(n, var);
        reportCodeChange();

        return var;
      }
    }

    return n;
  }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testGoogDefineClass1
  public void testGoogDefineClass1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);

    String code = "" +
        "var ns = {};\n" +
        "ns.C = goog.defineClass(null, {\n" +
        "  \n" +
        "  constructor: function () {this.someProperty = 1}\n" +
        "});\n" +
        "alert(new ns.C().someProperty + new ns.C().someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, "alert(2);");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefineClass2
  public void testGoogDefineClass2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);

    String code = "" +
        "var C = goog.defineClass(null, {\n" +
        "  \n" +
        "  constructor: function () {this.someProperty = 1}\n" +
        "});\n" +
        "alert(new C().someProperty + new C().someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, "alert(2);");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefineClass3
  public void testGoogDefineClass3() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);
    WarningLevel warnings = WarningLevel.VERBOSE;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "var C = goog.defineClass(null, {\n" +
        "  \n" +
        "  constructor: function () {\n" +
        "    \n" +
        "    this.someProperty = 1},\n" +
        "  \n" +
        "  someMethod: function (a) {}\n" +
        "});" +
        "var x = new C();\n" +
        "x.someMethod(x.someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConstants1
  public void testCheckConstants1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.QUIET;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "var foo; foo();\n" +
        "\n" +
        "var x = 1; foo(); x = 2;\n";
    test(options, code, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConstants2
  public void testCheckConstants2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "var foo;\n" +
        "\n" +
        "var x = 1; foo(); x = 2;\n";
    test(options, code, ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue787
  public void testIssue787() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "function some_function() {\n" +
        "  var fn1;\n" +
        "  var fn2;\n" +
        "\n" +
        "  if (any_expression) {\n" +
        "    fn2 = external_ref;\n" +
        "    fn1 = function (content) {\n" +
        "      return fn2();\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  return {\n" +
        "    method1: function () {\n" +
        "      if (fn1) fn1();\n" +
        "      return true;\n" +
        "    },\n" +
        "    method2: function () {\n" +
        "      return false;\n" +
        "    }\n" +
        "  }\n" +
        "}";

    String result = "" +
        "function some_function() {\n" +
        "  var a, b;\n" +
        "  any_expression && (b = external_ref, a = function(a) {\n" +
        "    return b()\n" +
        "  });\n" +
        "  return{method1:function() {\n" +
        "    a && a();\n" +
        "    return !0\n" +
        "  }, method2:function() {\n" +
        "    return !1\n" +
        "  }}\n" +
        "}\n" +
        "";

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testManyAdds
  public void testManyAdds() {}

// com.google.javascript.jscomp.IntegrationTest::testPerfTracker
  public void testPerfTracker() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream outstream = new PrintStream(output);
    Compiler compiler = new Compiler(outstream);
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> inputs = Lists.newArrayList();
    List<SourceFile> externs = Lists.newArrayList();

    options.setTracerMode(TracerMode.ALL);
    inputs.add(SourceFile.fromCode("foo", "function fun(){}"));
    compiler.compile(externs, inputs, options);
    outstream.flush();
    outstream.close();
    Pattern p = Pattern.compile(
        ".*Summary:\npass,runtime,runs,changingRuns,reduction,gzReduction" +
        ".*TOTAL:" +
        "\nRuntime\\(ms\\): [0-9]+" +
        "\n#Runs: [0-9]+" +
        "\n#Changing runs: [0-9]+" +
        "\n#Loopable runs: [0-9]+" +
        "\n#Changing loopable runs: [0-9]+" +
        "\nReduction\\(bytes\\): [0-9]+" +
        "\nGzReduction\\(bytes\\): [0-9]+" +
        "\nSize\\(bytes\\): [0-9]+" +
        "\nGzSize\\(bytes\\): [0-9]+" +
        "\n\nLog:\n" +
        "pass,runtime,runs,changingRuns,reduction,gzReduction,size,gzSize.*",
        Pattern.DOTALL);
    assertTrue(p.matcher(output.toString()).matches());
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
         "function z() {return true;}");

    fold("function z() {if (a()) { return true }" +
         "else if (b()) { return true }" +
         "else { return true }}",
         "function z() {a()||b();return true;}");
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
    late = true;
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
    late = false;
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testAvoidCommaSplitting
  public void testAvoidCommaSplitting() {
    late = false;
    fold("x(),y(),z()", "x();y();z()");
    late = true;
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

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldHook2
  public void testFoldHook2() {
    fold("function f(a) {if (!a) return a; else return a;}",
         "function f(a) {return a}");
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
    fold("if(!x)z=1;else if(y)z=2", "if(x){y&&(z=2);}else{z=1;}");
    fold("if(x)y&&(z=2);else z=1;", "x ? y&&(z=2) : z=1");
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
    
    testSame("x = RegExp(\"foobar\", \"bogus\")",
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
    fold("for(;a;) { if (b) break; if (c) break; }",
         "for(;(a && !b);) if (c) break;");
    fold("for(;(a && !b);) if (c) break;", "for(;(a && !b) && !c;);");

    
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
         "function f(){return}");
    fold("function f(){return;}",
         "function f(){}");
    foldSame("function f(){return void foo();}");
    fold("function f(){return undefined;}",
         "function f(){return}");
    fold("function f(){if(a()){return undefined;}}",
         "function f(){if(a()){return}}");
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
    late = false;
    
    foldSame("while (foo(), !0) boo()");
    foldSame("var a = (foo(), !0);");
    foldSame("a = (foo(), !0);");

    
    foldSame("a:a(),b()");

    fold("(x=2), foo()", "x=2; foo()");
    fold("foo(), boo();", "foo(); boo()");
    fold("(a(), b()), (c(), d());", "a(); b(); (c(), d());");
    fold("a(); b(); (c(), d());", "a(); b(); c(); d();");
    fold("foo(), true", "foo();true");
    fold("foo();true", "foo();1");
    fold("function x(){foo(), !0}", "function x(){foo(); !0}");
    fold("function x(){foo(); !0}", "function x(){foo(); 1}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma1
  public void testComma1() {
    late = false;
    fold("1, 2", "1; 2");
    fold("1; 2", "1; 1");
    late = true;
    foldSame("1, 2");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma2
  public void testComma2() {
    late = false;
    test("1, a()", "1; a()");
    late = true;
    foldSame("1, a()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma3
  public void testComma3() {
    late = false;
    test("1, a(), b()", "1; a(); b()");
    late = true;
    foldSame("1, a(), b()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma4
  public void testComma4() {
    late = false;
    test("a(), b()", "a();b()");
    late = true;
    foldSame("a(), b()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma5
  public void testComma5() {
    late = false;
    test("a(), b(), 1", "a();b();1");
    late = true;
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
         "var x='123456'.split('')");
    test("var x=['1','2','3','4','5','00']",
         "var x='1 2 3 4 5 00'.split(' ')");
    test("var x=['1','2','3','4','5','6','7']",
        "var x='1234567'.split('')");
    test("var x=['1','2','3','4','5','6','00']",
         "var x='1 2 3 4 5 6 00'.split(' ')");
    test("var x=[' ,',',',',',',',',',',']",
         "var x=' ,;,;,;,;,;,'.split(';')");
    test("var x=[',,',' ',',',',',',',',']",
         "var x=',,; ;,;,;,;,'.split(';')");
    test("var x=['a,',' ',',',',',',',',']",
         "var x='a,; ;,;,;,;,'.split(';')");

    
    testSame("var x=[',', ' ', ';', '{', '}']");
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

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testIssue925
  public void testIssue925() {
    test(
        "if (x[--y] === 1) {\n" +
        "    x[y] = 0;\n" +
        "} else {\n" +
        "    x[y] = 1;\n" +
        "}",
        "(x[--y] === 1) ? x[y] = 0 : x[y] = 1;");

    test(
        "if (x[--y]) {\n" +
        "    a = 0;\n" +
        "} else {\n" +
        "    a = 1;\n" +
        "}",
        "a = (x[--y]) ? 0 : 1;");

    test("if (x++) { x += 2 } else { x += 3 }",
         "x++ ? x += 2 : x += 3");

    test("if (x++) { x = x + 2 } else { x = x + 3 }",
        "x = x++ ? x + 2 : x + 3");
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
    testSame("var a = String('hello', bar());");
    testSame("var a = String({valueOf: function() { return 1; }});");
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
