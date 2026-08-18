// buggy code
  void tryMinimizeExits(Node n, int exitType, String labelName) {

    // Just an 'exit'.
    if (matchingExitNode(n, exitType, labelName)) {
      NodeUtil.removeChild(n.getParent(), n);
      compiler.reportCodeChange();
      return;
    }

    // Just an 'if'.
    if (n.isIf()) {
      Node ifBlock = n.getFirstChild().getNext();
      tryMinimizeExits(ifBlock, exitType, labelName);
      Node elseBlock = ifBlock.getNext();
      if (elseBlock != null) {
        tryMinimizeExits(elseBlock, exitType, labelName);
      }
      return;
    }

    // Just a 'try/catch/finally'.
    if (n.isTry()) {
      Node tryBlock = n.getFirstChild();
      tryMinimizeExits(tryBlock, exitType, labelName);
      Node allCatchNodes = NodeUtil.getCatchBlock(n);
      if (NodeUtil.hasCatchHandler(allCatchNodes)) {
        Preconditions.checkState(allCatchNodes.hasOneChild());
        Node catchNode = allCatchNodes.getFirstChild();
        Node catchCodeBlock = catchNode.getLastChild();
        tryMinimizeExits(catchCodeBlock, exitType, labelName);
      }
      /* Don't try to minimize the exits of finally blocks, as this
       * can cause problems if it changes the completion type of the finally
       * block. See ECMA 262 Sections 8.9 & 12.14
       */
      if (NodeUtil.hasFinally(n)) {
        Node finallyBlock = n.getLastChild();
        tryMinimizeExits(finallyBlock, exitType, labelName);
      }
    }

    // Just a 'label'.
    if (n.isLabel()) {
      Node labelBlock = n.getLastChild();
      tryMinimizeExits(labelBlock, exitType, labelName);
    }

    // TODO(johnlenz): The last case of SWITCH statement?

    // The rest assumes a block with at least one child, bail on anything else.
    if (!n.isBlock() || n.getLastChild() == null) {
      return;
    }

    // Multiple if-exits can be converted in a single pass.
    // Convert "if (blah) break;  if (blah2) break; other_stmt;" to
    // become "if (blah); else { if (blah2); else { other_stmt; } }"
    // which will get converted to "if (!blah && !blah2) { other_stmt; }".
    for (Node c : n.children()) {

      // An 'if' block to process below.
      if (c.isIf()) {
        Node ifTree = c;
        Node trueBlock, falseBlock;

        // First, the true condition block.
        trueBlock = ifTree.getFirstChild().getNext();
        falseBlock = trueBlock.getNext();
        tryMinimizeIfBlockExits(trueBlock, falseBlock,
            ifTree, exitType, labelName);

        // Now the else block.
        // The if blocks may have changed, get them again.
        trueBlock = ifTree.getFirstChild().getNext();
        falseBlock = trueBlock.getNext();
        if (falseBlock != null) {
          tryMinimizeIfBlockExits(falseBlock, trueBlock,
              ifTree, exitType, labelName);
        }
      }

      if (c == n.getLastChild()) {
        break;
      }
    }

    // Now try to minimize the exits of the last child, if it is removed
    // look at what has become the last child.
    for (Node c = n.getLastChild(); c != null; c = n.getLastChild()) {
      tryMinimizeExits(c, exitType, labelName);
      // If the node is still the last child, we are done.
      if (c == n.getLastChild()) {
        break;
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testInlineProperties
  public void testInlineProperties() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    level.setTypeBasedOptimizationOptions(options);

    String code = "" +
        "var ns = {};\n" +
        "\n" +
        "ns.C = function () {this.someProperty = 1}\n" +
        "alert(new ns.C().someProperty + new ns.C().someProperty);\n";
    assertTrue(options.inlineProperties);
    assertTrue(options.collapseProperties);
    
    test(options, code, "alert(2);");
  }

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

// com.google.javascript.jscomp.IntegrationTest::testIssue937
  public void testIssue937() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "console.log(" +
            " ((new x())['abc'])() );";
    String result = "" +
        "console.log((new x()).abc());";
    test(options, code, result);
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

// com.google.javascript.jscomp.IntegrationTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    String[] input1 = {"function f(z) { return z; }"};
    String[] input2 = {"function f(y) { return y; }"};
    CompilerOptions options = new CompilerOptions();
    Node out1 = parse(input1, options, false);
    Node out2 = parse(input2, options, false);
    assertFalse(out1.isEquivalentTo(out2));
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
    fold("function f(){ if (x) return; if (y) return; if (z) return; w(); }",
        " function f() {" +
        "   if (x) {} else { if (y) {} else { if (z) {} else w(); }}" +
        " }");

    fold("function f(){while(a())return;}",
         "function f(){while(a())return}");
    foldSame("function f(){for(x in a())return}");

    fold("function f(){while(a())break;}",
         "function f(){while(a())break}");
    foldSame("function f(){for(x in a())break}");

    fold("function f(){try{return;}catch(e){throw 9;}finally{return}}",
         "function f(){try{}catch(e){throw 9;}finally{return}}");
    foldSame("function f(){try{throw 9;}finally{return;}}");

    fold("function f(){try{return;}catch(e){return;}}",
         "function f(){try{}catch(e){}}");
    fold("function f(){try{if(a()){return;}else{return;} return;}catch(e){}}",
         "function f(){try{if(a()){}else{}}catch(e){}}");

    fold("function f(){g:return}",
         "function f(){}");
    fold("function f(){g:if(a()){return;}else{return;} return;}",
         "function f(){g:if(a()){}else{}}");
    fold("function f(){try{g:if(a()){throw 9;} return;}finally{return}}",
         "function f(){try{g:if(a()){throw 9;}}finally{return}}");
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

// com.google.javascript.jscomp.MinimizeExitPointsTest::testDontRemoveBreakInTryFinally
  public void testDontRemoveBreakInTryFinally() throws Exception {
    foldSame("function f() {b:try{throw 9} finally {break b} return 1;}");
  }
