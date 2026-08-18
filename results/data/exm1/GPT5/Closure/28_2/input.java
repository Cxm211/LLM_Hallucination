// buggy code
    void addIdentifier(String identifier) {
      add(ESTIMATED_IDENTIFIER);
    }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testFoldLocals4
  public void testFoldLocals4() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = "\n"
        + "function InternalWidget(){this.x = 1;}"
        + "InternalWidget.prototype.internalGo = function (){this.x = 2};"
        + "new InternalWidget().internalGo();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String optimized = ""
      + "function InternalWidget(){this.x = 1;}"
      + "InternalWidget.prototype.internalGo = function (){this.x = 2};";

    test(options, code, optimized);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals5
  public void testFoldLocals5() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function fn(){var a={};a.x={};return a}"
        + "fn().x.y = 1;";

    
    
    String result = ""
        + "function fn(){var a={x:{}};return a}"
        + "fn().x.y = 1;";

    test(options, code, result);

    options.computeFunctionSideEffects = true;

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals6
  public void testFoldLocals6() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function fn(){return {}}"
        + "fn().x.y = 1;";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldLocals7
  public void testFoldLocals7() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = true;

    String code = ""
        + "function InternalWidget(){return [];}"
        + "Array.prototype.internalGo = function (){this.x = 2};"
        + "InternalWidget().internalGo();";

    testSame(options, code);

    options.computeFunctionSideEffects = true;

    String optimized = ""
      + "function InternalWidget(){return [];}"
      + "Array.prototype.internalGo = function (){this.x = 2};";

    test(options, code, optimized);
  }

// com.google.javascript.jscomp.IntegrationTest::testVarDeclarationsIntoFor
  public void testVarDeclarationsIntoFor() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "var a = 1; for (var b = 2; ;) {}";

    testSame(options, code);

    options.collapseVariableDeclarations = false;

    test(options, code, "for (var a = 1, b = 2; ;) {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testExploitAssigns
  public void testExploitAssigns() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "a = 1; b = a; c = b";

    testSame(options, code);

    options.collapseVariableDeclarations = true;

    test(options, code, "c=b=a=1");
  }

// com.google.javascript.jscomp.IntegrationTest::testRecoverOnBadExterns
  public void testRecoverOnBadExterns() throws Exception {
    
    
    
    
    
    
    
    
    
    CompilerOptions options = createCompilerOptions();

    options.aliasExternals = true;
    externs = ImmutableList.of(
        SourceFile.fromCode("externs", "extern.foo"));

    test(options,
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testDuplicateVariablesInExterns
  public void testDuplicateVariablesInExterns() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    externs = ImmutableList.of(
        SourceFile.fromCode("externs",
            "var externs = {};  var externs = {};"));
    testSame(options, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode
  public void testLanguageMode() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);

    String code = "var a = {get f(){}}";

    Compiler compiler = compile(options, code);
    checkUnexpectedErrorsOrWarnings(compiler, 1);
    assertEquals(
        "JSC_PARSE_ERROR. Parse error. " +
        "getters are not supported in older versions of JS. " +
        "If you are targeting newer versions of JS, " +
        "set the appropriate language_in option. " +
        "at i0 line 1 : 0",
        compiler.getErrors()[0].toString());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode2
  public void testLanguageMode2() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.OFF);

    String code = "var a  = 2; delete a;";

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    test(options,
        code,
        code,
        StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue598
  public void testIssue598() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    WarningLevel.VERBOSE.setOptionsForWarningLevel(options);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    String code =
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};";

    Compiler compiler = compile(options, code);
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue701
  public void testIssue701() {
    
    String ascii = "";
    String result = "\n";
    testSame(createCompilerOptions(), ascii);
    assertEquals(result, lastCompiler.toSource());
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue724
  public void testIssue724() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    String code =
        "isFunction = function(functionToCheck) {" +
        "  var getType = {};" +
        "  return functionToCheck && " +
        "      getType.toString.apply(functionToCheck) === " +
        "     '[object Function]';" +
        "};";
    String result =
        "isFunction=function(a){var b={};" +
        "return a&&\"[object Function]\"===b.b.a(a)}";

    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testCoaleseVariables
  public void testCoaleseVariables() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = false;
    options.coalesceVariableNames = true;

    String code =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";
    String expected =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    a = a;" +
        "    return a;" +
        "  }" +
        "  return a;" +
        "}";

    test(options, code, expected);

    options.foldConstants = true;
    options.coalesceVariableNames = false;

    code =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";
    expected =
        "function f(a) {" +
        "  if (!a) {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";

    test(options, code, expected);

    options.foldConstants = true;
    options.coalesceVariableNames = true;

    expected =
      "function f(a) {" +
      "  return a;" +
      "}";

    test(options, code, expected);
  }

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
      fail("Expected runtime exception");
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

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInline
  public void testSpecializeInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
        );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeCascadedInline
  public void testSpecializeCascadedInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return C()};" +
        "var C = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return C()};" + 
        "C = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithMultipleDependents
  public void testSpecializeInlineWithMultipleDependents() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
        "A();",
        
        "A();"
    );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();",
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();",

    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithNamespaces
  public void testSpecializeInlineWithNamespaces() {
    JSModule[] modules = createModuleStar(
        
        "var ns = {};" +
        
        "ns.A = function() {alert(B());ns.A()};" +
        "var B = function() {return 6};" +
        "ns.A();",
        
        "B = function() {return 7};" +
    "ns.A();");

    test(modules, new String[] {
        
        "var ns = {};" +
        "ns.A = function() {alert(6);ns.A()};" + 
        "ns.A();" +
        "var B;",
        
        "ns.A = function() {alert(B());ns.A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "ns.A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithRegularFunctions
  public void testSpecializeInlineWithRegularFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "function A() {alert(6);A()}" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeLocalNonAnonymousFunctions
  public void testDontSpecializeLocalNonAnonymousFunctions() {
    
    enableNormalize(false);

    JSModule[] modules = createModuleStar(
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        "");

    test(modules, new String[] {
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        ""
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testAddDummyVarsForRemovedFunctions
  public void testAddDummyVarsForRemovedFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B() + C());A()};" +
        "var B = function() {return 6};" +
        "var C = function() {return 8};" +
        "A();",
        
        "" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6 + 8);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B() + C());A()};" + 
        "B = function() {return 6};" + 
        "C = function() {return 8};" + 
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeRemoveUnusedProperties
  public void testSpecializeRemoveUnusedProperties() {
    JSModule[] modules = createModuleStar(
        
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "var aliasA = Foo.prototype.a;" +
        "var x = new Foo();" +
        "x.a();",
        
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_inline
  public void testDontSpecializeAliasedFunctions_inline() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();");

    test(modules, new String[] {
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_remove_unused_properties
  public void testDontSpecializeAliasedFunctions_remove_unused_properties() {
    JSModule[] modules = createModuleStar(
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "Foo.prototype.d = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "Foo.prototype.d = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethods
  public void testSpecializeDevirtualizePrototypeMethods() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};" +
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var JSCompiler_StaticMethods_a =" +
              "function(JSCompiler_StaticMethods_a$self) {" +
           "JSCompiler_StaticMethods_a(JSCompiler_StaticMethods_a$self);" +
           "return 7" +
        "};" +
        "var x = new Foo();" +
        "JSCompiler_StaticMethods_a(x);",
        
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethodsWithInline
  public void testSpecializeDevirtualizePrototypeMethodsWithInline() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {return 7};" +
        "var x = new Foo();" +
        "var z = x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var x = new Foo();" +
        "var z = 7;",
        
        "Foo.prototype.a = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testRemovedFunctions
    public void testRemovedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());

      Node functionF = findFunction("F");

      lastState.reportRemovedFunction(functionF, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF), lastState.getRemovedFunctions());

      Node functionG = findFunction("F");

      lastState.reportRemovedFunction(functionG, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getRemovedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializedFunctions
    public void testSpecializedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());

      Node functionF = findFunction("F");

      lastState.reportSpecializedFunction(functionF);
      assertEquals(ImmutableSet.of(functionF),
          lastState.getSpecializedFunctions());

      Node functionG = findFunction("F");

      lastState.reportSpecializedFunction(functionG);
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getSpecializedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testCanFixupFunction
    public void testCanFixupFunction() {
      testSame("function F(){}\n" +
               "var G = function(a){};\n" +
               "var ns = {};" +
               "ns.H = function(){};" +
               "var ns2 = {I : function anon1(){}};" +
               "(function anon2(){})();");

      assertTrue(lastState.canFixupFunction(findFunction("F")));
      assertTrue(lastState.canFixupFunction(findFunction("G")));
      assertTrue(lastState.canFixupFunction(findFunction("ns.H")));
      assertFalse(lastState.canFixupFunction(findFunction("anon1")));
      assertFalse(lastState.canFixupFunction(findFunction("anon2")));

      
      testSame("function A(){}\n" +
          "var aliasA = A;\n");

      assertFalse(lastState.canFixupFunction(findFunction("A")));
    }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
