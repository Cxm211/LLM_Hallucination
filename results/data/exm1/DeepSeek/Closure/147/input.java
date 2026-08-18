// buggy code
  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // or @override annotation.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null &&
          (jsDoc.isConstructor() ||
           jsDoc.isInterface() ||
           jsDoc.hasThisType() ||
           jsDoc.isOverride())) {
        return false;
      }

      // Don't traverse functions unless they would normally
      // be able to have a @this annotation associated with them. e.g.,
      // var a = function() { }; // or
      // function a() {} // or
      // a.x = function() {}; // or
      // var a = {x: function() {}};
      int pType = parent.getType();
      if (!(pType == Token.BLOCK ||
            pType == Token.SCRIPT ||
            pType == Token.NAME ||
            pType == Token.ASSIGN)) {
        return false;
      }
    }

    if (parent != null && parent.getType() == Token.ASSIGN) {
      Node lhs = parent.getFirstChild();
      Node rhs = lhs.getNext();

      if (n == lhs) {
        // Always traverse the left side of the assignment. To handle
        // nested assignments properly (e.g., (a = this).property = c;),
        // assignLhsChild should not be overridden.
        if (assignLhsChild == null) {
          assignLhsChild = lhs;
        }
      } else {
        // Only traverse the right side if it's not an assignment to a prototype
        // property or subproperty.
        if (NodeUtil.isGet(lhs)) {
          if (lhs.getType() == Token.GETPROP &&
              lhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
          Node llhs = lhs.getFirstChild();
          if (llhs.getType() == Token.GETPROP &&
              llhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
        }
      }
    }

    return true;
  }

    private void visitFunction(NodeTraversal t, Node n) {
      FunctionType funType = (FunctionType) n.getJSType();
      Node block = n.getLastChild();
      Node paramName = NodeUtil.getFnParameters(n).getFirstChild();
      Node insertionPoint = null;

      // To satisfy normalization constraints, the type checking must be
      // added after any inner function declarations.

      for (Node paramType : funType.getParameters()) {
        // Can this ever happen?
        if (paramName == null) {
          return;
        }

        Node checkNode = createCheckTypeCallNode(
            paramType.getJSType(), paramName.cloneTree());

        if (checkNode == null) {
          // We don't know how to check this parameter type.
          paramName = paramName.getNext();
          continue;
        }

        checkNode = new Node(Token.EXPR_RESULT, checkNode);
        if (insertionPoint == null) {
          block.addChildToFront(checkNode);
        } else {
          block.addChildAfter(checkNode, insertionPoint);
        }

        compiler.reportCodeChange();
        paramName = paramName.getNext();
        insertionPoint = checkNode;
      }
    }

// relevant test
// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis1
  public void testGlobalThis1() throws Exception {
    testSame("var a = this;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis2
  public void testGlobalThis2() {
    testFailure("this.foo = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis3
  public void testGlobalThis3() {
    testFailure("this[foo] = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis4
  public void testGlobalThis4() {
    testFailure("this['foo'] = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis5
  public void testGlobalThis5() {
    testFailure("(a = this).foo = 4;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis6
  public void testGlobalThis6() {
    testSame("a = this;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis7
  public void testGlobalThis7() {
    testFailure("var a = this.foo;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction1
  public void testStaticFunction1() {
    testSame("function a() { return this; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction2
  public void testStaticFunction2() {
    testFailure("function a() { this.complex = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction3
  public void testStaticFunction3() {
    testSame("var a = function() { return this; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction4
  public void testStaticFunction4() {
    testFailure("var a = function() { this.foo.bar = 6; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction5
  public void testStaticFunction5() {
    testSame("function a() { return function() { return this; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction6
  public void testStaticFunction6() {
    testSame("function a() { return function() { this = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction7
  public void testStaticFunction7() {
    testSame("var a = function() { return function() { this = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction8
  public void testStaticFunction8() {
    testFailure("var a = function() { return this.foo; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor1
  public void testConstructor1() {
    testSame("function A() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor2
  public void testConstructor2() {
    testSame("var A = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor3
  public void testConstructor3() {
    testSame("a.A = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInterface1
  public void testInterface1() {
    testSame(
        "function A() {  this.m2; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testOverride1
  public void testOverride1() {
    testSame("function A() { } var a = new A();" +
             " a.foo = function() { this.bar = 5; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc1
  public void testThisJSDoc1() throws Exception {
    testSame("function h() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc2
  public void testThisJSDoc2() throws Exception {
    testSame("var h = function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc3
  public void testThisJSDoc3() throws Exception {
    testSame("foo.bar = function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc4
  public void testThisJSDoc4() throws Exception {
    testSame("function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc5
  public void testThisJSDoc5() throws Exception {
    testSame("function a() { function() { this.foo = 56; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod1
  public void testMethod1() {
    testSame("A.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod2
  public void testMethod2() {
    testSame("a.B.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod3
  public void testMethod3() {
    testSame("a.b.c.D.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod4
  public void testMethod4() {
    testSame("a.prototype['x' + 'y'] =  function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testPropertyOfMethod
  public void testPropertyOfMethod() {
    testFailure("a.protoype.b = {}; " +
        "a.prototype.b.c = function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod1
  public void testStaticMethod1() {
    testFailure("a.b = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod2
  public void testStaticMethod2() {
    testSame("a.b = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod3
  public void testStaticMethod3() {
    testSame("a.b.c = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethodInStaticFunction
  public void testMethodInStaticFunction() {
    testSame("function f() { A.prototype.m1 = function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunctionInMethod1
  public void testStaticFunctionInMethod1() {
    testSame("A.prototype.m1 = function() { function me() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunctionInMethod2
  public void testStaticFunctionInMethod2() {
    testSame("A.prototype.m1 = function() {" +
        "  function me() {" +
        "    function myself() {" +
        "      function andI() { this.m2 = 5; } } } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction1
  public void testInnerFunction1() {
    testFailure("function f() { function g() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction2
  public void testInnerFunction2() {
    testFailure("function f() { var g = function() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction3
  public void testInnerFunction3() {
    testFailure(
        "function f() { var x = {}; x.y = function() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction4
  public void testInnerFunction4() {
    testSame(
        "function f() { var x = {}; x.y(function() { return this.x; }); }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182a
  public void testIssue182a() {
    testFailure("var NS = {read: function() { return this.foo; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182b
  public void testIssue182b() {
    testFailure("var NS = {write: function() { this.foo = 3; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182c
  public void testIssue182c() {
    testFailure("var NS = {}; NS.write2 = function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182d
  public void testIssue182d() {
    testSame("function Foo() {} " +
        "Foo.prototype = {write: function() { this.foo = 3; }};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

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
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testConstValue
  public void testConstValue() {
    
    
    testChecks(" function f(CONST) {}",
        "function f(CONST) {" +
        "  jscomp.typecheck.checkType(CONST, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValueWithInnerFn
  public void testValueWithInnerFn() {
    testChecks(" function f(i) { function g() {} }",
        "function f(i) {" +
        "  function g() {}" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.valueChecker('number'), " +
        "      jscomp.typecheck.valueChecker('string')" +
        "]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUntypedParam
  public void testUntypedParam() {
    testChecks(" function f(x) {}", "function f(x) {}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturn
  public void testReturn() {
    testChecks(" function f() { return 'x'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('x', " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "      [jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "    [jscomp.typecheck.classChecker('goog.Foo')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerClasses
  public void testInnerClasses() {
    testChecks(
        "function f() {  function inner() {} }" +
        "function g() {  function inner() {} }",
        "function f() {" +
        "   function inner() {}" +
        "  inner.prototype['instance_of__inner'] = true;" +
        "}" +
        "function g() {" +
        "   function inner$$1() {}" +
        "  inner$$1.prototype['instance_of__inner$$1'] = true;" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInterface
  public void testInterface() {
    testChecks("function I() {}" +
        "function f(i) {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "    [jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testExtendedInterface
  public void testExtendedInterface() {
    testChecks("function I() {}" +
        "function J() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function J() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype['implements__J'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrdering
  public void testImplementedInterfaceOrdering() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}" +
        "C.prototype.f = function() {};",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrderingGoogInherits
  public void testImplementedInterfaceOrderingGoogInherits() {
    testChecks("var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {}" +
        "function B() {}" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype.f = function() {};",
        "var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function B() {}" +
        "B.prototype['instance_of__B'] = true;" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerConstructor
  public void testInnerConstructor() {
    testChecks("(function() {  function C() {} })()",
        "(function() {" +
        "  function C() {} C.prototype['instance_of__C'] = true;" +
        "})()");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturnNothing
  public void testReturnNothing() {
    testChecks("function f() { return; }", "function f() { return; }");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testFunctionType
  public void testFunctionType() {
    testChecks("function f() {}", "function f() {}");
  }
