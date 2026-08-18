// buggy code
      public boolean apply(Node n) {
        // When the node is null it means, we reached the implicit return
        // where the function returns (possibly without an return statement)
        if (n == null) {
          return false;
        }

        // TODO(user): We only care about calls to functions that
        // passes one of the dependent variable to a non-side-effect free
        // function.
        if (n.isCall() && NodeUtil.functionCallHasSideEffects(n)) {
          return true;
        }

        if (n.isNew() && NodeUtil.constructorCallHasSideEffects(n)) {
          return true;
        }


        for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
          if (!ControlFlowGraph.isEnteringNewCfgNode(c) && apply(c)) {
            return true;
          }
        }
        return false;
      }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testLateStatementFusion
  public void testLateStatementFusion() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
        "while(a){a();if(b){b();b()}}",
        "for(;a;)a(),b&&(b(),b())");
  }

// com.google.javascript.jscomp.IntegrationTest::testLateConstantReordering
  public void testLateConstantReordering() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
        "if (x < 1 || x > 1 || 1 < x || 1 > x) { alert(x) }",
        "   (1 > x || 1 < x || 1 < x || 1 > x) && alert(x) ");
  }

// com.google.javascript.jscomp.IntegrationTest::testsyntheticBlockOnDeadAssignments
  public void testsyntheticBlockOnDeadAssignments() {
    CompilerOptions options = createCompilerOptions();
    options.deadAssignmentElimination = true;
    options.removeUnusedVars = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    test(options, "var x; x = 1; START(); x = 1;END();x()",
                  "var x; x = 1;{START();{x = 1}END()}x()");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug4152835
  public void testBug4152835() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    test(options, "START();END()", "{START();{}END()}");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug5786871
  public void testBug5786871() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    test(options, "function () {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue378
  public void testIssue378() {
    CompilerOptions options = createCompilerOptions();
    options.inlineVariables = true;
    options.flowSensitiveInlineVariables = true;
    testSame(options, "function f(c) {var f = c; arguments[0] = this;" +
                      "    f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue550
  public void testIssue550() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.foldConstants = true;
    options.inlineVariables = true;
    options.flowSensitiveInlineVariables = true;
    test(options,
        "function f(h) {\n" +
        "  var a = h;\n" +
        "  a = a + 'x';\n" +
        "  a = a + 'y';\n" +
        "  return a;\n" +
        "}",
        "function f(a) {return a + 'xy'}");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue284
  public void testIssue284() {
    CompilerOptions options = createCompilerOptions();
    options.smartNameRemoval = true;
    test(options,
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "" +
        "ns.PageSelectionModel = function() {};" +
        "" +
        "ns.PageSelectionModel.FooEvent = function() {};" +
        "" +
        "ns.PageSelectionModel.SelectEvent = function() {};" +
        "goog.inherits(ns.PageSelectionModel.ChangeEvent," +
        "    ns.PageSelectionModel.FooEvent);",
        "");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue772
  public void testIssue772() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    test(
        options,
        " var a = {};" +
        " a.b = {};" +
        " a.b.c = {};" +
        "goog.scope(function() {" +
        "  var b = a.b;" +
        "  var c = b.c;" +
        "  " +
        "  c.MyType;" +
        "  " +
        "  c.myFunc = function(x) {};" +
        "});",
        " var a = {};" +
        " a.b = {};" +
        " a.b.c = {};" +
        "a.b.c.MyType;" +
        "a.b.c.myFunc = function(x) {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testCodingConvention
  public void testCodingConvention() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    assertEquals(
      compiler.getCodingConvention().getClass().toString(),
      ClosureCodingConvention.class.toString());
  }

// com.google.javascript.jscomp.IntegrationTest::testJQueryStringSplitLoops
  public void testJQueryStringSplitLoops() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
      "var x=['1','2','3','4','5','6','7']",
      "var x='1234567'.split('')");

    options = createCompilerOptions();
    options.foldConstants = true;
    options.computeFunctionSideEffects = false;
    options.removeUnusedVars = true;

    
    test(options,
      "var x=['1','2','3','4','5','6','7']",
      "");

  }

// com.google.javascript.jscomp.IntegrationTest::testAlwaysRunSafetyCheck
  public void testAlwaysRunSafetyCheck() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = false;
    options.customPasses = ArrayListMultimap.create();
    options.customPasses.put(
        CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
        new CompilerPass() {
          @Override public void process(Node externs, Node root) {
            Node var = root.getLastChild().getFirstChild();
            assertEquals(Token.VAR, var.getType());
            var.detachFromParent();
          }
        });
    try {
      test(options,
           "var x = 3; function f() { return x + z; }",
           "function f() { return x + z; }");
      fail("Expected run-time exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressEs5StrictWarning
  public void testSuppressEs5StrictWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() { var arguments; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesWarning
  public void testCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES, CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    test(options,
        "\n" +
        "function f() { var arguments; }",
        DiagnosticType.warning("JSC_MISSING_PROVIDE", "missing goog.provide(''{0}'')"));
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCheckProvidesWarning
  public void testSuppressCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES, CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() { var arguments; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespace
  public void testRenamePrefixNamespace() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    options.renamePrefixNamespace = "_";
    test(options, code, "_.x$FOO = 5; _.x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespaceActivatesMoveFunctionDeclarations
  public void testRenamePrefixNamespaceActivatesMoveFunctionDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var x = f; function f() { return 3; }";
    testSame(options, code);
    assertFalse(options.moveFunctionDeclarations);
    options.renamePrefixNamespace = "_";
    test(options, code, "_.f = function() { return 3; }; _.x = _.f;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBrokenNameSpace
  public void testBrokenNameSpace() {
    CompilerOptions options = createCompilerOptions();
    String code = "var goog; goog.provide('i.am.on.a.Horse');" +
                  "i.am.on.a.Horse = function() {};" +
                  "i.am.on.a.Horse.prototype.x = function() {};" +
                  "i.am.on.a.Boat.prototype.y = function() {}";
    options.closurePass = true;
    options.collapseProperties = true;
    options.smartNameRemoval = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testNamelessParameter
  public void testNamelessParameter() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    String code =
        "var impl_0;" +
        "$load($init());" +
        "function $load(){" +
        "  window['f'] = impl_0;" +
        "}" +
        "function $init() {" +
        "  impl_0 = {};" +
        "}";
    String result =
        "window.f = {};";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testHiddenSideEffect
  public void testHiddenSideEffect() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setAliasExternals(true);
    String code =
        "window.offsetWidth;";
    String result =
        "window.offsetWidth;";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testNegativeZero
  public void testNegativeZero() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
        "function bar(x) { return x; }\n" +
        "function foo(x) { print(x / bar(0));\n" +
        "                 print(x / bar(-0)); }\n" +
        "foo(3);",
        "print(3/0);print(3/-0);");
  }

// com.google.javascript.jscomp.IntegrationTest::testSingletonGetter1
  public void testSingletonGetter1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
        "\n" +
        "var goog = goog || {};\n" +
        "goog.addSingletonGetter = function(ctor) {\n" +
        "  ctor.getInstance = function() {\n" +
        "    return ctor.instance_ || (ctor.instance_ = new ctor());\n" +
        "  };\n" +
        "};" +
        "function Foo() {}\n" +
        "goog.addSingletonGetter(Foo);" +
        "Foo.prototype.bar = 1;" +
        "function Bar() {}\n" +
        "goog.addSingletonGetter(Bar);" +
        "Bar.prototype.bar = 1;",
        "");
  }

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction1
  public void testIncompleteFunction1() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var foo = {bar: function(e) }" },
        new String[] { "var foo = {bar: function(e){}};" },
        warnings
    );
  }

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction2
  public void testIncompleteFunction2() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "function hi" },
        new String[] { "function hi() {}" },
        warnings
    );
  }

// com.google.javascript.jscomp.IntegrationTest::testSortingOff
  public void testSortingOff() {
    CompilerOptions options = new CompilerOptions();
    options.closurePass = true;
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
         new String[] {
           "goog.require('goog.beer');",
           "goog.provide('goog.beer');"
         },
         ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testUnboundedArrayLiteralInfiniteLoop
  public void testUnboundedArrayLiteralInfiniteLoop() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    test(options,
         "var x = [1, 2",
         "var x = [1, 2]",
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvideRequireSameFile
  public void testProvideRequireSameFile() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    options.closurePass = true;
    test(
        options,
        "goog.provide('x');\ngoog.require('x');",
        "var x = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testDependencySorting
  public void testDependencySorting() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    test(
        options,
        new String[] {
          "goog.require('x');",
          "goog.provide('x');",
        },
        new String[] {
          "goog.provide('x');",
          "goog.require('x');",

          
          
          "",
        });
  }

// com.google.javascript.jscomp.IntegrationTest::testStrictWarningsGuard
  public void testStrictWarningsGuard() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.addWarningsGuard(new StrictWarningsGuard());

    Compiler compiler = compile(options,
        " function f() { return true; }");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(0, compiler.getWarnings().length);
  }

// com.google.javascript.jscomp.IntegrationTest::testStrictWarningsGuardEmergencyMode
  public void testStrictWarningsGuardEmergencyMode() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.addWarningsGuard(new StrictWarningsGuard());
    options.useEmergencyFailSafe();

    Compiler compiler = compile(options,
        " function f() { return true; }");
    assertEquals(0, compiler.getErrors().length);
    assertEquals(1, compiler.getWarnings().length);
  }

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
