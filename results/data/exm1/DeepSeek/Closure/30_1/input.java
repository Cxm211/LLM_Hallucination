// buggy code
  public void process(Node externs, Node root) {
    (new NodeTraversal(compiler, this)).traverse(root);
  }

  MustBeReachingVariableDef(
      ControlFlowGraph<Node> cfg, Scope jsScope, AbstractCompiler compiler) {
    super(cfg, new MustDefJoin());
    this.jsScope = jsScope;
    this.compiler = compiler;
    this.escaped = Sets.newHashSet();
    computeEscaped(jsScope, escaped, compiler);
  }

  private void computeDependence(final Definition def, Node rValue) {
    NodeTraversal.traverse(compiler, rValue,
        new AbstractCfgNodeTraversalCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isName() && jsScope.isDeclared(n.getString(), true)) {
          Var dep = jsScope.getVar(n.getString());
            def.depends.add(dep);
        }
      }
    });
  }

  boolean dependsOnOuterScopeVars(String name, Node useNode) {
    Preconditions.checkArgument(getCfg().hasNode(useNode));
    GraphNode<Node, Branch> n = getCfg().getNode(useNode);
    FlowState<MustDef> state = n.getAnnotation();
    Definition def = state.getIn().reachingDef.get(jsScope.getVar(name));

    for (Var s : def.depends) {
      if (s.scope != jsScope) {
        return true;
      }
    }
    return false;
  }

// relevant test
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

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testStraightLine
  public void testStraightLine() {
    assertMatch("D:var x=1; U: x");
    assertMatch("var x; D:x=1; U: x");
    assertNotMatch("D:var x=1; x = 2; U: x");
    assertMatch("var x=1; D:x=2; U: x");
    assertNotMatch("U:x; D:var x = 1");
    assertNotMatch("D:var x; U:x; x=1");
    assertNotMatch("D:var x; U:x; x=1; x");
    assertMatch("D: var x = 1; var y = 2; y; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testIf
  public void testIf() {
    assertNotMatch("var x; if(a){ D:x=1 } else { x=2 }; U:x");
    assertNotMatch("var x; if(a){ x=1 } else { D:x=2 }; U:x");
    assertMatch("D:var x=1; if(a){ U:x } else { x };");
    assertMatch("D:var x=1; if(a){ x } else { U:x };");
    assertNotMatch("var x; if(a) { D: x = 1 }; U:x;");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testLoops
  public void testLoops() {
    assertNotMatch("var x=0; while(a){ D:x=1 }; U:x");
    assertNotMatch("var x=0; for(;;) { D:x=1 }; U:x");
    assertMatch("D:var x=1; while(a) { U:x }");
    assertMatch("D:var x=1; for(;;)  { U:x }");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testConditional
  public void testConditional() {
    assertMatch("var x=0,y; D:(x=1)&&y; U:x");
    assertNotMatch("var x=0,y; D:y&&(x=1); U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testUseAndDefInSameInstruction
  public void testUseAndDefInSameInstruction() {
    assertMatch("D:var x=0; U:x=1,x");
    assertMatch("D:var x=0; U:x,x=1");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testAssignmentInExpressions
  public void testAssignmentInExpressions() {
    assertMatch("var x=0; D:foo(bar(x=1)); U:x");
    assertMatch("var x=0; D:foo(bar + (x = 1)); U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testHook
  public void testHook() {
    assertNotMatch("var x=0; D:foo() ? x=1 : bar(); U:x");
    assertNotMatch("var x=0; D:foo() ? x=1 : x=2; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testExpressionVariableReassignment
  public void testExpressionVariableReassignment() {
    assertMatch("var a,b; D: var x = a + b; U:x");
    assertNotMatch("var a,b,c; D: var x = a + b; a = 1; U:x");
    assertNotMatch("var a,b,c; D: var x = a + b; f(b = 1); U:x");
    assertMatch("var a,b,c; D: var x = a + b; c = 1; U:x");

    
    assertNotMatch("var a,b,c; D: var x = a + b; c ? a = 1 : 0; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testMergeDefinitions
  public void testMergeDefinitions() {
    assertNotMatch("var x,y; D: y = x + x; if(x) { x = 1 }; U:y");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testMergesWithOneDefinition
  public void testMergesWithOneDefinition() {
    assertNotMatch(
        "var x,y; while(y) { if (y) { print(x) } else { D: x = 1 } } U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testRedefinitionUsingItself
  public void testRedefinitionUsingItself() {
    assertMatch("var x = 1; D: x = x + 1; U:x;");
    assertNotMatch("var x = 1; D: x = x + 1; x = 1; U:x;");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testMultipleDefinitionsWithDependence
  public void testMultipleDefinitionsWithDependence() {
    assertMatch("var x, a, b; D: x = a, x = b; U: x");
    assertMatch("var x, a, b; D: x = a, x = b; a = 1; U: x");
    assertNotMatch("var x, a, b; D: x = a, x = b; b = 1; U: x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testExterns
  public void testExterns() {
    assertNotMatch("D: goog = {}; U: goog");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testAssignmentOp
  public void testAssignmentOp() {
    assertMatch("var x = 0; D: x += 1; U: x");
    assertMatch("var x = 0; D: x *= 1; U: x");
    assertNotMatch("D: var x = 0; x += 1; U: x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testIncAndDec
  public void testIncAndDec() {
    assertMatch("var x; D: x++; U: x");
    assertMatch("var x; D: x--; U: x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testFunctionParams1
  public void testFunctionParams1() {
    computeDefUse("if (param2) { D: param1 = 1; U: param1 }");
    assertSame(def, defUse.getDef("param1", use));
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testFunctionParams2
  public void testFunctionParams2() {
    computeDefUse("if (param2) { D: param1 = 1} U: param1");
    assertNotSame(def, defUse.getDef("param1", use));
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testArgumentsObjectModifications
  public void testArgumentsObjectModifications() {
    computeDefUse("D: param1 = 1; arguments[0] = 2; U: param1");
    assertNotSame(def, defUse.getDef("param1", use));
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testArgumentsObjectEscaped
  public void testArgumentsObjectEscaped() {
    computeDefUse("D: param1 = 1; var x = arguments; x[0] = 2; U: param1");
    assertNotSame(def, defUse.getDef("param1", use));
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testArgumentsObjectEscapedDependents
  public void testArgumentsObjectEscapedDependents() {
    assertNotMatch("param1=1; var x; D:x=param1; var y=arguments; U:x");
  }
