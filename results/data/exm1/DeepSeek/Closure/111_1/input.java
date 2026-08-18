// buggy code
        protected JSType caseTopType(JSType topType) {
          return topType;
        }

// relevant test
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
    test(options,
        "\n" +
        "function f() { var arguments; }",
        "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesWarning
  public void testCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES,
        CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    test(options,
        "\n" +
        "function f() { var arguments; }",
        DiagnosticType
        .warning("JSC_MISSING_PROVIDE", "missing goog.provide(''{0}'')"));
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCheckProvidesWarning
  public void testSuppressCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES,
        CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCastWarning
  public void testSuppressCastWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    normalizeResults = true;

    test(options,
        "function f() { var xyz =  (0); }",
        DiagnosticType.warning(
            "JSC_INVALID_CAST", "invalid cast"));

    testSame(options,
        "\n" +
        "function f() { var xyz =  (0); }");

    testSame(options,
        " var g = {};" +
        "" +
        "g.a = g.b = function() { var xyz =  (0); }");
  }

// com.google.javascript.jscomp.IntegrationTest::testLhsCast
  public void testLhsCast() {
    CompilerOptions options = createCompilerOptions();
    test(
        options,
        " var g = {};" +
        " (g.foo) = 3;",
        " var g = {};" +
        "g.foo = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefix
  public void testRenamePrefix() {
    String code = "var x = {}; function f(y) {}";
    CompilerOptions options = createCompilerOptions();
    options.renamePrefix = "G_";
    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options, code, "var G_={}; function G_a(a) {}");
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

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespaceProtectSideEffects
  public void testRenamePrefixNamespaceProtectSideEffects() {
    String code = "var x = null; try { +x.FOO; } catch (e) {}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    options.renamePrefixNamespace = "_";
    test(options, code, "_.x = null; try { +_.x.FOO; } catch (e) {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenameCollision
  public void testRenameCollision() {
    String code = "" +
          "" +
          "var x = {};\ntry {\n(0,use)(x.FOO);\n} catch (e) {}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    options.renamePrefixNamespace = "a";
    options.setVariableRenaming(VariableRenamingPolicy.ALL);
    options.setRenamePrefixNamespaceAssumeCrossModuleNames(false);
    WarningLevel.DEFAULT.setOptionsForWarningLevel(options);

    test(options, code,
        "var b = {}; try { (0,window.use)(b.FOO); } catch (c) {}");
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

// com.google.javascript.jscomp.IntegrationTest::testBiasedLabelRenaming
  public void testBiasedLabelRenaming() {
    CompilerOptions options = createCompilerOptions();
    options.setAggressiveRenaming(true);
    options.setLabelRenaming(true);
    String code = "function a() {lbl: while(1) {while(1) {break lbl}}}";
    String result = "function a() {f: for(;1;) for(;1;)break f}";
    test(options, code, result);
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
            " ((new x())['abc'])());";
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

// com.google.javascript.jscomp.IntegrationTest::testExports
  public void testExports() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        " var X = function() {" +
           " this.abc = 1;};\n" +
        " var Y = function() {" +
           " this.abc = 1;};\n" +
        "alert(new X().abc + new Y().abc);";

    
    test(options, code,
        "alert((new function(){this.a = 1}).a + " +
            "(new function(){this.a = 1}).a);");

    options.generateExports = true;

    
    test(options,
        " var X = function() {" +
        " this.abc = 1;};\n",
        FindExportableNodes.NON_GLOBAL_ERROR);

    options.exportLocalPropertyDefinitions = true;

    
    
    test(options, code,
        DefaultPassConfig.CANNOT_USE_EXPORT_LOCALS_AND_EXTERN_PROP_REMOVAL);

    options.removeUnusedPrototypePropertiesInExterns = false;

    
    test(options, code,
        "alert((new function(){this.abc = 1}).abc + " +
            "(new function(){this.abc = 1}).abc);");

    
    test(options, "" +
        " var X = function() {" +
        " this.abc = 1;};\n" +
        " var Y = function() {" +
        " this.abc = 1;};\n" +
        "alert(new X() + new Y());",
        "alert((new function(){this.abc = 1}) + " +
            "(new function(){this.abc = 1}));");

    
    options.checkTypes = true;
    options.disambiguateProperties = true;
    options.ambiguateProperties = true;
    options.propertyInvalidationErrors = ImmutableMap.of(
        "abc", CheckLevel.ERROR);

    test(options, code,
        "alert((new function(){this.abc = 1}).abc + " +
            "(new function(){this.abc = 1}).abc);");

    
    test(options, "" +
        " var X = function() {" +
        " this.abc = 1;};\n" +
        " var Y = function() {" +
        " this.abc = 1;};\n" +
        "alert(new X() + new Y());",
        "alert((new function(){this.abc = 1}) + " +
            "(new function(){this.abc = 1}));");
  }

// com.google.javascript.jscomp.IntegrationTest::testManyAdds
  public void testManyAdds() {}

// com.google.javascript.jscomp.IntegrationTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    String[] input1 = {"function f(z) { return z; }"};
    String[] input2 = {"function f(y) { return y; }"};
    CompilerOptions options = new CompilerOptions();
    Node out1 = parse(input1, options, false);
    Node out2 = parse(input2, options, false);
    assertFalse(out1.isEquivalentTo(out2));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        CodingConventions.getDefault()).createInitialScope(
            new Node(Token.BLOCK));

    assertTypeEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertTypeEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertTypeEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertTypeEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertTypeEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertTypeEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertTypeEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertTypeEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertTypeEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertTypeEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertTypeEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertTypeEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertTypeEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertTypeEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16a
  public void testTypeCheck16a() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16b
  public void testTypeCheck16b() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray1
  public void testTemplatizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray2
  public void testTemplatizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array.<number>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray3
  public void testTemplatizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray4
  public void testTemplatizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray5
  public void testTemplatizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray6
  public void testTemplatizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedArray7
  public void testTemplatizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject1
  public void testTemplatizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject2
  public void testTemplatizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject3
  public void testTemplatizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTemplatizedObject4
  public void testTemplatizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam2
  public void testOptionalArgFunctionParam2() throws Exception {
    testTypes("" +
        "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam3
  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam4
  public void testOptionalArgFunctionParam4() throws Exception {
    String expectedWarning = "Function a: called with 2 argument(s). " +
        "Function requires at least 0 argument(s) and no more than 1 " +
        "argument(s).";

    testTypes("function f(a) {a(3,4)};",
              expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParamError
  public void testOptionalArgFunctionParamError() throws Exception {
    String expectedWarning =
        "Bad type annotation. variable length argument must be last";
    testTypes("" +
              "function f(a) {};", expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam
  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam2
  public void testOptionalNullableArgFunctionParam2() throws Exception {
    testTypes("" +
              "function f(a) {a(null)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam3
  public void testOptionalNullableArgFunctionParam3() throws Exception {
    testTypes("" +
              "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn
  public void testOptionalArgFunctionReturn() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn2
  public void testOptionalArgFunctionReturn2() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()({})");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanType
  public void testBooleanType() throws Exception {
    testTypes("var x = 1 < 2;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction1
  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction2
  public void testBooleanReduction2() throws Exception {
    
    
    testTypes("" +
        "(function(s) { return ((s == 'a') && s) || 'b'; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction3
  public void testBooleanReduction3() throws Exception {
    testTypes("" +
        "(function(s) { return s && null && 3; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction4
  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction5
  public void testBooleanReduction5() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || typeof x == 'string') {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction6
  public void testBooleanReduction6() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!(x && typeof x != 'string')) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction7
   public void testBooleanReduction7() throws Exception {
     testTypes("var T = function() {};\n" +
         "\n" +
         "var f = function(x) {\n" +
         "if (!x) {\n" +
         "return x;\n" +
         "}\n" +
         "return null;\n" +
         "};");
   }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullAnd
  public void testNullAnd() throws Exception {
    testTypes("var x;\n" +
        "var r = x && x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullOr
  public void testNullOr() throws Exception {
    testTypes("var x;\n" +
        "var r = x || x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation1
  public void testBooleanPreservation1() throws Exception {
    testTypes("var x = \"a\";" +
        "x = ((x == \"a\") && x) || x == \"b\";",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation2
  public void testBooleanPreservation2() throws Exception {
    testTypes("var x = \"a\"; x = (x == \"a\") || x;",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation3
  public void testBooleanPreservation3() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "condition always evaluates to false\n" +
        "left : Function\n" +
        "right: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation4
  public void testBooleanPreservation4() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "inconsistent return type\n" +
        "found   : (boolean|null|undefined)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction1
  public void testTypeOfReduction1() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'number' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction2
  public void testTypeOfReduction2() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x != 'string' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction3
  public void testTypeOfReduction3() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'object' ? 1 : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction4
  public void testTypeOfReduction4() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'undefined' ? {} : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction5
  public void testTypeOfReduction5() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x != 'number' ? x : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction6
  public void testTypeOfReduction6() throws Exception {
    testTypes("\n" +
        "function f(x) {\n" +
        "return typeof x == 'string' && x.length == 3 ? x : 'a';\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction7
  public void testTypeOfReduction7() throws Exception {
    testTypes("var f = function(x) { " +
        "return typeof x == 'number' ? x : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction8
  public void testTypeOfReduction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isString(x) && x.length == 3 ? x : 'a';\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction9
  public void testTypeOfReduction9() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? 'a' : x;\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction10
  public void testTypeOfReduction10() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction11
  public void testTypeOfReduction11() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isObject(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction12
  public void testTypeOfReduction12() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x == 'object' ? x : []; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction13
  public void testTypeOfReduction13() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return goog.isObject(x) ? x : []; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction14
  public void testTypeOfReduction14() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(x) { " +
        "  return goog.isString(arguments[0]) ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction15
  public void testTypeOfReduction15() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(x) { " +
        "  return typeof arguments[0] == 'string' ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction1
  public void testQualifiedNameReduction1() throws Exception {
    testTypes("var x = {};  x.a = 'a';\n" +
        " var f = function() {\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction2
  public void testQualifiedNameReduction2() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return this.a ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction3
  public void testQualifiedNameReduction3() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return typeof this.a == 'string' ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction4
  public void testQualifiedNameReduction4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return goog.isString(this.a) ? this.a : 'a'; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction1
  public void testInstanceOfReduction1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return x; } else { return new T(); }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction2
  public void testInstanceOfReduction2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return ''; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInferredPropagation
  public void testPropertyInferredPropagation() throws Exception {
    testTypes("function f() { return {}; }\n" +
         "function g() { var x = f(); if (x.p) x.a = 'a'; else x.a = 'b'; }\n" +
         "function h() { var x = f(); x.a = false; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference1
  public void testPropertyInference1() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference2
  public void testPropertyInference2() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = null; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference3
  public void testPropertyInference3() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : (boolean|number)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference4
  public void testPropertyInference4() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.x_ = 3;" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference5
  public void testPropertyInference5() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference6
  public void testPropertyInference6() throws Exception {
    testTypes(
        " function F() { }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference7
  public void testPropertyInference7() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference8
  public void testPropertyInference8() throws Exception {
    testTypes(
        " function F() { " +
        "   this.x_ = 'x';" +
        "}" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "assignment to property x_ of F\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoPersistentTypeInferenceForObjectProperties
  public void testNoPersistentTypeInferenceForObjectProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoPersistentTypeInferenceForFunctionProperties
  public void testNoPersistentTypeInferenceForFunctionProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectPropertyTypeInferredInLocalScope1
  public void testObjectPropertyTypeInferredInLocalScope1() throws Exception {
    testTypes("\n" +
        "function f(o) { o.x = 1; return o.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectPropertyTypeInferredInLocalScope2
  public void testObjectPropertyTypeInferredInLocalScope2() throws Exception {
    testTypes("" +
        "function f(o, x) { o.x = 'a';\nif (x) {o.x = x;}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectPropertyTypeInferredInLocalScope3
  public void testObjectPropertyTypeInferredInLocalScope3() throws Exception {
    testTypes("" +
        "function f(o, x) { if (x) {o.x = x;} else {o.x = 'a';}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x = 0;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3()
      throws Exception {
    testTypes(" var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4()
      throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyUsedBeforeDefinition1
  public void testPropertyUsedBeforeDefinition1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "" +
        "T.prototype.f = function() { return this.g(); };\n" +
        " T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyUsedBeforeDefinition2
  public void testPropertyUsedBeforeDefinition2() throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() {};\n" +
        "" +
        "n.T.prototype.f = function() { return this.g(); };\n" +
        " n.T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd1
  public void testAdd1() throws Exception {
    testTypes("function foo(){var a = 'abc'+foo();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd2
  public void testAdd2() throws Exception {
    testTypes("function foo(){var a = foo()+4;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd3
  public void testAdd3() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd4
  public void testAdd4() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd5
  public void testAdd5() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd6
  public void testAdd6() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd7
  public void testAdd7() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd8
  public void testAdd8() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd9
  public void testAdd9() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd10
  public void testAdd10() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd11
  public void testAdd11() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd12
  public void testAdd12() throws Exception {
    testTypes(" function a() { return 5; }" +
        " var b = 5;" +
        " var c = a() + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd13
  public void testAdd13() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd14
  public void testAdd14() throws Exception {
    testTypes(" var a = null;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd15
  public void testAdd15() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd16
  public void testAdd16() throws Exception {
    testTypes(" var a = undefined;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd17
  public void testAdd17() throws Exception {
    testTypes(" var a = 5;" +
        " var b = undefined;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd18
  public void testAdd18() throws Exception {
    testTypes("function f() {};" +
        " var a = 'a';" +
        " var c = a + f();",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd19
  public void testAdd19() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd20
  public void testAdd20() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd21
  public void testAdd21() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison1
  public void testNumericComparison1() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison2
  public void testNumericComparison2() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : Object\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison3
  public void testNumericComparison3() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison4
  public void testNumericComparison4() throws Exception {
    testTypes(" " +
              "function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison5
  public void testNumericComparison5() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : *\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison6
  public void testNumericComparison6() throws Exception {
    testTypes(" function foo() { if (3 >= foo()) return; }",
        "right side of numeric comparison\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison1
  public void testStringComparison1() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison2
  public void testStringComparison2() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison3
  public void testStringComparison3() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison4
  public void testStringComparison4() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison5
  public void testStringComparison5() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison6
  public void testStringComparison6() throws Exception {
    testTypes(" " +
        "function foo() { if ('a' >= foo()) return; }",
        "right side of comparison\n" +
        "found   : undefined\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueOfComparison1
  public void testValueOfComparison1() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        " function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueOfComparison2
  public void testValueOfComparison2() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueOfComparison3
  public void testValueOfComparison3() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.toString = function() { return 'o'; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGenericRelationalExpression
  public void testGenericRelationalExpression() throws Exception {
    testTypes(" " +
                  "function f(a,b) {return a < b;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof1
  public void testInstanceof1() throws Exception {
    testTypes("function foo(){" +
        "if (bar instanceof 3)return;}",
        "instanceof requires an object\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof2
  public void testInstanceof2() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}",
        "deterministic instanceof yields false\n" +
        "found   : undefined\n" +
        "required: NoObject");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof3
  public void testInstanceof3() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof4
  public void testInstanceof4() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof5
  public void testInstanceof5() throws Exception {
    
    testTypes(" function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof6
  public void testInstanceof6() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction3
  public void testInstanceOfReduction3() throws Exception {
    testTypes(
        "\n" +
        "var f = function(x, y) {\n" +
        "  return x instanceof y;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping1
  public void testScoping1() throws Exception {
    testTypes(
        "function foo(a){" +
        "  function bar(a){" +
        "    if (a instanceof Array)return;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping2
  public void testScoping2() throws Exception {
    testTypes(
        " var a;" +
        "function Foo() {" +
        "   var a;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping3
  public void testScoping3() throws Exception {
    testTypes("\n\nvar b;\nvar b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:3 with type (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping4
  public void testScoping4() throws Exception {
    testTypes("var b; if (true) var b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:1 with type (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping5
  public void testScoping5() throws Exception {
    
    
    testTypes("if (true) var b; var b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping6
  public void testScoping6() throws Exception {
    
    
    testTypes("if (true) var b; if (true) var b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping7
  public void testScoping7() throws Exception {
    testTypes("function A() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of A\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping8
  public void testScoping8() throws Exception {
    testTypes("function A() {}" +
        "function B() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping9
  public void testScoping9() throws Exception {
    testTypes("function B() {" +
        "  this.a = null;" +
        "}" +
        "function A() {}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping10
  public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping11
  public void testScoping11() throws Exception {
    
    
    testTypes(
        "var a = function b(){ return b };",
        "inconsistent return type\n" +
        "found   : function (): number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments1
  public void testFunctionArguments1() throws Exception {
    testFunctionType(
        "" +
        "function f(a) {}",
        "function (number): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments2
  public void testFunctionArguments2() throws Exception {
    testFunctionType(
        "" +
        "function f(opt_a) {}",
        "function (number=): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments3
  public void testFunctionArguments3() throws Exception {
    testFunctionType(
        "" +
        "function f(a,b) {}",
        "function (?, number): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments4
  public void testFunctionArguments4() throws Exception {
    testFunctionType(
        "" +
        "function f(a,opt_a) {}",
        "function (?, number=): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments5
  public void testFunctionArguments5() throws Exception {
    testTypes(
        "function a(opt_a,a) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments6
  public void testFunctionArguments6() throws Exception {
    testTypes(
        "function a(var_args,a) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments7
  public void testFunctionArguments7() throws Exception {
    testTypes(
        "" +
        "function a(a,opt_a,var_args) {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments8
  public void testFunctionArguments8() throws Exception {
    testTypes(
        "function a(a,opt_a,var_args,b) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments9
  public void testFunctionArguments9() throws Exception {
    
    testTypes(
        "function a(a,opt_a,var_args,b,c) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments10
  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments11
  public void testFunctionArguments11() throws Exception {
    testTypes(
        "function a(a,opt_a,b,c,var_args,d) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments12
  public void testFunctionArguments12() throws Exception {
    testTypes("function bar(baz){}",
        "parameter foo does not appear in bar's parameter list");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments13
  public void testFunctionArguments13() throws Exception {
    
    testTypes(
        " function u() { return true; }" +
        "" +
        "function f(b) { if (u()) { b = null; } return b; }",
        "assignment\n" +
        "found   : null\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments14
  public void testFunctionArguments14() throws Exception {
    testTypes(
        " function f(x, opt_y, var_args) {}" +
        "f('3'); f('3', 2); f('3', 2, true); f('3', 2, true, false);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments15
  public void testFunctionArguments15() throws Exception {
    testTypes(
        "" +
        "function g(f) { f(1, 2); }",
        "Function f: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrintFunctionName1
  public void testPrintFunctionName1() throws Exception {
    
    testTypes(
        "var goog = {}; goog.run = function(f) {};" +
        "goog.run();",
        "Function goog.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrintFunctionName2
  public void testPrintFunctionName2() throws Exception {
    testTypes(
        " var Foo = function() {}; " +
        "Foo.prototype.run = function(f) {};" +
        "(new Foo).run();",
        "Function Foo.prototype.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference1
  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference2
  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference3
  public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference4
  public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference5
  public void testFunctionInference5() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference6
  public void testFunctionInference6() throws Exception {
    testFunctionType(
        "function f(opt_a) {}",
        "function (this:Date, ?=): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference7
  public void testFunctionInference7() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference8
  public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference9
  public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference10
  public void testFunctionInference10() throws Exception {
    testFunctionType(
        "" +
        "var f = function(a,b) {};",
        "function (this:Date, ?, boolean): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference11
  public void testFunctionInference11() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference12
  public void testFunctionInference12() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference13
  public void testFunctionInference13() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(f){};",
        "eatFoo",
        "function (goog.Foo): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference14
  public void testFunctionInference14() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(){ return new goog.Foo; };",
        "eatFoo",
        "function (): goog.Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference15
  public void testFunctionInference15() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "f.prototype.foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference16
  public void testFunctionInference16() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference17
  public void testFunctionInference17() throws Exception {
    testFunctionType(
        " function f() {}" +
        "function abstractMethod() {}" +
        " f.prototype.foo = abstractMethod;",
        "(new f).foo",
        "function (this:f, number): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference18
  public void testFunctionInference18() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.eatWithDate;",
        "goog.eatWithDate",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference19
  public void testFunctionInference19() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (string): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference20
  public void testFunctionInference20() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction1
  public void testInnerFunction1() throws Exception {
    testTypes(
        "function f() {" +
        "  var x = 3;\n" +
        " function g() { x = null; }" +
        " return x;" +
        "}",
        "assignment\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction2
  public void testInnerFunction2() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = null;\n" +
        " function g() { x = 3; }" +
        " g();" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : (null|number)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction3
  public void testInnerFunction3() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = 3;\n" +
        " \n" +
        " function g() { x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction4
  public void testInnerFunction4() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = '3';\n" +
        " \n" +
        " function g() { x = 3; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction5
  public void testInnerFunction5() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = 3;\n" +
        " " +
        " function g() { var x = 3;x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction6
  public void testInnerFunction6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction7
  public void testInnerFunction7() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " " +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction8
  public void testInnerFunction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " function x() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInnerFunction9
  public void testInnerFunction9() throws Exception {
    testTypes(
        "function f() {" +
        " var x = 3;\n" +
        " function g() { x = null; };\n" +
        " function h() { return x == null; }" +
        " return h();" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling1
  public void testAbstractMethodHandling1() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        "abstractFn(1);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling2
  public void testAbstractMethodHandling2() throws Exception {
    testTypes(
        "var abstractFn = function() {};" +
        "abstractFn(1);",
        "Function abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling3
  public void testAbstractMethodHandling3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        "goog.abstractFn(1);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling4
  public void testAbstractMethodHandling4() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.abstractFn = function() {};" +
        "goog.abstractFn(1);",
        "Function goog.abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling5
  public void testAbstractMethodHandling5() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        " var f = abstractFn;" +
        "f('x');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAbstractMethodHandling6
  public void testAbstractMethodHandling6() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        " goog.f = abstractFn;" +
        "goog.f('x');",
        "actual parameter 1 of goog.f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference1
  public void testMethodInference1() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function() { return 3; };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference2
  public void testMethodInference2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.F = function() {};" +
        " goog.F.prototype.foo = " +
        "    function() { return 3; };" +
        " " +
        "goog.G = function() {};" +
        " goog.G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference3
  public void testMethodInference3() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference4
  public void testMethodInference4() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(y) { return y; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference5
  public void testMethodInference5() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 'x'; };" +
        " " +
        "function G() {}" +
        " G.prototype.num = 3;" +
        " " +
        "G.prototype.foo = function(y) { return this.num + y; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference6
  public void testMethodInference6() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function(x) { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { };" +
        "(new G()).foo(1);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference7
  public void testMethodInference7() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function(x, y) { };",
        "mismatch of the foo property type and the type of the property " +
        "it overrides from superclass F\n" +
        "original: function (this:F): undefined\n" +
        "override: function (this:G, ?, ?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference8
  public void testMethodInference8() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(opt_b, var_args) { };" +
        "(new G()).foo(1, 2, 3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMethodInference9
  public void testMethodInference9() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(var_args, opt_b) { };",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticMethodDeclaration1
  public void testStaticMethodDeclaration1() throws Exception {
    testTypes(
        " function F() { F.foo(true); }" +
        " F.foo = function(x) {};",
        "actual parameter 1 of F.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticMethodDeclaration2
  public void testStaticMethodDeclaration2() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        " goog.foo = function(x) {};",
        "actual parameter 1 of goog.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticMethodDeclaration3
  public void testStaticMethodDeclaration3() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        "goog.foo = function() {};",
        "Function goog.foo: called with 1 argument(s). Function requires " +
        "at least 0 argument(s) and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl1
  public void testDuplicateStaticMethodDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (number): undefined, " +
        "original definition at [testcode]:1 " +
        "with type function (number): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl2
  public void testDuplicateStaticMethodDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " " +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl3
  public void testDuplicateStaticMethodDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl4
  public void testDuplicateStaticMethodDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticMethodDecl5
  public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl1
  public void testDuplicateStaticPropertyDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl2
  public void testDuplicateStaticPropertyDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl3
  public void testDuplicateStaticPropertyDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl4
  public void testDuplicateStaticPropertyDecl4() throws Exception {
    testClosureTypesMultipleWarnings(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        Lists.newArrayList(
            "assignment to property foo of goog\n" +
            "found   : string\n" +
            "required: Foo",
            "variable goog.foo redefined with type string, " +
            "original definition at [testcode]:1 with type Foo"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl5
  public void testDuplicateStaticPropertyDecl5() throws Exception {
    testClosureTypesMultipleWarnings(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        Lists.newArrayList(
            "assignment to property foo of goog\n" +
            "found   : string\n" +
            "required: Foo",
            "variable goog.foo redefined with type string, " +
            "original definition at [testcode]:1 with type Foo"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl6
  public void testDuplicateStaticPropertyDecl6() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = 'y';" +
        " goog.foo = 'x';");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl7
  public void testDuplicateStaticPropertyDecl7() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl8
  public void testDuplicateStaticPropertyDecl8() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " function EventCopy() {}" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateStaticPropertyDecl9
  public void testDuplicateStaticPropertyDecl9() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function EventCopy() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateLocalVarDecl
  public void testDuplicateLocalVarDecl() throws Exception {
    testClosureTypesMultipleWarnings(
        "\n" +
        "function f(x) {  var x = ''; }",
        Lists.newArrayList(
            "variable x redefined with type string, original definition" +
            " at [testcode]:2 with type number",
            "initializing variable\n" +
            "found   : string\n" +
            "required: number"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration1
  public void testStubFunctionDeclaration1() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.prototype.foo;",
        "(new f).foo",
        "function (this:f, number, string): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration2
  public void testStubFunctionDeclaration2() throws Exception {
    testExternFunctionType(
        
        " function f() {};" +
        " f.subclass;",
        "f.subclass",
        "function (new:f.subclass): ?");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration3
  public void testStubFunctionDeclaration3() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.foo;",
        "f.foo",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration4
  public void testStubFunctionDeclaration4() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        "function (this:f): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration5
  public void testStubFunctionDeclaration5() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration6
  public void testStubFunctionDeclaration6() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo;",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration7
  public void testStubFunctionDeclaration7() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo = function() {};",
        "(new f).foo",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration8
  public void testStubFunctionDeclaration8() throws Exception {
    testFunctionType(
        " var f = function() {}; ",
        "f",
        createOptionalType(createNullableType(U2U_CONSTRUCTOR_TYPE))
            .toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration9
  public void testStubFunctionDeclaration9() throws Exception {
    testFunctionType(
        " var f; ",
        "f",
        "function (): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubFunctionDeclaration10
  public void testStubFunctionDeclaration10() throws Exception {
    testFunctionType(
        " var f = function(x) {};",
        "f",
        "function (number): number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNestedFunctionInference1
  public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        " function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeRedefinition
  public void testTypeRedefinition() throws Exception {
    testClosureTypesMultipleWarnings(
        "a={}; a.A = {ZOR:'b'};"
        + " a.A = function() {}",
        Lists.newArrayList(
            "variable a.A redefined with type function (new:a.A): undefined, " +
            "original definition at [testcode]:1 with type enum{a.A}",
            "assignment to property A of a\n" +
            "found   : function (new:a.A): undefined\n" +
            "required: enum{a.A}"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn1
  public void testIn1() throws Exception {
    testTypes("'foo' in Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn2
  public void testIn2() throws Exception {
    testTypes("3 in Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn3
  public void testIn3() throws Exception {
    testTypes("undefined in Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn4
  public void testIn4() throws Exception {
    testTypes("Date in Object",
        "left side of 'in'\n" +
        "found   : function (new:Date, ?=, ?=, ?=, ?=, ?=, ?=, ?=): string\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn5
  public void testIn5() throws Exception {
    testTypes("'x' in null",
        "'in' requires an object\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn6
  public void testIn6() throws Exception {
    testTypes(
        "" +
        "function g(x) {}" +
        "g(1 in {});",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIn7
  public void testIn7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  return g(x.foo) in {};" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison2
  public void testComparison2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "if (a!==b) {}",
        "condition always evaluates to true\n" +
        "left : number\n" +
        "right: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComparison3
  public void testComparison3() throws Exception {
    
    testTypes("var a;" +
        "var b = a == null");
  }
