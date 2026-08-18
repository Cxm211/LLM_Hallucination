// buggy code
  public TypePair getTypesUnderInequality(JSType that) {
    // unions types
    if (that instanceof UnionType) {
      TypePair p = that.getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }

    // other types
    switch (this.testForEquality(that)) {
      case TRUE:
        return new TypePair(null, null);

      case FALSE:
      case UNKNOWN:
        return new TypePair(this, that);
    }

    // switch case is exhaustive
    throw new IllegalStateException();
  }

// relevant test
// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties
  public void testCheckUndefinedProperties() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function (a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = true, BAR = 5, CCC = true, DDD = true;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {}; goog.dom = {};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70
  public void testIssue70() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    testSame("function f() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         }, ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn
  public void testSourcePruningOn() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         },
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = true;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = true;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testEquals
  public void testEquals() {
    ConcreteFunctionType fun1 = createFunction("fun1");
    ConcreteFunctionType fun2 = createFunction("fun2");
    ConcreteType obj1 = fun1.getInstanceType();
    ConcreteType obj2 = fun2.getInstanceType();
    ConcreteType union1 = new ConcreteUnionType(fun1, fun2);
    ConcreteType union2 = new ConcreteUnionType(fun1, obj1);
    ConcreteType union3 = new ConcreteUnionType(fun1, obj1);

    checkEquality(Lists.newArrayList(fun1, fun2, obj1, obj2,
                                     union1, union2));

    assertEquals(union2, union3);
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testUnionWith
  public void testUnionWith() {
    ConcreteFunctionType fun = createFunction("fun");
    ConcreteType obj = fun.getInstanceType();
    ConcreteType both = new ConcreteUnionType(fun, obj);

    assertTrue(fun.isSingleton());
    assertTrue(obj.isSingleton());
    assertFalse(both.isSingleton());
    assertFalse(NONE.isSingleton());
    assertFalse(ALL.isSingleton());

    checkUnionWith(fun, NONE, fun);
    checkUnionWith(fun, ALL, ALL);

    checkUnionWith(fun, obj, both);
    checkUnionWith(both, NONE, both);
    checkUnionWith(both, ALL, ALL);
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testIntersectionWith
  public void testIntersectionWith() {
    ConcreteFunctionType fun = createFunction("fun");
    ConcreteFunctionType fun2 = createFunction("fun2");
    ConcreteType obj = fun.getInstanceType();
    ConcreteType both = new ConcreteUnionType(fun, obj);

    assertEquals(NONE, fun.intersectWith(obj));
    assertEquals(NONE, obj.intersectWith(fun));

    assertEquals(fun, both.intersectWith(fun));
    assertEquals(fun, fun.intersectWith(both));

    assertEquals(NONE, NONE.intersectWith(both));
    assertEquals(NONE, both.intersectWith(NONE));
    assertEquals(NONE, fun.intersectWith(NONE));
    assertEquals(NONE, NONE.intersectWith(fun));

    assertEquals(NONE, both.intersectWith(fun2));

    assertEquals(both, ALL.intersectWith(both));
    assertEquals(both, both.intersectWith(ALL));
    assertEquals(fun, ALL.intersectWith(fun));
    assertEquals(fun, fun.intersectWith(ALL));
    assertEquals(NONE, ALL.intersectWith(NONE));
    assertEquals(NONE, NONE.intersectWith(ALL));
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testFunction
  public void testFunction() {
    ConcreteFunctionType fun = createFunction("fun", "a", "b");
    assertTrue(fun.isFunction());
    assertNotNull(fun.getCallSlot());
    assertNotNull(fun.getReturnSlot());
    assertNotNull(fun.getParameterSlot(0));
    assertNotNull(fun.getParameterSlot(1));
    assertNull(fun.getParameterSlot(2));
    assertTrue(fun.getInstanceType().isInstance());
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testInstance
  public void testInstance() {
    ConcreteInstanceType obj = createInstance("MyObj", "a", "b");
    assertTrue(obj.isInstance());
    assertNotNull(obj.getPropertySlot("a"));
    assertNotNull(obj.getPropertySlot("b"));
    assertNull(obj.getPropertySlot("c"));

    
    
    for (int i = 0; i < 4; ++i) {
      assertNotNull(obj = obj.getImplicitPrototype());
      assertTrue(obj.isInstance());
    }
    assertNull(obj.getImplicitPrototype());
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testGetX
  public void testGetX() {
    ConcreteFunctionType fun1 = createFunction("fun1");
    ConcreteFunctionType fun2 = createFunction("fun2");
    ConcreteInstanceType obj1 = fun1.getInstanceType();
    ConcreteInstanceType obj2 = fun2.getInstanceType();
    ConcreteType union1 = fun1.unionWith(obj1);
    ConcreteType union2 =
        union1.unionWith(fun2).unionWith(obj2);

    assertEqualSets(Lists.newArrayList(), NONE.getFunctions());
    assertEqualSets(Lists.newArrayList(), NONE.getInstances());
    assertEqualSets(Lists.newArrayList(fun1), fun1.getFunctions());
    assertEqualSets(Lists.newArrayList(), fun1.getInstances());
    assertEqualSets(Lists.newArrayList(), obj1.getFunctions());
    assertEqualSets(Lists.newArrayList(obj1), obj1.getInstances());

    assertEqualSets(Lists.newArrayList(fun1), union1.getFunctions());
    assertEqualSets(Lists.newArrayList(obj1), union1.getInstances());

    assertEqualSets(Lists.newArrayList(fun1, fun2), union2.getFunctions());
    assertEqualSets(Lists.newArrayList(obj1, obj2), union2.getInstances());
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
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"TRY\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"BLOCK\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"THROW\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node4 [label=\"NAME\"];\n" +
      "  node3 -> node4 [weight=1];\n" +
      "  node5 [label=\"BLOCK\"];\n" +
      "  node3 -> node5 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node6 [label=\"EXPR_RESULT\"];\n" +
      "  node2 -> node6 [weight=1];\n" +
      "  node7 [label=\"CALL\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node8 [label=\"NAME\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"EXPR_RESULT\"];\n" +
      "  node6 -> node5 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node6 -> node9 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> node9 [weight=1];\n" +
      "  node10 [label=\"GETELEM\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node12 [label=\"STRING\"];\n" +
      "  node10 -> node12 [weight=1];\n" +
      "  node13 [label=\"EXPR_RESULT\"];\n" +
      "  node9 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 -> node5 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> node13 [weight=1];\n" +
      "  node14 [label=\"GETPROP\"];\n" +
      "  node13 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node16 [label=\"STRING\"];\n" +
      "  node14 -> node16 [weight=1];\n" +
      "  node17 [label=\"EXPR_RESULT\"];\n" +
      "  node13 -> node17 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node13 -> node5 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> node17 [weight=1];\n" +
      "  node18 [label=\"NAME\"];\n" +
      "  node17 -> node18 [weight=1];\n" +
      "  node17 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node5 [weight=1];\n" +
      "  node19 [label=\"CATCH\"];\n" +
      "  node5 -> node19 [weight=1];\n" +
      "  node20 [label=\"NAME\"];\n" +
      "  node19 -> node20 [weight=1];\n" +
      "  node21 [label=\"EMPTY\"];\n" +
      "  node19 -> node21 [weight=1];\n" +
      "  node22 [label=\"BLOCK\"];\n" +
      "  node19 -> node22 [weight=1];\n" +
      "  node23 [label=\"EXPR_RESULT\"];\n" +
      "  node22 -> node23 [weight=1];\n" +
      "  node24 [label=\"CALL\"];\n" +
      "  node23 -> node24 [weight=1];\n" +
      "  node25 [label=\"NAME\"];\n" +
      "  node24 -> node25 [weight=1];\n" +
      "  node23 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node22 -> node23 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node19 -> node22 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node5 -> node19 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node2 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testFunctionWithinTry
  public void testFunctionWithinTry() {
    
    String src = "try { function f() {throw 1;} } catch (e) { }";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"TRY\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"BLOCK\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"FUNCTION\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node4 [label=\"NAME\"];\n" +
      "  node3 -> node4 [weight=1];\n" +
      "  node5 [label=\"LP\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"BLOCK\"];\n" +
      "  node3 -> node6 [weight=1];\n" +
      "  node7 [label=\"THROW\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node8 [label=\"NUMBER\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node6 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node6 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 [label=\"BLOCK\"];\n" +
      "  node1 -> node9 [weight=1];\n" +
      "  node10 [label=\"CATCH\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node12 [label=\"EMPTY\"];\n" +
      "  node10 -> node12 [weight=1];\n" +
      "  node13 [label=\"BLOCK\"];\n" +
      "  node10 -> node13 [weight=1];\n" +
      "  node13 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node10 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node2 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
    "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedCatch
  public void testNestedCatch() {
    
    String src = "try{try{throw 1;}catch(e){throw 2}}catch(f){}";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"TRY\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"BLOCK\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"TRY\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node4 [label=\"BLOCK\"];\n" +
      "  node3 -> node4 [weight=1];\n" +
      "  node5 [label=\"THROW\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node5 -> node7 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node5 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node7 [weight=1];\n" +
      "  node8 [label=\"CATCH\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"EMPTY\"];\n" +
      "  node8 -> node10 [weight=1];\n" +
      "  node11 [label=\"BLOCK\"];\n" +
      "  node8 -> node11 [weight=1];\n" +
      "  node12 [label=\"THROW\"];\n" +
      "  node11 -> node12 [weight=1];\n" +
      "  node13 [label=\"NUMBER\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node14 [label=\"BLOCK\"];\n" +
      "  node12 -> node14 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node11 -> node12 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> node11 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node14 [weight=1];\n" +
      "  node15 [label=\"CATCH\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node16 [label=\"NAME\"];\n" +
      "  node15 -> node16 [weight=1];\n" +
      "  node17 [label=\"EMPTY\"];\n" +
      "  node15 -> node17 [weight=1];\n" +
      "  node18 [label=\"BLOCK\"];\n" +
      "  node15 -> node18 [weight=1];\n" +
      "  node18 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node15 -> node18 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node14 -> node15 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node2 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
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
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"TRY\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"BLOCK\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"IF\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node4 [label=\"NAME\"];\n" +
      "  node3 -> node4 [weight=1];\n" +
      "  node5 [label=\"BLOCK\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"THROW\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"NUMBER\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node8 [label=\"BLOCK\"];\n" +
      "  node6 -> node8 " +
      "[label=\"ON_EX\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node5 -> node6 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 [label=\"BLOCK\"];\n" +
      "  node3 -> node9 [weight=1];\n" +
      "  node10 [label=\"EXPR_RESULT\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node12 [label=\"BLOCK\"];\n" +
      "  node10 -> node12 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node5 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node9 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node2 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node8 [weight=1];\n" +
      "  node13 [label=\"CATCH\"];\n" +
      "  node8 -> node13 [weight=1];\n" +
      "  node14 [label=\"NAME\"];\n" +
      "  node13 -> node14 [weight=1];\n" +
      "  node15 [label=\"EMPTY\"];\n" +
      "  node13 -> node15 [weight=1];\n" +
      "  node16 [label=\"BLOCK\"];\n" +
      "  node13 -> node16 [weight=1];\n" +
      "  node17 [label=\"EXPR_RESULT\"];\n" +
      "  node16 -> node17 [weight=1];\n" +
      "  node18 [label=\"NAME\"];\n" +
      "  node17 -> node18 [weight=1];\n" +
      "  node17 -> node12 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node16 -> node17 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node13 -> node16 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node12 [weight=1];\n" +
      "  node19 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node19 [weight=1];\n" +
      "  node20 [label=\"NAME\"];\n" +
      "  node19 -> node20 [weight=1];\n" +
      "  node19 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node19 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node2 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
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
    String src = "START(); var x; END()";
    ControlFlowGraph<Node> cfg = createCfg(src, true);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.SYN_BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testPartialTraversalOfScope
  public void testPartialTraversalOfScope() {
    Compiler compiler = new Compiler();
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);

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
            "label: for (var x in y) { if (x) { break label; } else { i++ } x(); }"),
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

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function() { if (x) return; y(); }",
         "function(){if(!x)y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function() { if (x) return; y(); if (a) return; b(); }",
         "function(){if(!x){y();if(!a)b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function(){startMarker(\"FOO\");startMarker(\"BAR\");" +
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

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testDenormalize
  public void testDenormalize() {
    testSame("startMarker();for(;;);endMarker()");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testNonMarkingUse
  public void testNonMarkingUse() {
    testSame("function foo(endMarker){}");
    testSame("function foo(){startMarker:foo()}");
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

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod
  public void testMovePrototypeMethod() {
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
    inFunction("var x; x=2; if(0&&x=1){}; x");

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

    
    inFunction("var a; a = (a++)", "var a; void 0");
    inFunction("var a; a = (++a)", "var a; void 0");

    inFunction("var a; a = (b = (a = 1))", "var a; b = 1");
    inFunction("var a; a = (b = (a *= 2))", "var a; b = a * 2");
    inFunction("var a; a = (b = (a++))", "var a; b=a++");
    inFunction("var a; a = (b = (++a))", "var a; b=++a");

    
    inFunction("var a,b; a = (b = (a = 1))", "var a,b; 1");
    inFunction("var a,b; a = (b = (a *= 2))", "var a,b; a * 2");
    inFunction("var a,b; a = (b = (a++))", "var a,b; void 0");
    inFunction("var a,b; a = (b = (++a))", "var a,b; void 0");

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
    inFunction("extern = true;", "extern = true;");
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

    
    test("function(){ var a; for(; a < 2 ; a++) foo() }",
         "function(){ for(var a; a < 2 ; a++) foo() }");
    testSame("function(){ return; for(; a < 2 ; a++) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testInOperatorNotInsideFor
  public void testInOperatorNotInsideFor() {
    
    
    
    

    
    testSame("function(){ var a; var i=\"length\" in a;" +
        "for(; a < 2 ; a++) foo() }");
    
    testSame("function(){ var a; var i=(\"length\" in a);" +
        "for(; a < 2 ; a++) foo() }");
    
    
    test("function(){var b,a=0; for (var i=(\"length\" in b);a<2; a++) foo()}",
         "function(){var b; var a=0;var i=(\"length\" in b);" +
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType
  public void testOneType() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes
  public void testTwoTypes() {
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCast
  public void testCast() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "((F)).a = 0;";
    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Bar_prototype$a=0;";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Unique$1$a=0;";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testConstructorFields
  public void testConstructorFields() {
    String js = ""
      + "\n"
      + "var Foo = function() { this.a = 0; };\n"
      + " function Bar() {}\n"
      + "Bar.prototype.a = 0;"
      + "new Foo";
    String output = ""
        + "var Foo=function(){this.Foo$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    String ttOutput = ""
        + "var Foo=function(){this.Foo_prototype$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo]]}");
    testSets(true, js, ttOutput, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStaticProperty
  public void testStaticProperty() {
    String js = ""
      + " function Foo() {} \n"
      + " function Bar() {}\n"
      + "Foo.a = 0;"
      + "Bar.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "function Bar(){}"
        + "Foo.function__this_Foo___undefined$a = 0;"
        + "Bar.function__this_Bar___undefined$a = 0;";

    testSets(false, js, output,
        "{a=[[function (this:Bar): undefined]," +
        " [function (this:Foo): undefined]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeWithSameField
  public void testSupertypeWithSameField() {
    String js = ""
      + " function Foo() {}\n"
      + "Foo.prototype.a = 0;\n"
      + " function Bar() {}\n"
      + "\n"
      + "Bar.prototype.a = 0;\n"
      + " var B = new Bar;\n"
      + "B.a = 0;"
      + " function Baz() {}\n"
      + "Baz.prototype.a = function(){};\n";

    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Foo_prototype$a=0;"
        + "var B = new Bar;B.Foo_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var B = new Bar;B.Bar_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    testSets(false, js, output, "{a=[[Baz.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Baz.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testScopedType
  public void testScopedType() {
    String js = ""
        + "var g = {};\n"
        + " g.Foo = function() {}\n"
        + "g.Foo.prototype.a = 0;"
        + " g.Bar = function() {}\n"
        + "g.Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "g.Foo=function(){};"
        + "g.Foo.prototype.g_Foo_prototype$a=0;"
        + "g.Bar=function(){};"
        + "g.Bar.prototype.g_Bar_prototype$a=0;";
    testSets(false, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
    testSets(true, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnresolvedType
  public void testUnresolvedType() {
    String js = ""
        + "var g = {};"
        + " var Foo = function() {}\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {}\n"
        + "Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, js, js, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testNamedType
  public void testNamedType() {
    String js = ""
        + "var g = {};"
        + " var Foo = function() {}\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {}\n"
        + "Bar.prototype.a = 0;"
        + " g.Late = function() {}";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "g.Late = function(){}";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnknownType
  public void testUnknownType() {
    String js = ""
        + " var Foo = function() {};\n"
        + " var Bar = function() {};\n"
        + " function fun() {}\n"
        + "Foo.prototype.a = fun();\n"
        + "fun().a;\n"
        + "Bar.prototype.a = 0;";
    String ttOutput = ""
        + "var Foo=function(){};\n"
        + "var Bar=function(){};\n"
        + "function fun(){}\n"
        + "Foo.prototype.Foo_prototype$a=fun();\n"
        + "fun().Unique$1$a;\n"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, js, js, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES, js, ttOutput,
             "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnum
  public void testEnum() {
    String js = ""
      + " var En = {\n"
      + "  A: 'first',\n"
      + "  B: 'second'\n"
      + "};\n"
      + "var EA = En.A;\n"
      + "var EB = En.B;\n"
      + " function Foo(){};\n"
      + "Foo.prototype.A = 0;\n"
      + "Foo.prototype.B = 0;\n";
    String output = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.A=0;"
        + "Foo.prototype.B=0";
    String ttOutput = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    testSets(false, js, output, "{}");
    testSets(true, js, ttOutput, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUntypedExterns
  public void testUntypedExterns() {
    String externs =
        BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
        + "var window;"
        + "window.alert = function() {x};";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.alert = 0;\n"
        + "Foo.prototype.window = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.alert = 0;\n"
        + "Bar.prototype.window = 0;\n"
        + "window.alert();";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.alert=0;"
        + "Foo.prototype.Foo_prototype$window=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.alert=0;"
        + "Bar.prototype.Bar_prototype$window=0;"
        + "window.alert();";

    testSets(false, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]]"
             + ", window=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
             + " window=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeInvalidation
  public void testUnionTypeInvalidation() {
    String externs = ""
        + " function Baz() {}"
        + "Baz.prototype.a";
    String js = ""
        + " function Ind() {this.a=0}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 1\n;"
        + "F = new Bar;\n"
        + "\n"
        + "var Z = new Baz;\n"
        + "Z.a = 1\n;"
        + "\n"
        + "var B = new Baz;\n"
        + "B.a = 1;\n"
        + "B = new Bar;\n";
    
    
    String output = ""
        + "function Ind() { this.Ind$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    String ttOutput = ""
        + "function Ind() { this.Unique$1$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    testSets(false, externs, js, output, "{a=[[Ind]]}");
    testSets(true, externs, js, ttOutput, "{a=[[Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionAndExternTypes
  public void testUnionAndExternTypes() {
    String externs = ""
      + " function Foo() { }"
      + "Foo.prototype.a = 4;\n";
    String js = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.a = 3; }\n"
      + " function T2() { this.a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";
    String output = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.T1$a = 3; }\n"
      + " function T2() { this.T2$a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";

    
    
    testSets(false, externs, js, output, "{a=[[T1], [T2]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTypedExterns
  public void testTypedExterns() {
    String externs = ""
        + " function Window() {};\n"
        + "Window.prototype.alert;"
        + ""
        + "var window;";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.alert = 0;\n"
        + "window.alert('blarg');";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$alert=0;"
        + "window.alert('blarg');";
    testSets(false, externs, js, output, "{alert=[[Foo.prototype]]}");
    testSets(true, externs, js, output, "{alert=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSubtypesWithSameField
  public void testSubtypesWithSameField() {
    String js = ""
        + " function Top() {}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(top) {\n"
        + "  var x = top.a;\n"
        + "}\n"
        + "foo(new Foo);\n"
        + "foo(new Bar);\n";
    testSets(false, js, "{}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeReferenceOfSubtypeProperty
  public void testSupertypeReferenceOfSubtypeProperty() {
    String externs = ""
        + " function Ext() {}"
        + "Ext.prototype.a;";
    String js = ""
        + " function Foo() {}\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(foo) {\n"
        + "  var x = foo.a;\n"
        + "}\n";
    String result = ""
        + "function Foo() {}\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "function foo(foo) {\n"
        + "  var x = foo.Bar_prototype$a;\n"
        + "}\n";
    testSets(false, externs, js, result, "{a=[[Bar.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralNotRenamed
  public void testObjectLiteralNotRenamed() {
    String js = ""
        + "var F = {a:'a', b:'b'};"
        + "F.a = 'z';";
    testSets(false, js, js, "{}");
    testSets(true, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testClosureInherits
  public void testClosureInherits() {
    String js = ""
        + "var goog = {};"
        + "\n"
        + "goog.inherits = function(childCtor, parentCtor) {\n"
        + "  \n"
        + "  function tempCtor() {};\n"
        + "  tempCtor.prototype = parentCtor.prototype;\n"
        + "  childCtor.superClass_ = parentCtor.prototype;\n"
        + "  childCtor.prototype = new tempCtor();\n"
        + "  childCtor.prototype.constructor = childCtor;\n"
        + "};"
        + " function Top() {}\n"
        + "Top.prototype.f = function() {};"
        + " function Foo() {}\n"
        + "goog.inherits(Foo, Top);\n"
        + "Foo.prototype.f = function() {"
        + "  Foo.superClass_.f();"
        + "};\n"
        + " function Bar() {}\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.f = function() {"
        + "  Bar.superClass_.f();"
        + "};\n"
        + "(new Bar).f();\n";
    testSets(false, js, "{f=[[Top.prototype]]}");
    testSets(true, js, "{constructor=[[Bar.prototype, Foo.prototype]], "
                 + "f=[[Bar.prototype], [Foo.prototype], [Top.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionMethod
  public void testSkipNativeFunctionMethod() {
    String externs = ""
        + ""
        + "function Function(var_args) {}"
        + "Function.prototype.call = function() {};";
    String js = ""
        + " function Foo(){};"
        + ""
        + "function Bar() { Foo.call(this); };"; 
    testSame(externs, js, null);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeObjectMethod
  public void testSkipNativeObjectMethod() {
    String externs = ""
        + " function Object(opt_v) {}"
        + "Object.prototype.hasOwnProperty;";
    String js = ""
        + " function Foo(){};"
        + "(new Foo).hasOwnProperty('x');";
    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExtendNativeType
  public void testExtendNativeType() {
    String externs = ""
        + ""
        + "function Date(opt_1, opt_2, opt_3, opt_4, opt_5, opt_6, opt_7) {}"
        + " Date.prototype.toString = function() {}";
    String js = ""
        + " function SuperDate() {};\n"
        + "(new SuperDate).toString();";
    testSets(true, externs, js, js, "{}");
    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStringFunction
  public void testStringFunction() {
    
    
    String externs = ""
         + "function String(opt_str) {};\n"
         + "\n"
         + "String.prototype.toString = function() { };\n";
    String js = ""
         + " function Foo() {};\n"
         + "Foo.prototype.foo = function() {};\n"
         + "String.prototype.foo = function() {};\n"
         + "var a = 'str'.toString().foo();\n";
    String output = ""
         + "function Foo() {};\n"
         + "Foo.prototype.Foo_prototype$foo = function() {};\n"
         + "String.prototype.String_prototype$foo = function() {};\n"
         + "var a = 'str'.toString().String_prototype$foo();\n";

    testSets(false, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
    testSets(true, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnusedTypeInExterns
  public void testUnusedTypeInExterns() {
    String externs = ""
        + " function Foo() {};\n"
        + "Foo.prototype.a";
    String js = ""
        + " function Bar() {};\n"
        + "Bar.prototype.a;"
        + " function Baz() {};\n"
        + "Baz.prototype.a;";
    String output = ""
        + " function Bar() {};\n"
        + "Bar.prototype.Bar_prototype$a;"
        + " function Baz() {};\n"
        + "Baz.prototype.Baz_prototype$a";
    testSets(false, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
    testSets(true, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterface
  public void testInterface() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + "\n"
        + "var F = new Foo;"
        + "var x = F.a;";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceOfSuperclass
  public void testInterfaceOfSuperclass() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {};\n"
        + "Bar.prototype.a;\n"
        + "\n"
        + "var B = new Bar;"
        + "B.a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js,
        "{a=[[Bar.prototype], [Foo.prototype], [I.prototype]]}");
  }
