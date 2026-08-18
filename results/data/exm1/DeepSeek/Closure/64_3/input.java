// buggy code
  public void toSource(final CodeBuilder cb,
                       final int inputSeqNum,
                       final Node root) {
    runInCompilerThread(new Callable<Void>() {
      public Void call() throws Exception {
        if (options.printInputDelimiter) {
          if ((cb.getLength() > 0) && !cb.endsWith("\n")) {
            cb.append("\n");  // Make sure that the label starts on a new line
          }
          Preconditions.checkState(root.getType() == Token.SCRIPT);

          String delimiter = options.inputDelimiter;

          String sourceName = (String)root.getProp(Node.SOURCENAME_PROP);
          Preconditions.checkState(sourceName != null);
          Preconditions.checkState(!sourceName.isEmpty());

          delimiter = delimiter.replaceAll("%name%", sourceName)
            .replaceAll("%num%", String.valueOf(inputSeqNum));

          cb.append(delimiter)
            .append("\n");
        }
        if (root.getJSDocInfo() != null &&
            root.getJSDocInfo().getLicense() != null) {
          cb.append("/*\n")
            .append(root.getJSDocInfo().getLicense())
            .append("*/\n");
        }

        // If there is a valid source map, then indicate to it that the current
        // root node's mappings are offset by the given string builder buffer.
        if (options.sourceMapOutputPath != null) {
          sourceMap.setStartingPosition(
              cb.getLineIndex(), cb.getColumnIndex());
        }

        // if LanguageMode is ECMASCRIPT5_STRICT, only print 'use strict'
        // for the first input file
        String code = toSource(root, sourceMap);
        if (!code.isEmpty()) {
          cb.append(code);

          // In order to avoid parse ambiguity when files are concatenated
          // together, all files should end in a semi-colon. Do a quick
          // heuristic check if there's an obvious semi-colon already there.
          int length = code.length();
          char lastChar = code.charAt(length - 1);
          char secondLastChar = length >= 2 ?
              code.charAt(length - 2) : '\0';
          boolean hasSemiColon = lastChar == ';' ||
              (lastChar == '\n' && secondLastChar == ';');
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }

  String toSource(Node n) {
    initCompilerOptionsIfTesting();
    return toSource(n, null);
  }

  private String toSource(Node n, SourceMap sourceMap) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(options.prettyPrint);
    builder.setLineBreak(options.lineBreak);
    builder.setSourceMap(sourceMap);
    builder.setSourceMapDetailLevel(options.sourceMapDetailLevel);
    builder.setTagAsStrict(
        options.getLanguageOut() == LanguageMode.ECMASCRIPT5_STRICT);
    builder.setLineLengthThreshold(options.lineLengthThreshold);

    Charset charset = options.outputCharset != null ?
        Charset.forName(options.outputCharset) : null;
    builder.setOutputCharset(charset);

    return builder.build();
  }

// relevant test
// com.google.javascript.jscomp.CommandLineRunnerTest::testMissingReturnCheckOnWithVerbose
  public void testMissingReturnCheckOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {f()} f();",
        CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGenerateExports
  public void testGenerateExports() {
    args.add("--generate_exports=true");
    test(" foo.prototype.x = function() {};",
        "foo.prototype.x=function(){};"+
        "goog.exportSymbol(\"foo.prototype.x\",foo.prototype.x);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDepreciationWithVerbose
  public void testDepreciationWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}; f()",
       CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTwoParseErrors
  public void testTwoParseErrors() {
    
    
    Compiler compiler = compile(new String[] {
      "var a b;",
      "var b c;"
    });
    assertEquals(2, compiler.getErrors().length);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES3ByDefault
  public void testES3ByDefault() {
    test("var x = f.function", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5
  public void testES5() {
    args.add("--language_in=ECMASCRIPT5");
    test("var x = f.function", "var x = f.function");
    test("var let", "var let");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5Strict
  public void testES5Strict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    test("var x = f.function", "'use strict';var x = f.function");
    test("var let", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrict
  public void testES5StrictUseStrict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrictMultipleInputs
  public void testES5StrictUseStrictMultipleInputs() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function",
        "var y = f.function", "var z = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
    assertEquals(outputSource.substring(13).indexOf("'use strict'"), -1);
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterResetDummy
  public void testCodeBuilderColumnAfterResetDummy() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("");
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterReset
  public void testCodeBuilderColumnAfterReset() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    String js = "foo();\ngoo();";
    cb.append(js);
    assertEquals(js, cb.toString());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.reset();

    assertTrue(cb.toString().isEmpty());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderAppend
  public void testCodeBuilderAppend() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo();");
    assertEquals(0, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.append("goo();");

    assertEquals(0, cb.getLineIndex());
    assertEquals(12, cb.getColumnIndex());

    
    cb.append("blah();\ngoo();");

    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCyclicalDependencyInInputs
  public void testCyclicalDependencyInInputs() {
    JSSourceFile[] inputs = {
        JSSourceFile.fromCode(
            "gin", "goog.provide('gin'); goog.require('tonic'); var gin = {};"),
        JSSourceFile.fromCode("tonic",
            "goog.provide('tonic'); goog.require('gin'); var tonic = {};"),
        JSSourceFile.fromCode(
            "mix", "goog.require('gin'); goog.require('tonic');")};
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    options.manageClosureDependencies = true;
    Compiler compiler = new Compiler();
    compiler.init(new JSSourceFile[0], inputs, options);
    compiler.parseInputs();
    assertEquals(compiler.externAndJsRoot, compiler.jsRoot.getParent());
    assertEquals(compiler.externAndJsRoot, compiler.externsRoot.getParent());
    assertNotNull(compiler.externAndJsRoot);
  }

// com.google.javascript.jscomp.CompilerTest::testLocalUndefined
  public void testLocalUndefined() throws Exception {
    
    
    
    
    
    
    
    
    CompilerOptions options = new CompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    Compiler compiler = new Compiler();
    JSSourceFile externs = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js",
        "(function (undefined) { alert(undefined); })();");
    compiler.compile(externs, input, options);
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition1
  public void testConstantDefinition1() {
    testSame("var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition2
  public void testConstantDefinition2() {
    testSame("var a$b$XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace1
  public void testConstantInitializedInAnonymousNamespace1() {
    testSame("var XYZ; (function(){ XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace2
  public void testConstantInitializedInAnonymousNamespace2() {
    testSame("var a$b$XYZ; (function(){ a$b$XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectModified
  public void testObjectModified() {
    testSame("var IE = true, XYZ = {a:1,b:1}; if (IE) XYZ['c'] = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectPropertyInitializedLate
  public void testObjectPropertyInitializedLate() {
    testSame("var XYZ = {}; for (var i = 0; i < 10; i++) { XYZ[i] = i; }");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectRedefined1
  public void testObjectRedefined1() {
    testError("var XYZ = {}; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined1
  public void testConstantRedefined1() {
    testError("var XYZ = 1; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined2
  public void testConstantRedefined2() {
    testError("var a$b$XYZ = 1; a$b$XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope1
  public void testConstantRedefinedInLocalScope1() {
    testError("var XYZ = 1; (function(){ XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope2
  public void testConstantRedefinedInLocalScope2() {
    testError("var a$b$XYZ = 1; (function(){ a$b$XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScopeOutOfOrder
  public void testConstantRedefinedInLocalScopeOutOfOrder() {
    testError("function f() { XYZ = 2; } var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented1
  public void testConstantPostIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented2
  public void testConstantPostIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented1
  public void testConstantPreIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented2
  public void testConstantPreIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented1
  public void testConstantPostDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented2
  public void testConstantPostDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented1
  public void testConstantPreDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented2
  public void testConstantPreDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment1
  public void testAbbreviatedArithmeticAssignment1() {
    testError("var XYZ = 1; XYZ += 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment2
  public void testAbbreviatedArithmeticAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ %= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment1
  public void testAbbreviatedBitAssignment1() {
    testError("var XYZ = 1; XYZ |= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment2
  public void testAbbreviatedBitAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ &= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment1
  public void testAbbreviatedShiftAssignment1() {
    testError("var XYZ = 1; XYZ >>= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment2
  public void testAbbreviatedShiftAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ <<= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstAnnotation
  public void testConstAnnotation() {
    testError(" var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstSuppression
  public void testConstSuppression() {
    testSame("\n" +
             " var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleStatements
  public void testSimpleStatements() {
    String src = "var a; a = a; a = a";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SCRIPT, Token.VAR, Branch.UNCOND);
    assertCrossEdge(cfg, Token.VAR, Token.EXPR_RESULT, Branch.UNCOND);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.EXPR_RESULT, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleIf
  public void testSimpleIf() {
    String src = "var x; if (x) { x() } else { x() };";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SCRIPT, Token.VAR, Branch.UNCOND);
    assertCrossEdge(cfg, Token.VAR, Token.IF, Branch.UNCOND);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertNoEdge(cfg, Token.EXPR_RESULT, Token.CALL);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_FALSE);
    assertReturnEdge(cfg, Token.EMPTY);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakingBlock
  public void testBreakingBlock() {
    
    String src = "X: { while(1) { break } }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertUpEdge(cfg, Token.BREAK, Token.BLOCK, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakingTryBlock
  public void testBreakingTryBlock() {
    String src = "a: try { break a; } finally {} if(x) {}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.IF, Branch.UNCOND);

    src = "a: try {} finally {break a;} if(x) {}";
    cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.IF, Branch.UNCOND);

    src = "a: try {} catch(e) {break a;} if(x) {}";
    cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.IF, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testWithStatement
  public void testWithStatement() {
    String src = "var x, y; with(x) { y() }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.WITH, Token.BLOCK, Branch.UNCOND);
    assertNoEdge(cfg, Token.WITH, Token.NAME);
    assertNoEdge(cfg, Token.NAME, Token.BLOCK);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertReturnEdge(cfg, Token.EXPR_RESULT);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleWhile
  public void testSimpleWhile() {
    String src = "var x; while (x) { x(); if (x) { break; } x() }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.WHILE, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_TRUE);
    assertReturnEdge(cfg, Token.BREAK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleSwitch
  public void testSimpleSwitch() {
    String src = "var x; switch(x){ case(1): x(); case('x'): x(); break" +
        "; default: x();}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.VAR, Token.SWITCH, Branch.UNCOND);
    assertNoEdge(cfg, Token.SWITCH, Token.NAME);
    
    assertDownEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND);
    assertCrossEdge(cfg, Token.CASE, Token.CASE, Branch.ON_FALSE);
    assertCrossEdge(cfg, Token.CASE, Token.DEFAULT, Branch.ON_FALSE);
    
    assertDownEdge(cfg, Token.CASE, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertNoEdge(cfg, Token.EXPR_RESULT, Token.CALL);
    assertNoEdge(cfg, Token.CALL, Token.NAME);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleNoDefault
  public void testSimpleNoDefault() {
    String src = "var x; switch(x){ case(1): break; } x();";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.CASE, Token.EXPR_RESULT, Branch.ON_FALSE);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSwitchDefaultFirst
  public void testSwitchDefaultFirst() {
    
    String src = "var x; switch(x){ default: break; case 1: break; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND);
    assertCrossEdge(cfg, Token.CASE, Token.DEFAULT, Branch.ON_FALSE);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSwitchDefaultInMiddle
  public void testSwitchDefaultInMiddle() {
    
    String src = "var x; switch(x){ case 1: break; default: break; " +
        "case 2: break; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND);
    assertCrossEdge(cfg, Token.CASE, Token.CASE, Branch.ON_FALSE);
    assertCrossEdge(cfg, Token.CASE, Token.DEFAULT, Branch.ON_FALSE);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSwitchEmpty
  public void testSwitchEmpty() {
    
    String src = "var x; switch(x){}; x()";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.SWITCH, Token.EMPTY, Branch.UNCOND);
    assertCrossEdge(cfg, Token.EMPTY, Token.EXPR_RESULT, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnThrowingException
  public void testReturnThrowingException() {
    String src = "function f() {try { return a(); } catch (e) {e()}}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.RETURN, Token.BLOCK, Branch.ON_EX);
    assertDownEdge(cfg, Token.BLOCK, Token.CATCH, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleFor
  public void testSimpleFor() {
    String src = "var a; for (var x = 0; x < 100; x++) { a(); }";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"VAR\"];\n" +
      "  node1 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FOR\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node4 -> node3 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node3 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 [label=\"LT\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"NAME\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NUMBER\"];\n" +
      "  node7 -> node9 [weight=1];\n" +
      "  node10 [label=\"INC\"];\n" +
      "  node4 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node10 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 [label=\"BLOCK\"];\n" +
      "  node4 -> node12 [weight=1];\n" +
      "  node13 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node14 [label=\"CALL\"];\n" +
      "  node13 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node13 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node12 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleForWithContinue
  public void testSimpleForWithContinue() {
    String src = "var a; for (var x = 0; x < 100; x++) {a();continue;a()}";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"VAR\"];\n" +
      "  node1 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FOR\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node4 -> node3 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node3 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 [label=\"LT\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"NAME\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NUMBER\"];\n" +
      "  node7 -> node9 [weight=1];\n" +
      "  node10 [label=\"INC\"];\n" +
      "  node4 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node10 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 [label=\"BLOCK\"];\n" +
      "  node4 -> node12 [weight=1];\n" +
      "  node13 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node14 [label=\"CALL\"];\n" +
      "  node13 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node16 [label=\"CONTINUE\"];\n" +
      "  node13 -> node16 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node16 [weight=1];\n" +
      "  node16 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node17 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node17 [weight=1];\n" +
      "  node18 [label=\"CALL\"];\n" +
      "  node17 -> node18 [weight=1];\n" +
      "  node19 [label=\"NAME\"];\n" +
      "  node18 -> node19 [weight=1];\n" +
      "  node17 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node12 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedFor
  public void testNestedFor() {
    
    String src = "var a,b;a();for(var x=0;x<100;x++){for(var y=0;y<100;y++){" +
      "continue;b();}}";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NAME\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"EXPR_RESULT\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"CALL\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"NAME\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"VAR\"];\n" +
      "  node4 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 [label=\"FOR\"];\n" +
      "  node0 -> node8 [weight=1];\n" +
      "  node8 -> node7 [weight=1];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node7 -> node9 [weight=1];\n" +
      "  node10 [label=\"NUMBER\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node11 [label=\"LT\"];\n" +
      "  node8 -> node11 [weight=1];\n" +
      "  node12 [label=\"NAME\"];\n" +
      "  node11 -> node12 [weight=1];\n" +
      "  node13 [label=\"NUMBER\"];\n" +
      "  node11 -> node13 [weight=1];\n" +
      "  node14 [label=\"INC\"];\n" +
      "  node8 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node14 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node16 [label=\"BLOCK\"];\n" +
      "  node8 -> node16 [weight=1];\n" +
      "  node17 [label=\"FOR\"];\n" +
      "  node16 -> node17 [weight=1];\n" +
      "  node18 [label=\"VAR\"];\n" +
      "  node17 -> node18 [weight=1];\n" +
      "  node19 [label=\"NAME\"];\n" +
      "  node18 -> node19 [weight=1];\n" +
      "  node20 [label=\"NUMBER\"];\n" +
      "  node19 -> node20 [weight=1];\n" +
      "  node18 -> node17 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node21 [label=\"LT\"];\n" +
      "  node17 -> node21 [weight=1];\n" +
      "  node22 [label=\"NAME\"];\n" +
      "  node21 -> node22 [weight=1];\n" +
      "  node23 [label=\"NUMBER\"];\n" +
      "  node21 -> node23 [weight=1];\n" +
      "  node24 [label=\"INC\"];\n" +
      "  node17 -> node24 [weight=1];\n" +
      "  node25 [label=\"NAME\"];\n" +
      "  node24 -> node25 [weight=1];\n" +
      "  node24 -> node17 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node26 [label=\"BLOCK\"];\n" +
      "  node17 -> node26 [weight=1];\n" +
      "  node27 [label=\"CONTINUE\"];\n" +
      "  node26 -> node27 [weight=1];\n" +
      "  node27 -> node24 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node28 [label=\"EXPR_RESULT\"];\n" +
      "  node26 -> node28 [weight=1];\n" +
      "  node29 [label=\"CALL\"];\n" +
      "  node28 -> node29 [weight=1];\n" +
      "  node30 [label=\"NAME\"];\n" +
      "  node29 -> node30 [weight=1];\n" +
      "  node28 -> node24 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node26 -> node27 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node17 -> node14 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node17 -> node26 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node16 -> node18 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> node16 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedDoWithBreak
  public void testNestedDoWithBreak() {
    
    String src = "var a;do{do{break}while(a);do{a()}while(a)}while(a);";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"BLOCK\"];\n" +
      "  node1 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"DO\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node4 -> node3 [weight=1];\n" +
      "  node5 [label=\"DO\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"BLOCK\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"BREAK\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node8 [label=\"BLOCK\"];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node6 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node5 -> node9 [weight=1];\n" +
      "  node5 -> node6 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node5 -> node8 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node10 [label=\"DO\"];\n" +
      "  node3 -> node10 [weight=1];\n" +
      "  node10 -> node8 [weight=1];\n" +
      "  node11 [label=\"EXPR_RESULT\"];\n" +
      "  node8 -> node11 [weight=1];\n" +
      "  node12 [label=\"CALL\"];\n" +
      "  node11 -> node12 [weight=1];\n" +
      "  node13 [label=\"NAME\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node11 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> node11 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node14 [label=\"NAME\"];\n" +
      "  node10 -> node14 [weight=1];\n" +
      "  node10 -> node4 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node10 -> node8 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node6 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node4 -> node15 [weight=1];\n" +
      "  node4 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node3 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testForIn
  public void testForIn() {
    String src = "var a,b;for(a in b){a()};";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NAME\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"FOR\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"NAME\"];\n" +
      "  node4 -> node6 [weight=1];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"CALL\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node8 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node11 [label=\"EMPTY\"];\n" +
      "  node4 -> node11 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node7 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node11 [weight=1];\n" +
      "  node11 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testThrow
  public void testThrow() {
    String src = "function f() { throw 1; f() }";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"FUNCTION\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"LP\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"BLOCK\"];\n" +
      "  node1 -> node4 [weight=1];\n" +
      "  node5 [label=\"THROW\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"EXPR_RESULT\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"CALL\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node7 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node5 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleFunction
  public void testSimpleFunction() {
    String src = "function f() { f() } f()";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"FUNCTION\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"LP\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"BLOCK\"];\n" +
      "  node1 -> node4 [weight=1];\n" +
      "  node5 [label=\"EXPR_RESULT\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"CALL\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"NAME\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node5 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node5 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node0 -> node8 [weight=1];\n" +
      "  node9 [label=\"CALL\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node8 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleCatch
  public void testSimpleCatch() {
    String src = "try{ throw x; x(); x['stuff']; x.x; x} catch (e) { e() }";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"THROW\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"NAME\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"BLOCK\"];\n"
        + "  node3 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node6 [label=\"EXPR_RESULT\"];\n"
        + "  node2 -> node6 [weight=1];\n"
        + "  node7 [label=\"CALL\"];\n"
        + "  node6 -> node7 [weight=1];\n"
        + "  node8 [label=\"NAME\"];\n"
        + "  node7 -> node8 [weight=1];\n"
        + "  node9 [label=\"EXPR_RESULT\"];\n"
        + "  node6 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node6 -> node9 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node9 [weight=1];\n"
        + "  node10 [label=\"GETELEM\"];\n"
        + "  node9 -> node10 [weight=1];\n"
        + "  node11 [label=\"NAME\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"STRING\"];\n"
        + "  node10 -> node12 [weight=1];\n"
        + "  node13 [label=\"EXPR_RESULT\"];\n"
        + "  node9 -> node13 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node13 [weight=1];\n"
        + "  node14 [label=\"GETPROP\"];\n"
        + "  node13 -> node14 [weight=1];\n"
        + "  node15 [label=\"NAME\"];\n"
        + "  node14 -> node15 [weight=1];\n"
        + "  node16 [label=\"STRING\"];\n"
        + "  node14 -> node16 [weight=1];\n"
        + "  node17 [label=\"EXPR_RESULT\"];\n"
        + "  node13 -> node17 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node13 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node17 [weight=1];\n"
        + "  node18 [label=\"NAME\"];\n"
        + "  node17 -> node18 [weight=1];\n"
        + "  node17 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node3 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node5 [weight=1];\n"
        + "  node19 [label=\"CATCH\"];\n"
        + "  node5 -> node19 [weight=1];\n"
        + "  node20 [label=\"NAME\"];\n"
        + "  node19 -> node20 [weight=1];\n"
        + "  node21 [label=\"BLOCK\"];\n"
        + "  node19 -> node21 [weight=1];\n"
        + "  node22 [label=\"EXPR_RESULT\"];\n"
        + "  node21 -> node22 [weight=1];\n"
        + "  node23 [label=\"CALL\"];\n"
        + "  node22 -> node23 [weight=1];\n"
        + "  node24 [label=\"NAME\"];\n"
        + "  node23 -> node24 [weight=1];\n"
        + "  node22 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node21 -> node22 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node19 -> node21 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node5 -> node19 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testFunctionWithinTry
  public void testFunctionWithinTry() {
    
    String src = "try { function f() {throw 1;} } catch (e) { }";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"FUNCTION\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"NAME\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"LP\"];\n"
        + "  node3 -> node5 [weight=1];\n"
        + "  node6 [label=\"BLOCK\"];\n"
        + "  node3 -> node6 [weight=1];\n"
        + "  node7 [label=\"THROW\"];\n"
        + "  node6 -> node7 [weight=1];\n"
        + "  node8 [label=\"NUMBER\"];\n"
        + "  node7 -> node8 [weight=1];\n"
        + "  node6 -> node7 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node6 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 [label=\"BLOCK\"];\n"
        + "  node1 -> node9 [weight=1];\n"
        + "  node10 [label=\"CATCH\"];\n"
        + "  node9 -> node10 [weight=1];\n"
        + "  node11 [label=\"NAME\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"BLOCK\"];\n"
        + "  node10 -> node12 [weight=1];\n"
        + "  node12 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node10 -> node12 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 -> node10 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedCatch
  public void testNestedCatch() {
    
    String src = "try{try{throw 1;}catch(e){throw 2}}catch(f){}";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"TRY\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"BLOCK\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"THROW\"];\n"
        + "  node4 -> node5 [weight=1];\n"
        + "  node6 [label=\"NUMBER\"];\n"
        + "  node5 -> node6 [weight=1];\n"
        + "  node7 [label=\"BLOCK\"];\n"
        + "  node5 -> node7 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node4 -> node5 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node7 [weight=1];\n"
        + "  node8 [label=\"CATCH\"];\n"
        + "  node7 -> node8 [weight=1];\n"
        + "  node9 [label=\"NAME\"];\n"
        + "  node8 -> node9 [weight=1];\n"
        + "  node10 [label=\"BLOCK\"];\n"
        + "  node8 -> node10 [weight=1];\n"
        + "  node11 [label=\"THROW\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"NUMBER\"];\n"
        + "  node11 -> node12 [weight=1];\n"
        + "  node13 [label=\"BLOCK\"];\n"
        + "  node11 -> node13 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node10 -> node11 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node8 -> node10 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node7 -> node8 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node4 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node3 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node13 [weight=1];\n"
        + "  node14 [label=\"CATCH\"];\n"
        + "  node13 -> node14 [weight=1];\n"
        + "  node15 [label=\"NAME\"];\n"
        + "  node14 -> node15 [weight=1];\n"
        + "  node16 [label=\"BLOCK\"];\n"
        + "  node14 -> node16 [weight=1];\n"
        + "  node16 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node14 -> node16 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node13 -> node14 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleFinally
  public void testSimpleFinally() {
    String src = "try{var x; foo()}finally{}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.TRY, Token.BLOCK, Branch.UNCOND);
    assertDownEdge(cfg, Token.BLOCK, Token.VAR, Branch.UNCOND);
    
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.BLOCK, Branch.UNCOND);
    
    assertNoEdge(cfg, Token.BLOCK, Token.BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleCatchFinally
  public void testSimpleCatchFinally() {
    
    String src = "try{ if(a){throw 1}else{a} } catch(e){a}finally{a}";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"IF\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"NAME\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"BLOCK\"];\n"
        + "  node3 -> node5 [weight=1];\n"
        + "  node6 [label=\"THROW\"];\n"
        + "  node5 -> node6 [weight=1];\n"
        + "  node7 [label=\"NUMBER\"];\n"
        + "  node6 -> node7 [weight=1];\n"
        + "  node8 [label=\"BLOCK\"];\n"
        + "  node6 -> node8 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node5 -> node6 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 [label=\"BLOCK\"];\n"
        + "  node3 -> node9 [weight=1];\n"
        + "  node10 [label=\"EXPR_RESULT\"];\n"
        + "  node9 -> node10 [weight=1];\n"
        + "  node11 [label=\"NAME\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"BLOCK\"];\n"
        + "  node10 -> node12 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 -> node10 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node5 [label=\"ON_TRUE\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node9 [label=\"ON_FALSE\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node3 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node8 [weight=1];\n"
        + "  node13 [label=\"CATCH\"];\n"
        + "  node8 -> node13 [weight=1];\n"
        + "  node14 [label=\"NAME\"];\n"
        + "  node13 -> node14 [weight=1];\n"
        + "  node15 [label=\"BLOCK\"];\n"
        + "  node13 -> node15 [weight=1];\n"
        + "  node16 [label=\"EXPR_RESULT\"];\n"
        + "  node15 -> node16 [weight=1];\n"
        + "  node17 [label=\"NAME\"];\n"
        + "  node16 -> node17 [weight=1];\n"
        + "  node16 -> node12 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node15 -> node16 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node13 -> node15 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node8 -> node13 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node12 [weight=1];\n"
        + "  node18 [label=\"EXPR_RESULT\"];\n"
        + "  node12 -> node18 [weight=1];\n"
        + "  node19 [label=\"NAME\"];\n"
        + "  node18 -> node19 [weight=1];\n"
        + "  node18 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node12 -> node18 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testComplicatedFinally2
  public void testComplicatedFinally2() {
    
    String src = "while(1){try{" +
      "if(a){a;continue;}else if(b){b;break;} else if(c) throw 1; else a}" +
      "catch(e){}finally{c()}bar}foo";

    ControlFlowGraph<Node> cfg = createCfg(src);
    
    assertCrossEdge(cfg, Token.CONTINUE, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.BREAK, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.THROW, Token.BLOCK, Branch.ON_EX);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testDeepNestedBreakwithFinally
  public void testDeepNestedBreakwithFinally() {
    String src = "X:while(1){try{while(2){try{var a;break X;}" +
        "finally{}}}finally{}}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.WHILE, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.TRY, Branch.UNCOND);
    assertDownEdge(cfg, Token.BLOCK, Token.VAR, Branch.UNCOND);
    
    assertCrossEdge(cfg, Token.BREAK, Token.BLOCK, Branch.UNCOND);
    
    assertCrossEdge(cfg, Token.BLOCK, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.WHILE, Token.BLOCK, Branch.ON_FALSE);
    assertReturnEdge(cfg, Token.BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testDeepNestedFinally
  public void testDeepNestedFinally() {
    String src = "try{try{try{throw 1}" +
        "finally{1;var a}}finally{2;if(a);}}finally{3;a()}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.THROW, Token.BLOCK, Branch.ON_EX);
    assertCrossEdge(cfg, Token.VAR, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.IF, Token.BLOCK, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturn
  public void testReturn() {
    String src = "function f() { return; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnInFinally
  public void testReturnInFinally() {
    String src = "function f(x){ try{} finally {return x;} }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnInFinally2
  public void testReturnInFinally2() {
    String src = "function f(x){" +
      " try{ try{}finally{var dummy; return x;} } finally {} }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.VAR, Token.RETURN, Branch.UNCOND);
    assertCrossEdge(cfg, Token.RETURN, Token.BLOCK, Branch.UNCOND);
    assertReturnEdge(cfg, Token.BLOCK);
    assertNoReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnInTry
  public void testReturnInTry() {
    String src = "function f(x){ try{x; return x()} finally {} var y;}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.RETURN, Branch.UNCOND);
    assertCrossEdge(cfg, Token.RETURN, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.BLOCK, Token.VAR, Branch.UNCOND);
    assertReturnEdge(cfg, Token.VAR);
    assertReturnEdge(cfg, Token.BLOCK);
    assertNoReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testOptionNotToTraverseFunctions
  public void testOptionNotToTraverseFunctions() {
    String src = "var x = 1; function f() { x = null; }";
    String expectedWhenNotTraversingFunctions = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NUMBER\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node1 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FUNCTION\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"LP\"];\n" +
      "  node4 -> node6 [weight=1];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"ASSIGN\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NULL\"];\n" +
      "  node9 -> node11 [weight=1];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NUMBER\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node1 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FUNCTION\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"LP\"];\n" +
      "  node4 -> node6 [weight=1];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"ASSIGN\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NULL\"];\n" +
      "  node9 -> node11 [weight=1];\n" +
      "  node8 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
    testCfg(src, expectedWhenNotTraversingFunctions, false);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testInstanceOf
  public void testInstanceOf() {
    String src = "try { x instanceof 'x' } catch (e) { }";
    ControlFlowGraph<Node> cfg = createCfg(src, true);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.BLOCK, Branch.ON_EX);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSynBlock
  public void testSynBlock() {
    String src = "START(); var x; END(); var y;";
    ControlFlowGraph<Node> cfg = createCfg(src, true);
    assertCrossEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.SYN_BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testPartialTraversalOfScope
  public void testPartialTraversalOfScope() {
    Compiler compiler = new Compiler();
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);

    Node script1 = compiler.parseSyntheticCode("cfgtest", "var foo;");
    Node script2 = compiler.parseSyntheticCode("cfgtest2", "var bar;");
    Node root = new Node(Token.BLOCK, script1, script2);

    cfa.process(null, script1);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    assertNotNull(cfg.getNode(script1));
    assertNull(cfg.getNode(script2));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testForLoopOrder
  public void testForLoopOrder() {
    assertNodeOrder(
        createCfg("for (var i = 0; i < 5; i++) { var x = 3; } if (true) {}"),
        Lists.newArrayList(
            Token.SCRIPT, Token.VAR, Token.FOR, Token.BLOCK, Token.VAR,
            Token.INC ,
            Token.IF, Token.BLOCK));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testLabelledForInLoopOrder
  public void testLabelledForInLoopOrder() {
    assertNodeOrder(
        createCfg("var i = 0; var y = {}; " +
            "label: for (var x in y) { " +
            "    if (x) { break label; } else { i++ } x(); }"),
        Lists.newArrayList(
            Token.SCRIPT, Token.VAR, Token.VAR,
            Token.FOR, Token.BLOCK,
            Token.IF, Token.BLOCK, Token.BREAK,
            Token.BLOCK, Token.EXPR_RESULT, Token.EXPR_RESULT));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testLocalFunctionOrder
  public void testLocalFunctionOrder() {
    ControlFlowGraph<Node> cfg =
        createCfg("function f() { while (x) { x++; } } var x = 3;");
    assertNodeOrder(
        cfg,
        Lists.newArrayList(
            Token.SCRIPT, Token.VAR,

            Token.FUNCTION, Token.BLOCK,
            Token.WHILE, Token.BLOCK, Token.EXPR_RESULT));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testDoWhileOrder
  public void testDoWhileOrder() {
    assertNodeOrder(
        createCfg("do { var x = 3; } while (true); void x;"),
        Lists.newArrayList(
            Token.SCRIPT, Token.BLOCK, Token.VAR, Token.DO, Token.EXPR_RESULT));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakInFinally1
  public void testBreakInFinally1() {
    String src =
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + 
        "};";
    String expected =
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"SCRIPT\"];\n" +
        "  node1 [label=\"EXPR_RESULT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node2 [label=\"ASSIGN\"];\n" +
        "  node1 -> node2 [weight=1];\n" +
        "  node3 [label=\"NAME\"];\n" +
        "  node2 -> node3 [weight=1];\n" +
        "  node4 [label=\"FUNCTION\"];\n" +
        "  node2 -> node4 [weight=1];\n" +
        "  node5 [label=\"NAME\"];\n" +
        "  node4 -> node5 [weight=1];\n" +
        "  node6 [label=\"LP\"];\n" +
        "  node4 -> node6 [weight=1];\n" +
        "  node7 [label=\"BLOCK\"];\n" +
        "  node4 -> node7 [weight=1];\n" +
        "  node8 [label=\"VAR\"];\n" +
        "  node7 -> node8 [weight=1];\n" +
        "  node9 [label=\"NAME\"];\n" +
        "  node8 -> node9 [weight=1];\n" +
        "  node10 [label=\"LABEL\"];\n" +
        "  node7 -> node10 [weight=1];\n" +
        "  node11 [label=\"LABEL_NAME\"];\n" +
        "  node10 -> node11 [weight=1];\n" +
        "  node12 [label=\"BLOCK\"];\n" +
        "  node10 -> node12 [weight=1];\n" +
        "  node13 [label=\"VAR\"];\n" +
        "  node12 -> node13 [weight=1];\n" +
        "  node14 [label=\"NAME\"];\n" +
        "  node13 -> node14 [weight=1];\n" +
        "  node15 [label=\"NULL\"];\n" +
        "  node14 -> node15 [weight=1];\n" +
        "  node16 [label=\"TRY\"];\n" +
        "  node12 -> node16 [weight=1];\n" +
        "  node17 [label=\"BLOCK\"];\n" +
        "  node16 -> node17 [weight=1];\n" +
        "  node18 [label=\"EXPR_RESULT\"];\n" +
        "  node17 -> node18 [weight=1];\n" +
        "  node19 [label=\"ASSIGN\"];\n" +
        "  node18 -> node19 [weight=1];\n" +
        "  node20 [label=\"NAME\"];\n" +
        "  node19 -> node20 [weight=1];\n" +
        "  node21 [label=\"NEW\"];\n" +
        "  node19 -> node21 [weight=1];\n" +
        "  node22 [label=\"NAME\"];\n" +
        "  node21 -> node22 [weight=1];\n" +
        "  node23 [label=\"BLOCK\"];\n" +
        "  node16 -> node23 [weight=1];\n" +
        "  node24 [label=\"BLOCK\"];\n" +
        "  node16 -> node24 [weight=1];\n" +
        "  node25 [label=\"EXPR_RESULT\"];\n" +
        "  node24 -> node25 [weight=1];\n" +
        "  node26 [label=\"ASSIGN\"];\n" +
        "  node25 -> node26 [weight=1];\n" +
        "  node27 [label=\"NAME\"];\n" +
        "  node26 -> node27 [weight=1];\n" +
        "  node28 [label=\"NAME\"];\n" +
        "  node26 -> node28 [weight=1];\n" +
        "  node29 [label=\"BREAK\"];\n" +
        "  node24 -> node29 [weight=1];\n" +
        "  node30 [label=\"LABEL_NAME\"];\n" +
        "  node29 -> node30 [weight=1];\n" +
        "  node31 [label=\"EXPR_RESULT\"];\n" +
        "  node7 -> node31 [weight=1];\n" +
        "  node32 [label=\"CALL\"];\n" +
        "  node31 -> node32 [weight=1];\n" +
        "  node33 [label=\"NAME\"];\n" +
        "  node32 -> node33 [weight=1];\n" +
        "  node34 [label=\"NAME\"];\n" +
        "  node32 -> node34 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakInFinally2
  public void testBreakInFinally2() {
    String src =
      "var action;\n" +
      "a: {\n" +
      "  var proto = null;\n" +
      "  try {\n" +
      "    proto = new Proto\n" +
      "  } finally {\n" +
      "    action = proto;\n" +
      "    break a\n" +
      "  }\n" +
      "}\n" +
      "alert(action)\n";

    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.EXPR_RESULT, Branch.UNCOND);
    assertNoEdge(cfg, Token.BREAK, Token.BLOCK);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testWhile
  public void testWhile() {
    assertNoError("while(1) { break; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testNextedWhile
  public void testNextedWhile() {
    assertNoError("while(1) { while(1) { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreak
  public void testBreak() {
    assertInvalidBreak("break;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinue
  public void testContinue() {
    assertInvalidContinue("continue;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunction
  public void testBreakCrossFunction() {
    assertInvalidBreak("while(1) { function f() { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunctionInFor
  public void testBreakCrossFunctionInFor() {
    assertInvalidBreak("while(1) {for(var f = function () { break; };;) {}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitch
  public void testContinueToSwitch() {
    assertInvalidContinue("switch(1) {case(1): continue; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithNoCases
  public void testContinueToSwitchWithNoCases() {
    assertNoError("switch(1){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithTwoCases
  public void testContinueToSwitchWithTwoCases() {
    assertInvalidContinue("switch(1){case(1):break;case(2):continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithDefault
  public void testContinueToSwitchWithDefault() {
    assertInvalidContinue("switch(1){case(1):break;case(2):default:continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToLabelSwitch
  public void testContinueToLabelSwitch() {
    assertInvalidLabeledContinue(
        "while(1) {a: switch(1) {case(1): continue a; }}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueOutsideSwitch
  public void testContinueOutsideSwitch() {
    assertNoError("b: while(1) { a: switch(1) { case(1): continue b; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction1
  public void testContinueNotCrossFunction1() {
    assertNoError("a:switch(1){case(1):function f(){a:while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction2
  public void testContinueNotCrossFunction2() {
    assertUndefinedLabel(
        "a:switch(1){case(1):function f(){while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith1
  public void testUseOfWith1() {
    testSame("with(a){}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith2
  public void testUseOfWith2() {
    testSame("" +
             "with(a){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith3
  public void testUseOfWith3() {
    testSame(
        "function f(expr, context) {\n" +
        "  try {\n" +
        "     with (context) {\n" +
        "      return eval('[' + expr + '][0]');\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    return null;\n" +
        "  }\n" +
        "};\n");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testConvert
  public void testConvert() {
    test("a['p']", "a.p");
    test("a['_p_']", "a._p_");
    test("a['_']", "a._");
    test("a['$']", "a.$");
    test("a.b.c['p']", "a.b.c.p");
    test("a.b['c'].p", "a.b.c.p");
    test("a['p']();", "a.p();");
    test("a()['p']", "a().p");
    
    test("a['\u0041A']", "a.AA");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testDoNotConvert
  public void testDoNotConvert() {
    testSame("a[0]");
    testSame("a['']");
    testSame("a[' ']");
    testSame("a[',']");
    testSame("a[';']");
    testSame("a[':']");
    testSame("a['.']");
    testSame("a['0']");
    testSame("a['p ']");
    testSame("a['p' + '']");
    testSame("a[p]");
    testSame("a[P]");
    testSame("a[$]");
    testSame("a[p()]");
    testSame("a['default']");
    
    
    test("a['\u1d17A']", "a['\u1d17A']");
    
    
    test("a['\u00d1StuffAfter']", "a['\u00d1StuffAfter']");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testQuotedProps
  public void testQuotedProps() {
    testSame("({'':0})");
    testSame("({'1.0':0})");
    testSame("({'\u1d17A':0})");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function f() { if (x) return; y(); }",
         "function f(){x||y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function f(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1a
  public void testFoldWithMarkers1a() {
    testSame("function f(){startMarker();if(x)return;endMarker()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function f() { if (x) return; y(); if (a) return; b(); }",
         "function f(){if(!x){y();a||b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function f(){startMarker(\"FOO\");startMarker(\"BAR\");" +
             "if(x)return;endMarker(\"BAR\");y();if(a)return;" +
             "endMarker(\"FOO\");b()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedStartMarker
  public void testUnmatchedStartMarker() {
    testSame("startMarker()", CreateSyntheticBlocks.UNMATCHED_START_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker1
  public void testUnmatchedEndMarker1() {
    testSame("endMarker()", CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker2
  public void testUnmatchedEndMarker2() {
    test("if(y){startMarker();x()}endMarker()",
        "if(y){startMarker();x()}endMarker()", null,
         CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid1
  public void testInvalid1() {
    test("startMarker() && true",
        "startMarker()", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid2
  public void testInvalid2() {
    test("false && endMarker()",
        "", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testDenormalize
  public void testDenormalize() {
    testSame("startMarker();for(;;);endMarker()");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testNonMarkingUse
  public void testNonMarkingUse() {
    testSame("function foo(endMarker){}");
    testSame("function foo(){startMarker:foo()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testContainingBlockPreservation
  public void testContainingBlockPreservation() {
    testSame("if(y){startMarker();x();endMarker()}");
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement1
  public void testFunctionMovement1() {
    
    
    
    
    
    
    

    JSModule[] modules = createModuleStar(
      
      "function f1(a) { alert(a); }" +
      "function f2(a) { alert(a); }" +
      "function f3(a) { alert(a); }" +
      "function f4() { alert(1); }" +
      "function g() { alert('ciao'); }",
      
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "f2('hi'); f2('hi'); f3('bye');");

    test(modules, new String[] {
      
      "function f3(a) { alert(a); }" +
      "function g() { alert('ciao'); }",
      
      "function f4() { alert(1); }" +
      "function f1(a) { alert(a); }" +
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "function f2(a) { alert(a); }" +
      "f2('hi'); f2('hi'); f3('bye');",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement2
  public void testFunctionMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g() {var f = 1; f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g() {var f = 1; f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement3
  public void testFunctionMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g(f) {f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g(f) {f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement4
  public void testFunctionMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(){return function(a){}}",
      
      "var a = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return function(a){}}" +
      "var a = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5
  public void testFunctionMovement5() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(n){return (n<1)?1:f(n-1)}",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(n){return (n<1)?1:f(n-1)}" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5b
  public void testFunctionMovement5b() {
    
    JSModule[] modules = createModuleStar(
      
      "var f = function(n){return (n<1)?1:f(n-1)};",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "var f = function(n){return (n<1)?1:f(n-1)};" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement6
  public void testFunctionMovement6() {
    
    JSModule[] modules = createModuleChain(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}" +
      "var a = f();",
      
      "var b = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement7
  public void testFunctionMovement7() {
    
    JSModule[] modules = createModules(
      
      "function f(){return 1}",
      
      "",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();"
    );

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    modules[4].addDependency(modules[1]);

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement8
  public void testFunctionMovement8() {
    
    JSModule[] modules = createModuleChain(
      
      "var v = function f(){return 1}",
      
      "v();"
    );

    test(modules, new String[] {
      
      "",
      
      "var v = function f(){return 1};" +
      "v();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement1
  public void testFunctionNonMovement1() {
    
    
    
    
    
    testSame(createModuleStar(
      
      "function f(){};f.prototype.bar=new f;" +
      "if(a)function f2(){}" +
      "{{while(a)function f3(){}}}",
      
      "var a = new f();f2();f3();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement2
  public void testFunctionNonMovement2() {
    
    
    testSame(createModuleStar(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement1
  public void testClassMovement1() {
    test(createModuleStar(
             
             "function f(){} f.prototype.bar=function (){};",
             
             "var a = new f();"),
         new String[] {
           "",
           "function f(){} f.prototype.bar=function (){};" +
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement2
  public void testClassMovement2() {
    
    test(createModuleChain(
             
             "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
             
             "f.prototype.baq = 7;",
             
             "f.prototype.baz = 9;",
             
             "var a = new f();"),
         new String[] {
           
           "",
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;" +
           "f.prototype.baq = 7;" +
           "f.prototype.baz = 9;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement3
  public void testClassMovement3() {
    
    test(createModuleChain(
             
             "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;",
             
             "f = 7;",
             
             "f = 9;",
             
             "f = 11;"),
         new String[] {
           
           "",
           
           "",
           
           "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;" +
           "f = 7;" +
           "f = 9;",
           
           "f = 11;"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement4
  public void testClassMovement4() {
    testSame(createModuleStar(
                 
                 "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
                 
                 "f.prototype.baq = 7;",
                 
                 "var a = new f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement5
  public void testClassMovement5() {
    JSModule[] modules = createModules(
        
        "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
        
        "",
        
        "f.prototype.baq = 7;",
        
        "var a = new f();");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);

    test(modules,
         new String[] {
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
           
           "f.prototype.baq = 7;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement6
  public void testClassMovement6() {
    test(createModuleChain(
             
             "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
             "new Foo();",
             
             "new Bar();"),
         new String[] {
           
           "function Foo(){} new Foo();",
           
           "function Bar(){} goog.inherits(Bar, Foo); new Bar();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement7
  public void testClassMovement7() {
    testSame(createModuleChain(
                 
                 "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
                 "new Bar();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement1
  public void testStubMethodMovement1() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_stubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_stubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement2
  public void testStubMethodMovement2() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_unstubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_unstubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoMoveSideEffectProperty
  public void testNoMoveSideEffectProperty() {
    testSame(createModuleChain(
                 
                 "function Foo(){} " +
                 "Foo.prototype.bar = createSomething();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testAssignMovement
  public void testAssignMovement() {
    test(createModuleChain(
             
             "var f = 3;" +
             "f = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = 3;" +
          "f = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = 3;" +
                 "var g = f = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoClassMovement2
  public void testNoClassMovement2() {
    test(createModuleChain(
             
             "var f = {};" +
             "f.h = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {};" +
          "f.h = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = {};" +
                 "var g = f.h = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement1
  public void testLiteralMovement1() {
    test(createModuleChain(
             
             "var f = {'hi': 'mom', 'bye': function() {}};",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {'hi': 'mom', 'bye': function() {}};" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement2
  public void testLiteralMovement2() {
    testSame(createModuleChain(
                 
                 "var f = {'hi': 'mom', 'bye': goog.nullFunction};",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement3
  public void testLiteralMovement3() {
    test(createModuleChain(
             
             "var f = ['hi', function() {}];",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = ['hi', function() {}];" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement4
  public void testLiteralMovement4() {
    testSame(createModuleChain(
                 
                 "var f = ['hi', goog.nullFunction];",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement1
  public void testVarMovement1() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0;",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = 0;" +
      "var x = a;",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement2
  public void testVarMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0; var b = 1; var c = 2;",
      
      "var x = b;"
    );

    test(modules, new String[] {
      
      "var a = 0; var c = 2;",
      
      "var b = 1;" +
      "var x = b;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement3
  public void testVarMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0; var b = 1;",
      
      "var x = a + b;"
    );

    test(modules, new String[] {
      
      "",
      
      "var b = 1;" +
      "var a = 0;" +
      "var x = a + b;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement4
  public void testVarMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = function(){alert(1)};",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = function(){alert(1)};" +
      "var x = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement5
  public void testVarMovement5() {
    
    testSame(createModuleStar(
      
      "var a = alert;",
      
      "var x = a;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement6
  public void testVarMovement6() {
    
    JSModule[] modules = createModuleStar(
      
      "var a;",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a;" +
      "var x = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement7
  public void testVarMovement7() {
    
    testSame(createModuleStar(
      
      "function f() {g();}",
      
      "function g(){};"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement8
  public void testVarMovement8() {
    JSModule[] modules = createModuleBush(
      
      "var a = 0;",
      
      "",
      
      "var x = a;",
      
      "var y = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = 0;",
      
      "var x = a;",
      
      "var y = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement9
  public void testVarMovement9() {
    JSModule[] modules = createModuleTree(
      
      "var a = 0; var b = 1; var c = 3;",
      
      "",
      
      "",
      
      "a;",
      
      "a;c;",
      
      "b;",
      
      "b;c;"
    );

    test(modules, new String[] {
      
      "var c = 3;",
      
      "var a = 0;",
      
      "var b = 1;",
      
      "a;",
      
      "a;c;",
      
      "b;",
      
      "b;c;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClone1
  public void testClone1() {
    test(createModuleChain(
             
             "function f(){} f.prototype.clone = function() { return new f };",
             
             "var a = (new f).clone();"),
         new String[] {
           
           "",
           "function f(){} f.prototype.clone = function() { return new f() };" +
           
           "var a = (new f).clone();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClone2
  public void testClone2() {
    test(createModuleChain(
             
             "function f(){}" +
             "f.prototype.cloneFun = function() {" +
             "  return function() {new f}" +
             "};",
             
             "var a = (new f).cloneFun();"),
         new String[] {
           
           "",
           "function f(){}" +
           "f.prototype.cloneFun = function() {" +
           "  return function() {new f}" +
           "};" +
           
           "var a = (new f).cloneFun();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testBug4118005
  public void testBug4118005() {
    testSame(createModuleChain(
             
             "var m = 1;\n" +
             "(function () {\n" +
             " var x = 1;\n" +
             " m = function() { return x };\n" +
             "})();\n",
             
             "m();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testEmptyModule
  public void testEmptyModule() {
    
    
    
    
    
    
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1", "function x() {}"));

    JSModule empty = new JSModule("empty");
    empty.addDependency(m1);

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2", "x()"));
    m2.addDependency(empty);

    JSModule m3 = new JSModule("m3");
    m3.add(JSSourceFile.fromCode("m3", "x()"));
    m3.addDependency(empty);

    test(new JSModule[] {m1,empty,m2,m3},
        new String[] {
          "",
          "function x() {}",
          "x()",
          "x()"
    });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod1
  public void testMovePrototypeMethod1() {
    testSame(createModuleChain(
                 "function Foo() {}" +
                 "Foo.prototype.bar = function() {};",
                 
                 "(new Foo).bar()"));

    canMoveExterns = true;
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.bar = function() {};",
             
             "(new Foo).bar()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.bar = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.bar = JSCompiler_unstubMethod(0, function() {});" +
             "(new Foo).bar()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod2
  public void testMovePrototypeMethod2() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype = { method: function() {} };",
             
             "(new Foo).method()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype = { method: JSCompiler_stubMethod(0) };",
             
             "Foo.prototype.method = " +
             "    JSCompiler_unstubMethod(0, function() {});" +
             "(new Foo).method()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod3
  public void testMovePrototypeMethod3() {
    testSame(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype = { get method() {} };",
             
             "(new Foo).method()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeRecursiveMethod
  public void testMovePrototypeRecursiveMethod() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() { this.baz(); };",
             
             "(new Foo).baz()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, " +
             "    function() { this.baz(); });" +
             "(new Foo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testCantMovePrototypeProp
  public void testCantMovePrototypeProp() {
    testSame(createModuleChain(
                 "function Foo() {}" +
                 "Foo.prototype.baz = goog.nullFunction;",
                 
                 "(new Foo).baz()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsInRightOrder
  public void testMoveMethodsInRightOrder() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() { return 1; };" +
             "Foo.prototype.baz = function() { return 2; };",
             
             "(new Foo).baz()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(1);" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(1, function() { return 1; });" +
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(0, function() { return 2; });" +
             "(new Foo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsInRightOrder2
  public void testMoveMethodsInRightOrder2() {
    JSModule[] m = createModules(
        "function Foo() {}" +
        "Foo.prototype.baz = function() { return 1; };" +
        "function Goo() {}" +
        "Goo.prototype.baz = function() { return 2; };",
        
        "",
        
        "(new Foo).baz()",
        
        "",
        
        "(new Goo).baz()");

    m[1].addDependency(m[0]);
    m[2].addDependency(m[1]);
    m[3].addDependency(m[2]);
    m[4].addDependency(m[2]);

    test(m,
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(1);" +
             "function Goo() {}" +
             "Goo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "",
             
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(1, function() { return 1; });" +
             "Goo.prototype.baz = " +
             "JSCompiler_unstubMethod(0, function() { return 2; });" +
             "(new Foo).baz()",
             
             "",
             
             "(new Goo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsUsedInTwoModules
  public void testMoveMethodsUsedInTwoModules() {
    testSame(createModuleStar(
                 "function Foo() {}" +
                 "Foo.prototype.baz = function() {};",
                 
                 "(new Foo).baz()",
                 
                 "(new Foo).baz()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsUsedInTwoModules2
  public void testMoveMethodsUsedInTwoModules2() {
    JSModule[] modules = createModules(
        "function Foo() {}" +
        "Foo.prototype.baz = function() {};",
        
        "", 
        
        "(new Foo).baz() + 1",
        
        "(new Foo).baz() + 2");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    test(modules,
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});",
             
             "(new Foo).baz() + 1",
             
             "(new Foo).baz() + 2"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testTwoMethods
  public void testTwoMethods() {}

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testTwoMethods2
  public void testTwoMethods2() {
    
    
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() {};",
             
             "(new Foo).callBaz()",
             
             "Foo.prototype.callBaz = function() { this.baz(); }"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "(new Foo).callBaz()",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});" +
             "Foo.prototype.callBaz = function() { this.baz(); };"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testGlobalFunctionsInGraph
  public void testGlobalFunctionsInGraph() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function() {};" +
            "function x() { return (new Foo).baz(); }",
            
            "x();"),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {}" +
          "Foo.prototype.baz = JSCompiler_stubMethod(0);" +
          "function x() { return (new Foo).baz(); }",
          
          "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});" +
          "x();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads1
  public void testClosureVariableReads1() {
    testSame(createModuleChain(
            "function Foo() {}" +
            "(function() {" +
            "var x = 'x';" +
            "Foo.prototype.baz = function() {x};" +
            "})();",
            
            "var y = new Foo(); y.baz();"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads2
  public void testClosureVariableReads2() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.b1 = function() {" +
            "  var x = 1;" +
            "  Foo.prototype.b2 = function() {" +
            "    Foo.prototype.b3 = function() {" +
            "      x;" +
            "    }" +
            "  }" +
            "};",
            
            "var y = new Foo(); y.b1();",
            
            "y = new Foo(); z.b2();",
            
            "y = new Foo(); z.b3();"
            ),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.b1 = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.b1 = JSCompiler_unstubMethod(0, function() {" +
           "  var x = 1;" +
           "  Foo.prototype.b2 = function() {" +
           "    Foo.prototype.b3 = function() {" +
           "      x;" +
           "    }" +
           "  }" +
           "});" +
           "var y = new Foo(); y.b1();",
           
           "y = new Foo(); z.b2();",
           
           "y = new Foo(); z.b3();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads3
  public void testClosureVariableReads3() {}

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testNoClosureVariableReads1
  public void testNoClosureVariableReads1() {
    test(createModuleChain(
            "function Foo() {}" +
            "var x = 'x';" +
            "Foo.prototype.baz = function(){x};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "var x = 'x';" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(0, function(){x});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testNoClosureVariableReads2
  public void testNoClosureVariableReads2() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function(){var x = 1;x};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(" +
           "    0, function(){var x = 1; x});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testInnerFunctionClosureVariableReads
  public void testInnerFunctionClosureVariableReads() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function(){var x = 1;" +
            "  return function(){x}};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(" +
           "    0, function(){var x = 1; return function(){x}});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testSimple
  public void testSimple() {
    inFunction("var a; a=1", "var a; 1");
    inFunction("var a; a=1+1", "var a; 1+1");
    inFunction("var a; a=foo();", "var a; foo()");
    inFunction("a=1; var a; a=foo();", "1; var a; foo();");
    
    
    inFunction("var a; a=function f(){}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testLoops
  public void testLoops() {
    inFunction("for(var a=0; a<10; a++) {}");
    inFunction("var x; for(var a=0; a<10; a++) {x=a}; a(x)");
    inFunction("var x; for(var a=0; x=a<10; a++) {}",
        "var x; for(var a=0; a<10; a++) {}");
    inFunction("var x; for(var a=0; a<10; x=a) {}",
        "var x; for(var a=0; a<10; a) {}");
    inFunction("var x; for(var a=0; a<10; x=a,a++) {}",
        "var x; for(var a=0; a<10; a,a++) {}");
    inFunction("var x; for(var a=0; a<10; a++,x=a) {}",
        "var x; for(var a=0; a<10; a++,a) {}");
    inFunction("var x;for(var a=0; a<10; a++) {x=1}",
        "var x;for(var a=0; a<10; a++) {1}");
    inFunction("var x; x=1; do{x=2}while(0); x",
        "var x; 1; do{x=2}while(0); x");
    inFunction("var x; x=1; while(1){x=2}; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testMultiPaths
  public void testMultiPaths() {
    inFunction("var x,y; if(x)y=1;", "var x,y; if(x)1;");
    inFunction("var x,y; if(x)y=1; y=2; x(y)", "var x,y; if(x)1; y=2; x(y)");
    inFunction("var x; switch(x) { case(1): x=1; break; } x");
    inFunction("var x; switch(x) { case(1): x=1; break; }",
        "var x; switch(x) { case(1): 1; break; }");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testUsedAsConditions
  public void testUsedAsConditions() {
    inFunction("var x; while(x=1){}", "var x; while(1){}");
    inFunction("var x; if(x=1){}", "var x; if(1){}");
    inFunction("var x; do{}while(x=1)", "var x; do{}while(1)");
    inFunction("var x; if(x=1==4&&1){}", "var x; if(1==4&&1) {}");
    inFunction("var x; if(0&&(x=1)){}", "var x; if(0&&1){}");
    inFunction("var x; if((x=2)&&(x=1)){}", "var x; if(2&&1){}");
    inFunction("var x; x=2; if(0&&(x=1)){}; x");

    inFunction("var x,y; if( (x=1)+(y=2) > 3){}",
        "var x,y; if( 1+2 > 3){}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testUsedAsConditionsInSwitchStatements
  public void testUsedAsConditionsInSwitchStatements() {
    inFunction("var x; switch(x=1){}","var x; switch(1){}");
    inFunction("var x; switch(x){case(x=1):break;}",
        "var x; switch(x){case(1):break;}");

    inFunction("var x,y; switch(y) { case (x += 1): break; case (x): break;}");

    inFunction("var x,y; switch(y) { case (x = 1): break; case (2): break;}",
               "var x,y; switch(y) { case (1): break; case (2): break;}");
    inFunction("var x,y; switch(y) { case (x+=1): break; case (x=2): break;}",
               "var x,y; switch(y) { case (x+1): break; case (2): break;}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentInReturn
  public void testAssignmentInReturn() {
    inFunction("var x; return x = 1;", "var x; return 1");
    inFunction("var x; return");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentSamples
  public void testAssignmentSamples() {
    
    inFunction("var x = 2;");
    inFunction("var x = 2; x++;", "var x=2; void 0");
    inFunction("var x; x=x++;", "var x;x++");
    inFunction("var x; x+=1;", "var x;x+1");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentInArgs
  public void testAssignmentInArgs() {
    inFunction("var x; foo(x = 1);", "var x; foo(1);");
    inFunction("var x; return foo(x = 1);", "var x; return foo(1);");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignAndReadInCondition
  public void testAssignAndReadInCondition() {
    inFunction("var a, b; if ((a = 1) && (b = a)) {b}");
    inFunction("var a, b; if ((b = a) && (a = 1)) {b}",
               "var a, b; if ((b = a) && (1)) {b}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testParameters
  public void testParameters() {
    inFunction("param1=1; param1=2; param2(param1)",
        "1; param1=2; param2(param1)");
    inFunction("param1=param2()", "param2()");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testErrorHandling
  public void testErrorHandling() {
    inFunction("var x; try{ x=1 } catch(e){ x=2 }; x");
    inFunction("var x; try{ x=1 } catch(e){ x=2 }",
        "var x;try{ 1 } catch(e) { 2 }");
    inFunction("var x; try{ x=1 } finally { x=2 }; x",
        "var x;try{ 1 } finally{ x=2 }; x");
    inFunction("var x; while(1) { try{x=1;break}finally{x} }");
    inFunction("var x; try{throw 1} catch(e){x=2} finally{x}");
    inFunction("var x; try{x=1;throw 1;x} finally{x=2}; x",
        "var x; try{1;throw 1;x} finally{x=2}; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadVarDeclarations
  public void testDeadVarDeclarations() {
    
    inFunction("var x=1;");
    inFunction("var x=1; x=2; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testGlobal
  public void testGlobal() {
    
    test("var x; x=1; x=2; x=3;", "var x; x=1; x=2; x=3;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInnerFunctions
  public void testInnerFunctions() {
    inFunction("var x = function() { var x; x=1; }",
        "var x = function() { var x; 1; }");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInnerFunctions2
  public void testInnerFunctions2() {
    
    inFunction("var x = 0; print(x); x = 1; var y = function(){}; y()");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testSelfReAssignment
  public void testSelfReAssignment() {
    inFunction("var x; x = x;", "var x; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testSelfIncrement
  public void testSelfIncrement() {
    inFunction("var x; x = x + 1;", "var x; x + 1");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOp
  public void testAssignmentOp() {
    
    inFunction("var x; x += foo()", "var x; x + foo()");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOpUsedAsLhs
  public void testAssignmentOpUsedAsLhs() {
    inFunction("var x,y; y = x += foo(); print(y)",
               "var x,y; y = x +  foo(); print(y)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOpUsedAsCondition
  public void testAssignmentOpUsedAsCondition() {
    inFunction("var x; if(x += foo()) {}",
               "var x; if(x +  foo()) {}");

    inFunction("var x; if((x += foo()) > 1) {}",
               "var x; if((x +  foo()) > 1) {}");

    
    inFunction("var x; while((x += foo()) > 1) {}");

    inFunction("var x; for(;--x;){}");
    inFunction("var x; for(;x--;){}");
    inFunction("var x; for(;x -= 1;){}");
    inFunction("var x; for(;x = 0;){}", "var x; for(;0;){}");

    inFunction("var x; for(;;--x){}");
    inFunction("var x; for(;;x--){}");
    inFunction("var x; for(;;x -= 1){}");
    inFunction("var x; for(;;x = 0){}", "var x; for(;;0){}");

    inFunction("var x; for(--x;;){}", "var x; for(;;){}");
    inFunction("var x; for(x--;;){}", "var x; for(;;){}");
    inFunction("var x; for(x -= 1;;){}", "var x; for(x - 1;;){}");
    inFunction("var x; for(x = 0;;){}", "var x; for(0;;){}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadIncrement
  public void testDeadIncrement() {
    
    inFunction("var x; x ++", "var x; void 0");
    inFunction("var x; x --", "var x; void 0");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadButAlivePartiallyWithinTheExpression
  public void testDeadButAlivePartiallyWithinTheExpression() {
    inFunction("var x; x = 100, print(x), x = 101;",
               "var x; x = 100, print(x),     101;");
    inFunction("var x; x = 100, print(x), print(x), x = 101;",
               "var x; x = 100, print(x), print(x),     101;");
    inFunction("var x; x = 100, print(x), x = 0, print(x), x = 101;",
               "var x; x = 100, print(x), x = 0, print(x),     101;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testMutipleDeadAssignmentsButAlivePartiallyWithinTheExpression
  public void testMutipleDeadAssignmentsButAlivePartiallyWithinTheExpression() {
    inFunction("var x; x = 1, x = 2, x = 3, x = 4, x = 5," +
               "  print(x), x = 0, print(x), x = 101;",

               "var x; 1, 2, 3, 4, x = 5, print(x), x = 0, print(x), 101;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadPartiallyWithinTheExpression
  public void testDeadPartiallyWithinTheExpression() {
    
    
    inFunction("var x; x = 100, x = 101; print(x);");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentChain
  public void testAssignmentChain() {
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1",
               "var a,b,c,d,e; 1");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(c)",
               "var a,b,c,d,e;         c = 1        ; print(c)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(a + e)",
               "var a,b,c,d,e; a =             e = 1; print(a + e)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(b + d)",
               "var a,b,c,d,e;     b =     d     = 1; print(b + d)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(a + b + d + e)",
               "var a,b,c,d,e; a = b =     d = e = 1; print(a + b + d + e)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(a+b+c+d+e)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOpChain
  public void testAssignmentOpChain() {
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1",
               "var a,b,c,d,e;         c + 1");
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1;  print(e)",
               "var a,b,c,d,e;         c +     (e = 1); print(e)");
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1;  print(d)",
               "var a,b,c,d,e;         c + (d = 1)  ;   print(d)");
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1;  print(a)",
               "var a,b,c,d,e; a =     c +          1;  print(a)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIncDecInSubExpressions
  public void testIncDecInSubExpressions() {
    inFunction("var a; a = 1, a++; a");
    inFunction("var a; a = 1, ++a; a");
    inFunction("var a; a = 1, a--; a");
    inFunction("var a; a = 1, --a; a");

    inFunction("var a; a = 1, a++, print(a)");
    inFunction("var a; a = 1, ++a, print(a)");
    inFunction("var a; a = 1, a--, print(a)");
    inFunction("var a; a = 1, --a, print(a)");

    inFunction("var a; a = 1, print(a++)");
    inFunction("var a; a = 1, print(++a)");

    inFunction("var a; a = 1, print(a++)");
    inFunction("var a; a = 1, print(++a)");

    inFunction("var a; a = 1, print(a--)");
    inFunction("var a; a = 1, print(--a)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testNestedReassignments
  public void testNestedReassignments() {
    inFunction("var a; a = (a = 1)", "var a; 1");
    inFunction("var a; a = (a *= 2)", "var a; a*2");

    
    inFunction("var a; a = (a++)", "var a; a++"); 
    inFunction("var a; a = (++a)", "var a; ++a"); 

    inFunction("var a; a = (b = (a = 1))", "var a; b = 1");
    inFunction("var a; a = (b = (a *= 2))", "var a; b = a * 2");
    inFunction("var a; a = (b = (a++))", "var a; b=a++");
    inFunction("var a; a = (b = (++a))", "var a; b=++a");

    
    inFunction("var a,b; a = (b = (a = 1))", "var a,b; 1");
    inFunction("var a,b; a = (b = (a *= 2))", "var a,b; a * 2");
    inFunction("var a,b; a = (b = (a++))",
               "var a,b; a++"); 
    inFunction("var a,b; a = (b = (++a))",
               "var a,b; ++a"); 

    inFunction("var a; a += (a++)", "var a; a + a++");
    inFunction("var a; a += (++a)", "var a; a+ (++a)");

    
    inFunction("var a,b; a += (b = (a = 1))", "var a,b; a + 1");
    inFunction("var a,b; a += (b = (a *= 2))", "var a,b; a + (a * 2)");
    inFunction("var a,b; a += (b = (a++))", "var a,b; a + a++");
    inFunction("var a,b; a += (b = (++a))", "var a,b; a+(++a)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIncrementalReassignmentInForLoops
  public void testIncrementalReassignmentInForLoops() {
    inFunction("for(;x+=1;x+=1) {}");
    inFunction("for(;x;x+=1){}");
    inFunction("for(;x+=1;){foo(x)}");
    inFunction("for(;1;x+=1){foo(x)}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIdentityAssignments
  public void testIdentityAssignments() {
    inFunction("var x; x=x", "var x; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testBug8730257
  public void testBug8730257() {
    inFunction(
        "  try {" +
        "     var sortIndices = {};" +
        "     sortIndices = bar();" +
        "     for (var i = 0; i < 100; i++) {" +
        "       var sortIndex = sortIndices[i];" +
        "       bar(sortIndex);" +
        "     }" +
        "   } finally {" +
        "     bar();" +
        "   }" );
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignToExtern
  public void testAssignToExtern() {
    inFunction("extern = true;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297a
  public void testIssue297a() {
    testSame("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}; f('');");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297b
  public void testIssue297b() {
    test("function f() {" +
         " var x;" +
         " return (x='') && (x = x.substr(1));" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x='') && (x.substr(1));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297c
  public void testIssue297c() {
    test("function f() {" +
         " var x;" +
         " return (x=1) && (x = f(x));" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x=1) && f(x);" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297d
  public void testIssue297d() {
    test("function f(a) {" +
         " return (a=1) && (a = f(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) && (f(a));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297e
  public void testIssue297e() {
    test("function f(a) {" +
         " return (a=1) - (a = g(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) - (g(a));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297f
  public void testIssue297f() {
    test("function f(a) {" +
         " h((a=1) - (a = g(a)));" +
         "};",
         "function f(a) {" +
         " h((a=1) - (g(a)));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297g
  public void testIssue297g() {
    test("function f(a) {" +
         " var b = h((b=1) - (b = g(b)));" +
         " return b;" +
         "};",
         
         "function f(a) {" +
         " var b = h((b=1) - (b = g(b)));" +
         " return b;" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297h
  public void testIssue297h() {
    test("function f(a) {" +
         " var b = b=1;" +
         " return b;" +
         "};",
         
         "function f(a) {" +
         " var b = b = 1;" +
         " return b;" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInExpression1
  public void testInExpression1() {
    inFunction("var a; return a=(a=(a=3));", "var a; return 3;");
    inFunction("var a; return a=(a=(a=a));", "var a; return a;");
    inFunction("var a; return a=(a=(a=a+1)+1);", "var a; return a+1+1;");
    inFunction("var a; return a=(a=(a=f(a)+1)+1);", "var a; return f(a)+1+1;");
    inFunction("var a; return a=f(a=f(a=f(a)));", "var a; return f(f(f(a)));");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInExpression2
  public void testInExpression2() {
    
    
    inFunction(
        "var a; a = 1; if ((a = 2) || (a = 3) || (a)) {}",
        "var a; a = 1; if ((    2) || (a = 3) || (a)) {}");

    inFunction(
        "var a; (a = 1) || (a = 2)",
        "var a; 1 || 2");

    inFunction("var a; (a = 1) || (a = 2); return a");

    inFunction(
        "var a; a = 1; a ? a = 2 : a;",
        "var a; a = 1; a ?     2 : a;");

    inFunction("var a; a = 1; a ? a = 2 : a; return a");

    inFunction(
        "var a; a = 1; a ? a : a = 2;",
        "var a; a = 1; a ? a : 2;");

    inFunction("var a; a = 1; a ? a : a =2; return a");

    inFunction(
        "var a; (a = 1) ? a = 2 : a = 3;",
        "var a;      1  ?     2 :     3;");

    
    
    inFunction("var a; (a = 1) ? a = 2 : a = 3; return a");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384a
  public void testIssue384a() {
    inFunction(
            " var a, b;\n" +
            " if (f(b = true) || f(b = false))\n" +
            "   a = b;\n" +
            " else\n" +
            "   a = null;\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384b
  public void testIssue384b() {
    inFunction(
            " var a, b;\n" +
            " (f(b = true) || f(b = false)) ? (a = b) : (a = null);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384c
  public void testIssue384c() {
    inFunction(
            " var a, b;\n" +
            " (a ? f(b = true) : f(b = false)) && (a = b);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384d
  public void testIssue384d() {
    inFunction(
            " var a, b;\n" +
            " (f(b = true) || f(b = false)) && (a = b);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testVarAndOptionalParams
  public void testVarAndOptionalParams() {
    Node args = new Node(Token.LP,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Node optArgs = new Node(Token.LP,
        Node.newString(Token.NAME, "opt_a"),
        Node.newString(Token.NAME, "opt_b"));

    assertFalse(conv.isVarArgsParameter(args.getFirstChild()));
    assertTrue(conv.isVarArgsParameter(args.getLastChild()));
    assertFalse(conv.isVarArgsParameter(optArgs.getFirstChild()));
    assertTrue(conv.isVarArgsParameter(optArgs.getLastChild()));

    assertTrue(conv.isOptionalParameter(args.getFirstChild()));
    assertFalse(conv.isOptionalParameter(args.getLastChild()));
    assertTrue(conv.isOptionalParameter(optArgs.getFirstChild()));
    assertFalse(conv.isOptionalParameter(optArgs.getLastChild()));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInlineName
  public void testInlineName() {
    assertFalse(conv.isConstant("a"));
    assertFalse(conv.isConstant("XYZ123_"));
    assertFalse(conv.isConstant("ABC"));
    assertFalse(conv.isConstant("ABCdef"));
    assertFalse(conv.isConstant("aBC"));
    assertFalse(conv.isConstant("A"));
    assertFalse(conv.isConstant("_XYZ123"));
    assertFalse(conv.isConstant("a$b$XYZ123_"));
    assertFalse(conv.isConstant("a$b$ABC_DEF"));
    assertFalse(conv.isConstant("a$b$A"));
    assertFalse(conv.isConstant("a$b$a"));
    assertFalse(conv.isConstant("a$b$ABCdef"));
    assertFalse(conv.isConstant("a$b$aBC"));
    assertFalse(conv.isConstant("a$b$"));
    assertFalse(conv.isConstant("$"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testExportedName
  public void testExportedName() {
    assertFalse(conv.isExported("_a"));
    assertFalse(conv.isExported("_a_"));
    assertFalse(conv.isExported("a"));

    assertFalse(conv.isExported("$super", false));
    assertTrue(conv.isExported("$super", true));
    assertTrue(conv.isExported("$super"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testPrivateName
  public void testPrivateName() {
    assertFalse(conv.isPrivate("a_"));
    assertFalse(conv.isPrivate("a"));
    assertFalse(conv.isPrivate("_a_"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testEnumKey
  public void testEnumKey() {
    assertTrue(conv.isValidEnumKey("A"));
    assertTrue(conv.isValidEnumKey("123"));
    assertTrue(conv.isValidEnumKey("FOO_BAR"));

    assertTrue(conv.isValidEnumKey("a"));
    assertTrue(conv.isValidEnumKey("someKeyInCamelCase"));
    assertTrue(conv.isValidEnumKey("_FOO_BAR"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection1
  public void testInheritanceDetection1() {
    assertNotClassDefining("goog.foo(A, B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection2
  public void testInheritanceDetection2() {
    assertNotClassDefining("goog.inherits(A, B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection3
  public void testInheritanceDetection3() {
    assertNotClassDefining("A.inherits(B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection4
  public void testInheritanceDetection4() {
    assertNotClassDefining("goog.inherits(goog.A, goog.B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection5
  public void testInheritanceDetection5() {
    assertNotClassDefining("goog.A.inherits(goog.B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection6
  public void testInheritanceDetection6() {
    assertNotClassDefining("A.inherits(this.B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection7
  public void testInheritanceDetection7() {
    assertNotClassDefining("this.A.inherits(B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection8
  public void testInheritanceDetection8() {
    assertNotClassDefining("goog.inherits(A, B, C);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection9
  public void testInheritanceDetection9() {
    assertNotClassDefining("A.mixin(B.prototype);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection10
  public void testInheritanceDetection10() {
    assertNotClassDefining("goog.mixin(A.prototype, B.prototype);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetectionPostCollapseProperties
  public void testInheritanceDetectionPostCollapseProperties() {
    assertNotClassDefining("goog$inherits(A, B);");
    assertNotClassDefining("goog$inherits(A);");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveFunction
  public void testRemoveFunction() {
    testSame("{(function (){bar()})}");
    test("{function a(){bar()}}", "{}");
    test("foo(); function a(){} bar()", "foo(); bar();");
    test("foo(); function a(){} function b(){} bar()", "foo(); bar();");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveAssignment
  public void testRemoveAssignment() {
    test("x = 0;", "0");
    test("{x = 0}", "{0}");
    test("x = 0; y = 0;", "0; 0;");
    test("for (x = 0;x;x) {};", "for(0;x;x) {};");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveVarAssignment
  public void testRemoveVarAssignment() {
    test("var x = 0;", "0");
    test("{var x = 0}", "{0}");
    test("var x = 0; var y = 0;", "0;0");
    test("var x = 0; var y = 0;", "0;0");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveLiteral
  public void testRemoveLiteral() {
    test("foo({ 'one' : 1 })", "foo({ })");
    test("foo({ 'one' : 1 , 'two' : 2 })", "foo({ })");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveFunctionExpressionName
  public void testRemoveFunctionExpressionName() {
    test("foo(function f(){})", "foo(function (){})");
  }

// com.google.javascript.jscomp.DenormalizeTest::testFor
  public void testFor() {
    
    test("a = 0; for(; a < 2 ; a++) foo()",
         "for(a = 0; a < 2 ; a++) foo();");
    
    test("var a = 0; for(; c < b ; c++) foo()",
         "for(var a = 0; c < b ; c++) foo()");

    
    testSame("var a = 0; a:for(; c < b ; c++) foo()");
    testSame("var a = 0; a:b:for(; c < b ; c++) foo()");

    
    test("if(x){var a = 0; for(; c < b; c++) foo()}",
         "if(x){for(var a = 0; c < b; c++) foo()}");

    
    test("init(); for(; a < 2 ; a++) foo()",
         "for(init(); a < 2 ; a++) foo();");

    
    test("function f(){ var a; for(; a < 2 ; a++) foo() }",
         "function f(){ for(var a; a < 2 ; a++) foo() }");
    testSame("function f(){ return; for(; a < 2 ; a++) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testForIn
  public void testForIn() {
    test("var a; for(a in b) foo()", "for (var a in b) foo()");
    testSame("a = 0; for(a in b) foo()");
    testSame("var a = 0; for(a in b) foo()");

    
    testSame("var a; a:for(a in b) foo()");
    testSame("var a; a:b:for(a in b) foo()");

    
    test("if(x){var a; for(a in b) foo()}",
         "if(x){for(var a in b) foo()}");

    
    testSame("init(); for(a in b) foo()");

    
    testSame("function f(){ return; for(a in b) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testInOperatorNotInsideFor
  public void testInOperatorNotInsideFor() {
    
    
    
    

    
    testSame("function f(){ var a; var i=\"length\" in a;" +
        "for(; a < 2 ; a++) foo() }");
    
    testSame("function f(){ var a; var i=(\"length\" in a);" +
        "for(; a < 2 ; a++) foo() }");
    
    
    test("function f(){" +
         "var b,a=0; for (var i=(\"length\" in b);a<2; a++) foo()}",
         "function f(){var b; var a=0;var i=(\"length\" in b);" +
         "for (;a<2;a++) foo()}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeMethods1
  public void testRewritePrototypeMethods1() throws Exception {
    
    disableTypeCheck();
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_OFF);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeMethods2
  public void testRewritePrototypeMethods2() throws Exception {
    
    enableTypeCheck(CheckLevel.ERROR);
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_ON);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteChained
  public void testRewriteChained() throws Exception {
    String source = newlineJoin(
        "A.prototype.foo = function(){return this.b};",
        "B.prototype.bar = function(){};",
        "o.foo().bar()");

    String expected = newlineJoin(
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.b",
        "};",
        "var JSCompiler_StaticMethods_bar = ",
        "function(JSCompiler_StaticMethods_bar$self) {",
        "};",
        "JSCompiler_StaticMethods_bar(JSCompiler_StaticMethods_foo(o))");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDeclIsExpressionStatement
  public void testRewriteDeclIsExpressionStatement() throws Exception {
    test(semicolonJoin(NoRewriteDeclarationUsedAsRValue.DECL,
                       NoRewriteDeclarationUsedAsRValue.CALL),
         "var JSCompiler_StaticMethods_foo =" +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteDeclUsedAsAssignmentRhs
  public void testNoRewriteDeclUsedAsAssignmentRhs() throws Exception {
    testSame(semicolonJoin("var c = " + NoRewriteDeclarationUsedAsRValue.DECL,
                           NoRewriteDeclarationUsedAsRValue.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteDeclUsedAsCallArgument
  public void testNoRewriteDeclUsedAsCallArgument() throws Exception {
    testSame(semicolonJoin("f(" + NoRewriteDeclarationUsedAsRValue.DECL + ")",
                           NoRewriteDeclarationUsedAsRValue.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteInGlobalScope
  public void testRewriteInGlobalScope() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteIfNotInGlobalScopeTestInput.INPUT, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteIfNotInGlobalScope1
  public void testNoRewriteIfNotInGlobalScope1() throws Exception {
    testSame("if(true){" + NoRewriteIfNotInGlobalScopeTestInput.INPUT + "}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteIfNotInGlobalScope2
  public void testNoRewriteIfNotInGlobalScope2() throws Exception {
    testSame("function enclosingFunction() {" +
             NoRewriteIfNotInGlobalScopeTestInput.INPUT +
             "}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNamespaceFunctions
  public void testNoRewriteNamespaceFunctions() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.foo = function() {return this.x};",
        "a.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition1
  public void testRewriteSingleDefinition1() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition2
  public void testRewriteSingleDefinition2() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition1
  public void testNoRewriteMultipleDefinition1() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition2
  public void testNoRewriteMultipleDefinition2() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition3
  public void testNoRewriteMultipleDefinition3() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeNoObjectLiterals
  public void testRewritePrototypeNoObjectLiterals() throws Exception {
    test(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                       NoRewritePrototypeObjectLiteralsTestInput.CALL),
         "var JSCompiler_StaticMethods_foo = " +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals1
  public void testNoRewritePrototypeObjectLiterals1() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals2
  public void testNoRewritePrototypeObjectLiterals2() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods1
  public void testNoRewriteExternalMethods1() throws Exception {
    testSame("a.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods2
  public void testNoRewriteExternalMethods2() throws Exception {
    testSame("A.prototype.externalMethod = function(){}; o.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteCodingConvention
  public void testNoRewriteCodingConvention() throws Exception {
    
    testSame("a.prototype._foo = function() {};");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoVarArgs
  public void testRewriteNoVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");

    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");

    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteVarArgs
  public void testNoRewriteVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(var_args) {return arguments};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteCallReference
  public void testRewriteCallReference() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteNonCallReferenceTestInput.BASE + "o.foo()", expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNoReferences
  public void testNoRewriteNoReferences() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNonCallReference
  public void testNoRewriteNonCallReference() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE + "o.foo && o.foo()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoNestedFunction
  public void testRewriteNoNestedFunction() throws Exception {
    test(semicolonJoin(
             NoRewriteNestedFunctionTestInput.PREFIX + "}",
             NoRewriteNestedFunctionTestInput.SUFFIX,
             NoRewriteNestedFunctionTestInput.INNER),
         semicolonJoin(
             NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX + "}",
             NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX,
             "var JSCompiler_StaticMethods_bar=" +
             "function(JSCompiler_StaticMethods_bar$self){}",
             "JSCompiler_StaticMethods_bar(o)"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNestedFunction
  public void testNoRewriteNestedFunction() throws Exception {
    test(NoRewriteNestedFunctionTestInput.PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.SUFFIX,
         NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod
  public void testRewriteImplementedMethod() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod2
  public void testRewriteImplementedMethod2() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod3
  public void testRewriteImplementedMethod3() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod4
  public void testRewriteImplementedMethod4() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethodInObj
  public void testRewriteImplementedMethodInObj() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {foo: function(args) {return args}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet1
  public void testNoRewriteGet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return f}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet2
  public void testNoRewriteGet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return 1}};",
        "var o = new a;",
        "o.foo");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet1
  public void testNoRewriteSet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet2
  public void testNoRewriteSet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo = 1");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNotImplementedMethod
  public void testNoRewriteNotImplementedMethod() throws Exception {
    testSame(newlineJoin("function a(){}",
                         "var o = new a;",
                         "o.foo()"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule1
  public void testRewriteSameModule1() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE),
        
        "",
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule2
  public void testRewriteSameModule2() throws Exception {
    JSModule[] modules = createModuleStar(
        
        "",
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE));

    test(modules, new String[] {
        
        "",
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE)
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule3
  public void testRewriteSameModule3() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.USE,
                      ModuleTestInput.DEFINITION),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_USE,
                      ModuleTestInput.REWRITTEN_DEFINITION),
        
        ""
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDefinitionBeforeUse
  public void testRewriteDefinitionBeforeUse() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.DEFINITION,
        
        ModuleTestInput.USE);

    test(modules, new String[] {
        
        ModuleTestInput.REWRITTEN_DEFINITION,
        
        ModuleTestInput.REWRITTEN_USE
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteUseBeforeDefinition
  public void testNoRewriteUseBeforeDefinition() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.USE,
        
        ModuleTestInput.DEFINITION);

    testSame(modules);
  }

// com.google.javascript.jscomp.DiagnosticGroupTest::testRegistration
  public void testRegistration() throws Exception {
    DiagnosticGroups dg = new DiagnosticGroups();
    assertEquals(DiagnosticGroups.DEPRECATED,
        dg.forName("deprecated"));
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType1
  public void testOneType1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType2
  public void testOneType2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    
    
    String desired = "{a=[[Foo.prototype]]}";
    String expected = "{}";
    testSets(false, js, js, expected);

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType3
  public void testOneType3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    
    
    String desired = "{a=[[Foo.prototype]]}";
    String expected = "{}";
    testSets(false, js, js, expected);

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance
  public void testPrototypeAndInstance() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes1
  public void testTwoTypes1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes2
  public void testTwoTypes2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = {a: 0};"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = {Foo_prototype$a: 0};"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = {Bar_prototype$a: 0};"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    
    
    testSets(false, js, js, "{}");
    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes3
  public void testTwoTypes3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = { get Foo_prototype$a() {return  0},"
        + "                  set Foo_prototype$a(b) {} };\n"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = { get Bar_prototype$a() {return  0},"
        + "                  set Bar_prototype$a(b) {} };\n"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    
    
    testSets(false, js, js, "{}");
    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoFields
  public void testTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;";
    String output = "function Foo(){}Foo.prototype.a=0;Foo.prototype.b=0;"
        + "var F=new Foo;F.a=0;F.b=0";
    testSets(false, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoSeparateFieldsTwoTypes
  public void testTwoSeparateFieldsTwoTypes() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "Bar.prototype.b = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;"
        + "B.b = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.Foo_prototype$b=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "F.Foo_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                                + " b=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                               + " b=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionType
  public void testUnionType() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B = new Foo;\n"
        + "B.a = 0;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n";
    testSets(false, js,
             "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType
  public void testIgnoreUnknownType() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeTwoFields
  public void testUnionTypeTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.b = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.b = 0;\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B.b = 0;\n"
        + "B = new Foo;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n"
        + "Baz.prototype.b = 0;\n";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Bar_prototype$a=0;"
        + "Foo.prototype.Bar_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0;"
        + "function Baz(){}"
        + "Baz.prototype.a$Baz_prototype=0;"
        + "Baz.prototype.b$Baz_prototype=0;";
    testSets(false, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }
