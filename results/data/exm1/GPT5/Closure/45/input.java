// buggy code
  private void interpretAssigns() {
    boolean changes = false;
    do {
      changes = false;

      // We can't use traditional iterators and iterables for this list,
      // because our lazily-evaluated continuations will modify it while
      // we traverse it.
      for (int current = 0; current < maybeUnreferenced.size(); current++) {
        Var var = maybeUnreferenced.get(current);
        if (referenced.contains(var)) {
          maybeUnreferenced.remove(current);
          current--;
        } else {
          boolean assignedToUnknownValue = false;
          boolean hasPropertyAssign = false;

          if (var.getParentNode().isVar() &&
              !NodeUtil.isForIn(var.getParentNode().getParent())) {
            Node value = var.getInitialValue();
            assignedToUnknownValue = value != null &&
                !NodeUtil.isLiteralValue(value, true);
          } else {
            // This was initialized to a function arg or a catch param
            // or a for...in variable.
            assignedToUnknownValue = true;
          }

          for (Assign assign : assignsByVar.get(var)) {
            if (assign.isPropertyAssign) {
              hasPropertyAssign = true;
            } else if (!NodeUtil.isLiteralValue(
                assign.assignNode.getLastChild(), true)) {
              assignedToUnknownValue = true;
            }
          }

          if (assignedToUnknownValue && hasPropertyAssign) {
            changes = markReferencedVar(var) || changes;
            maybeUnreferenced.remove(current);
            current--;
          }
        }
      }
    } while (changes);
  }

    Assign(Node assignNode, Node nameNode, boolean isPropertyAssign) {
      Preconditions.checkState(NodeUtil.isAssignmentOp(assignNode));
      this.assignNode = assignNode;
      this.nameNode = nameNode;
      this.isPropertyAssign = isPropertyAssign;

      this.maybeAliased = !assignNode.getParent().isExprResult();
      this.mayHaveSecondarySideEffects =
          maybeAliased ||
          NodeUtil.mayHaveSideEffects(assignNode.getFirstChild()) ||
          NodeUtil.mayHaveSideEffects(assignNode.getLastChild());
    }

// relevant test
// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering1
  public void testWarningGuardOrdering1() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering2
  public void testWarningGuardOrdering2() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering3
  public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering4
  public void testWarningGuardOrdering4() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOffByDefault
  public void testCheckGlobalThisOffByDefault() {
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithAdvancedMode
  public void testCheckGlobalThisOnWithAdvancedMode() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithErrorFlag
  public void testCheckGlobalThisOnWithErrorFlag() {
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOff
  public void testCheckGlobalThisOff() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testReflectedMethods
  public void testReflectedMethods() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;",
        "function a() {}" +
        "a.prototype.a = function(e, d) { alert(d); };" +
        "var b = goog.c.b(a, {a: 1}),c;" +
        "for (c in b) { b[c].call(b); }" +
        "window.Foo = a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOffByDefault
  public void testTypeParsingOffByDefault() {
    testSame(" function f(a) { return a; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOnWithVerbose
  public void testTypeParsingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet
  public void testCheckSymbolsOverrideForQuiet() {
    args.add("--warning_level=QUIET");
    args.add("--jscomp_error=undefinedVars");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function f(a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
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
         "var FOO = !0, BAR = 5, CCC = !0, DDD = !0;");
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
         "var goog = {dom:{}};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGetMsgWiring
  public void testGetMsgWiring() throws Exception {
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');",
         "var goog={getMsg:function(a){return a}}, " +
         "MSG_FOO=goog.getMsg('foo');");
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');" +
         "window['foo'] = MSG_FOO;",
         "window.foo = 'foo';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCssNameWiring
  public void testCssNameWiring() throws Exception {
    test("var goog = {}; goog.getCssName = function() {};" +
         "goog.setCssNameMapping = function() {};" +
         "goog.setCssNameMapping({'goog': 'a', 'button': 'b'});" +
         "var a = goog.getCssName('goog-button');" +
         "var b = goog.getCssName('css-button');" +
         "var c = goog.getCssName('goog-menu');" +
         "var d = goog.getCssName('css-menu');",
         "var goog = { getCssName: function() {}," +
         "             setCssNameMapping: function() {} }," +
         "    a = 'a-b'," +
         "    b = 'css-b'," +
         "    c = 'a-menu'," +
         "    d = 'css-menu';");
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
    args.add("--jscomp_off=es5Strict");
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue297
  public void testIssue297() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}",
         "function f(b) {" +
         " var a;" +
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && 0<a);" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue504
  public void testIssue504() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("void function() { alert('hi'); }();",
         "alert('hi');", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601
  public void testIssue601() {
    args.add("--compilation_level=WHITESPACE_ONLY");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "function f(){return'\\v'=='v'}window['f']=f");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601b
  public void testIssue601b() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\v'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601c
  public void testIssue601c() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\u000B' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\u000B'=='v'}");
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
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
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.DEFAULT,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat2
  public void testSourceMapFormat2() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--source_map_format=V3");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.V3,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleWrapperBaseNameExpansion
  public void testModuleWrapperBaseNameExpansion() throws Exception {
    useModules = ModulePattern.CHAIN;
    args.add("--module_wrapper=m0:%s 
    testSame(new String[] {
      "var x = 3;",
      "var y = 4;"
    });

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.writeModuleOutput(
        builder,
        lastCompiler.getModuleGraph().getRootModule());
    assertEquals("var x=3; 
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
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
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag2
  public void testVersionFlag2() {
    lastArg = "--version";
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSyntheticExterns
  public void testSyntheticExterns() {
    externs = ImmutableList.of(
        JSSourceFile.fromCode("externs", "myVar.property;"));
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         "var theirVar={},myVar={},yourVar={};");

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var myVar = {};",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGoogAssertStripping
  public void testGoogAssertStripping() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("goog.asserts.assert(false)",
         "");
    args.add("--debug");
    test("goog.asserts.assert(false)", "goog.$asserts$.$assert$(!1)");
  }

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

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordDefault
  public void testWithKeywordDefault() {
    test("var x = {}; with (x) {}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordWithEs5ChecksOff
  public void testWithKeywordWithEs5ChecksOff() {
    args.add("--jscomp_off=es5Strict");
    testSame("var x = {}; with (x) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoSrCFilesWithManifest
  public void testNoSrCFilesWithManifest() throws IOException {
    args.add("--use_only_custom_externs=true");
    args.add("--output_manifest=test.MF");
    CommandLineRunner runner = createCommandLineRunner(new String[0]);
    String expectedMessage = "";
    try {
      runner.doRun();
    } catch (FlagUsageException e) {
      expectedMessage = e.getMessage();
    }
    assertEquals(expectedMessage, "Bad --js flag. " +
      "Manifest files cannot be generated when the input is from stdin.");
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

// com.google.javascript.jscomp.OptimizeCallsTest::testRemovingReturnCallToFunctionWithUnusedParams
  public void testRemovingReturnCallToFunctionWithUnusedParams() {
    test("function foo() {var x; return x = bar(1)} foo(); function bar(x) {}",
         "function foo() {          bar(); return;} foo(); function bar()  {}");
  }

// com.google.javascript.jscomp.OptimizeCallsTest::testNestingFunctionCallWithUnsedParams
  public void testNestingFunctionCallWithUnsedParams() {
    test("function f1(x) { } function f2(x) { }" +
         "function f3(x) { } function f4(x) { }" +
         "f3(f1(f2()));",
         "function f1() {f2()} function f2() { }" +
         "function f3() {f1()} " +
         "f3();"
    );
  }

// com.google.javascript.jscomp.OptimizeCallsTest::testUnusedAssignOnFunctionWithUnusedParams
  public void testUnusedAssignOnFunctionWithUnusedParams() {
    test("var foo = function(a){}; function bar(){var x;x = foo} bar(); foo(1)",
         "var foo = function( ){}; function bar(){             } bar(); foo()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveUnusedVars
  public void testRemoveUnusedVars() {
    
    test("var a;var b=3;var c=function(){};var x=A();var y; var z;" +
         "function A(){B()} function B(){C(b)} function C(){} " +
         "function X(){Y()} function Y(z){Z(x)} function Z(){y} " +
         "P=function(){A()}; " +
         "try{0}catch(e){a}",

         "var a;var b=3;A();function A(){B()}" +
         "function B(){C(b)}" +
         "function C(){}" +
         "P=function(){A()}" +
         ";try{0}catch(e){a}");

    
    test("var i=0;var j=0;if(i>0){var k=1;}",
         "var i=0;if(i>0);");

    
    test("for (var i in booyah) {" +
         "  if (i > 0) x += ', ';" +
         "  var arg = 'foo';" +
         "  if (arg.length > 40) {" +
         "    var unused = 'bar';" +   
         "    arg = arg.substr(0, 40) + '...';" +
         "  }" +
         "  x += arg;" +
         "}",

         "for(var i in booyah){if(i>0)x+=\", \";" +
         "var arg=\"foo\";if(arg.length>40)arg=arg.substr(0,40)+\"...\";" +
         "x+=arg}");

    
    test("function A(){}" +
         "if(0){function B(){}}win.setTimeout(function(){A()})",
         "function A(){}" +
         "if(0);win.setTimeout(function(){A()})");

    
    test("function A(){A()}function B(){B()}B()",
         "function B(){B()}B()");

    
    test("var x,y=2,z=3;A(x);B(z);var a,b,c=4;C()",
         "var x,z=3;A(x);B(z);C()");

    
    test("for(var i=0,j=0;i<10;){}" +
         "for(var x=0,y=0;;y++){}" +
         "for(var a,b;;){a}" +
         "for(var c,d;;);" +
         "for(var item in items){}",

         "for(var i=0;i<10;);" +
         "for(var y=0;;y++);" +
         "for(var a;;)a;" +
         "for(;;);" +
         "for(var item in items);");

    
    test("var a,b,c,d;var e=[b,c];var x=e[3];var f=[d];print(f[0])",
         "var d;var f=[d];print(f[0])");

    
    test("var x;function A(){var x;B()}function B(){print(x)}A()",
         "var x;function A(){B()}function B(){print(x)}A()");

    
    test("function A(){var x;return function(){print(x)}}A()",
         "function A(){var x;return function(){print(x)}}A()");

    
    test("function A(){}function B(){" +
         "var c,d,e,f,g,h;" +
         "function C(){print(c)}" +
         "var handler=function(){print(d)};" +
         "var handler2=function(){handler()};" +
         "e=function(){print(e)};" +
         "if(1){function G(){print(g)}}" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()",

         "function B(){" +
         "var f,h;" +
         "if(1);" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()");

    
    test("var a,b=1; function _A1() {this.foo(a)}",
         "var a;function _A1(){this.foo(a)}");

    
    test("undefinedVar = 1", "undefinedVar=1");

    
    test("var a,b=foo(),c=i++,d;var e=boo();var f;print(d);",
         "foo(); i++; var d; boo(); print(d)");

    test("var a,b=foo()", "foo()");
    test("var b=foo(),a", "foo()");
    test("var a,b=foo(a)", "var a; foo(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemoval
  public void testFunctionArgRemoval() {
    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b(1,2)");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");
    testSame("var b=function(e,f,c,d){return c+d};b(1,2)");

    
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(e,c,f,d){return c+d};b(1,2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemovalFromCallSites
  public void testFunctionArgRemovalFromCallSites() {
    this.modifyCallSites = true;

    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b()");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,f,c,d){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b()");

    
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionsDeadButEscaped
  public void testFunctionsDeadButEscaped() {
    testSame("function b(a) { a = 1; print(arguments[0]) }; b(6)");
    testSame("function b(a) { a = 1; arguments=1; }; b(6)");
    testSame("function b(a) { var c = 2; a = c; print(arguments[0]) }; b(6)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testVarInControlStructure
  public void testVarInControlStructure() {
    test("if (true) var b = 3;", "if(true);");
    test("if (true) var b = 3; else var c = 5;", "if(true);else;");
    test("while (true) var b = 3;", "while(true);");
    test("for (;;) var b = 3;", "for(;;);");
    test("do var b = 3; while(true)", "do;while(true)");
    test("with (true) var b = 3;", "with(true);");
    test("f: var b = 3;","f:{}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRValueHoisting
  public void testRValueHoisting() {
    test("var x = foo();", "foo()");
    test("var x = {a: foo()};", "({a:foo()})");

    test("var x=function y(){}", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testModule
  public void testModule() {
    test(createModules(
             "var unreferenced=1; function x() { foo(); }" +
             "function uncalled() { var x; return 2; }",
             "var a,b; function foo() { this.foo(a); } x()"),
         new String[] {
           "function x(){foo()}",
           "var a;function foo(){this.foo(a)}x()"
         });
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("(function x(){return x()})()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    test("var x = 3; (function x() { return x(); })();",
         "(function x$$1(){return x$$1()})()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName1
  public void testFunctionWithName1() {
    test("var x=function f(){};x()",
         "var x=function(){};x()");

    preserveFunctionExpressionNames = true;
    testSame("var x=function f(){};x()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName2
  public void testFunctionWithName2() {
    test("foo(function bar(){})",
         "foo(function(){})");

    preserveFunctionExpressionNames = true;
    testSame("foo(function bar(){})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal1
  public void testRemoveGlobal1() {
    removeGlobal = false;
    testSame("var x=1");
    test("var y=function(x){var z;}", "var y=function(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal2
  public void testRemoveGlobal2() {
    removeGlobal = false;
    testSame("var x=1");
    test("function y(x){var z;}", "function y(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal3
  public void testRemoveGlobal3() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}y()}",
         "function x(){function y(){}y()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal4
  public void testRemoveGlobal4() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}}",
         "function x(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168a
  public void testIssue168a() {
    test("function _a(){" +
         "  (function(x){ _b(); })(1);" +
         "}" +
         "function _b(){" +
         "  _a();" +
         "}",
         "function _a(){(function(){_b()})(1)}" +
         "function _b(){_a()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168b
  public void testIssue168b() {
    removeGlobal = false;
    test("function a(){" +
         "  (function(x){ b(); })(1);" +
         "}" +
         "function b(){" +
         "  a();" +
         "}",
         "function a(){(function(){b()})(1)}" +
         "function b(){a()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign1
  public void testUnusedAssign1() {
    test("var x = 3; x = 5;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign2
  public void testUnusedAssign2() {
    test("function f(a) { a = 3; } this.x = f;",
         "function f(){} this.x=f");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign3
  public void testUnusedAssign3() {
    
    
    test("try { throw ''; } catch (e) { e = 3; }",
        "try{throw\"\";}catch(e){e=3}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign4
  public void testUnusedAssign4() {
    test("function f(a, b) { this.foo(b); a = 3; } this.x = f;",
        "function f(a,b){this.foo(b);}this.x=f");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign5
  public void testUnusedAssign5() {
    test("var z = function f() { f = 3; }; z();",
         "var z=function(){};z()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign5b
  public void testUnusedAssign5b() {
    test("var z = function f() { f = alert(); }; z();",
         "var z=function(){alert()};z()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign6
  public void testUnusedAssign6() {
    test("var z; z = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign6b
  public void testUnusedAssign6b() {
    test("var z; z = alert();", "alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign7
  public void testUnusedAssign7() {
    
    test("var a = 3; for (var i in {}) { i = a; }",
         
         "var a = 3; var i; for (i in {}) {i = a;}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign8
  public void testUnusedAssign8() {
    
    test("var a = 3; for (var i in {}) { i = a; } alert(a);",
         
         "var a = 3; var i; for (i in {}) {i = a} alert(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign1
  public void testUnusedPropAssign1() {
    test("var x = {}; x.foo = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign1b
  public void testUnusedPropAssign1b() {
    test("var x = {}; x.foo = alert();", "alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign2
  public void testUnusedPropAssign2() {
    test("var x = {}; x['foo'] = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign2b
  public void testUnusedPropAssign2b() {
    test("var x = {}; x[alert()] = alert();", "alert(),alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign3
  public void testUnusedPropAssign3() {
    test("var x = {}; x['foo'] = {}; x['bar'] = 3", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign3b
  public void testUnusedPropAssign3b() {
    test("var x = {}; x[alert()] = alert(); x[alert() + alert()] = alert()",
         "alert(),alert();(alert() + alert()),alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign4
  public void testUnusedPropAssign4() {
    test("var x = {foo: 3}; x['foo'] = 5;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign5
  public void testUnusedPropAssign5() {
    test("var x = {foo: bar()}; x['foo'] = 5;",
         "var x={foo:bar()};x[\"foo\"]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign6
  public void testUnusedPropAssign6() {
    test("var x = function() {}; x.prototype.bar = function() {};", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7
  public void testUnusedPropAssign7() {
    test("var x = {}; x[x.foo] = x.bar;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7b
  public void testUnusedPropAssign7b() {
    testSame("var x = {}; x[x.foo] = alert(x.bar);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7c
  public void testUnusedPropAssign7c() {
    test("var x = {}; x[alert(x.foo)] = x.bar;",
         "var x={};x[alert(x.foo)]=x.bar");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign1
  public void testUsedPropAssign1() {
    test("function f(x) { x.bar = 3; } f({});",
         "function f(x){x.bar=3}f({})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign2
  public void testUsedPropAssign2() {
    test("try { throw z; } catch (e) { e.bar = 3; }",
         "try{throw z;}catch(e){e.bar=3}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign3
  public void testUsedPropAssign3() {
    
    test("var x = {}; x.foo = 3; x = bar();",
         "var x={};x.foo=3;x=bar()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign4
  public void testUsedPropAssign4() {
    test("var y = foo(); var x = {}; x.foo = 3; y[x.foo] = 5;",
         "var y=foo();var x={};x.foo=3;y[x.foo]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign5
  public void testUsedPropAssign5() {
    test("var y = foo(); var x = 3; y[x] = 5;",
         "var y=foo();var x=3;y[x]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign6
  public void testUsedPropAssign6() {
    test("var x = newNodeInDom(doc); x.innerHTML = 'new text';",
         "var x=newNodeInDom(doc);x.innerHTML=\"new text\"");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign7
  public void testUsedPropAssign7() {
    testSame("var x = {}; for (x in alert()) { x.foo = 3; }");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign8
  public void testUsedPropAssign8() {
    testSame("for (var x in alert()) { x.foo = 3; }");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign9
  public void testUsedPropAssign9() {
    testSame(
        "var x = {}; x.foo = newNodeInDom(doc); x.foo.innerHTML = 'new test';");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1
  public void testDependencies1() {
    test("var a = 3; var b = function() { alert(a); };", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1b
  public void testDependencies1b() {
    test("var a = 3; var b = alert(function() { alert(a); });",
         "var a=3;alert(function(){alert(a)})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1c
  public void testDependencies1c() {
    test("var a = 3; var _b = function() { alert(a); };",
         "var a=3;var _b=function(){alert(a)}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2
  public void testDependencies2() {
    test("var a = 3; var b = 3; b = function() { alert(a); };", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2b
  public void testDependencies2b() {
    test("var a = 3; var b = 3; b = alert(function() { alert(a); });",
         "var a=3;alert(function(){alert(a)})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2c
  public void testDependencies2c() {
    testSame("var a=3;var _b=3;_b=function(){alert(a)}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testGlobalVarReferencesLocalVar
  public void testGlobalVarReferencesLocalVar() {
    testSame("var a=3;function f(){var b=4;a=b}alert(a + f())");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testLocalVarReferencesGlobalVar1
  public void testLocalVarReferencesGlobalVar1() {
    testSame("var a=3;function f(b, c){b=a; alert(b + c);} f();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testLocalVarReferencesGlobalVar2
  public void testLocalVarReferencesGlobalVar2() {
    test("var a=3;function f(b, c){b=a; alert(c);} f();",
         "function f(b, c) { alert(c); } f();");
    this.modifyCallSites = true;
    test("var a=3;function f(b, c){b=a; alert(c);} f();",
         "function f(c) { alert(c); } f();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var b = null; var a = (b = 3); alert(a);",
         "var a = 3; alert(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign2
  public void testNestedAssign2() {
    test("var a = 1; var b = 2; var c = (b = a); alert(c);",
         "var a = 1; var c = a; alert(c);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign3
  public void testNestedAssign3() {
    test("var b = 0; var z; z = z = b = 1; alert(b);",
         "var b = 0; b = 1; alert(b);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testCallSiteInteraction
  public void testCallSiteInteraction() {
    this.modifyCallSites = true;

    testSame("var b=function(){return};b()");
    testSame("var b=function(c){return c};b(1)");
    test("var b=function(c){};b.call(null, x)",
         "var b=function(){};b.call(null)");
    test("var b=function(c){};b.apply(null, x)",
         "var b=function(){};b.apply(null, x)");

    test("var b=function(c){return};b(1)",
         "var b=function(){return};b()");
    test("var b=function(c){return};b(1,2)",
         "var b=function(){return};b()");
    test("var b=function(c){return};b(1,2);b(3,4)",
         "var b=function(){return};b();b()");

    
    
    test("var b=function(c,d){return d};b(1,2);b(3,4);b.length",
         "var b=function(c,d){return d};b(0,2);b(0,4);b.length");

    test("var b=function(c){return};b(1,2);b(3,new x())",
         "var b=function(){return};b();b(new x())");

    test("var b=function(c){return};b(1,2);b(new x(),4)",
         "var b=function(){return};b();b(new x())");

    test("var b=function(c,d){return d};b(1,2);b(new x(),4)",
         "var b=function(c,d){return d};b(0,2);b(new x(),4)");
    test("var b=function(c,d,e){return d};b(1,2,3);b(new x(),4,new x())",
         "var b=function(c,d){return d};b(0,2);b(new x(),4,new x())");

    
    test("var b=function(c,d){b(1,2);return d};b(3,4);b(5,6)",
         "var b=function(d){b(2);return d};b(4);b(6)");

    testSame("var b=function(c){return arguments};b(1,2);b(3,4)");

    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b()");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");

    
    test("var b=function(e,f,c,d){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b()");
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(2)");

    
    
    test("var b=function(c,d){};var b=function(e,f){};b(1,2)",
         "var b=function(){};var b=function(){};b(1,2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testCallSiteInteraction_contructors
  public void testCallSiteInteraction_contructors() {
    this.modifyCallSites = true;
    
    
    test("var Ctor1=function(a,b){return a};" +
        "var Ctor2=function(a,b){Ctor1.call(this,a,b)};" +
        "goog$inherits(Ctor2, Ctor1);" +
        "new Ctor2(1,2)",
        "var Ctor1=function(a){return a};" +
        "var Ctor2=function(a){Ctor1.call(this,a)};" +
        "goog$inherits(Ctor2, Ctor1);" +
        "new Ctor2(1)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemovalCausingInconsistency
  public void testFunctionArgRemovalCausingInconsistency() {
    this.modifyCallSites = true;
    
    
    
    test("var a=function(x,y){};" +
        "var b=function(z){};" +
        "a(new b, b)",
        "var a=function(){};" +
        "var b=function(){};" +
        "a(new b)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveUnusedVarsPossibleNpeCase
  public void testRemoveUnusedVarsPossibleNpeCase() {
    this.modifyCallSites = true;
    test("var a = [];" +
        "var register = function(callback) {a[0] = callback};" +
        "register(function(transformer) {});" +
        "register(function(transformer) {});",
        "var register=function(){};register();register()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeJSCompiler_renameProperty
  public void testDoNotOptimizeJSCompiler_renameProperty() {
    this.modifyCallSites = true;

    
    test("function JSCompiler_renameProperty(a) {};" +
         "JSCompiler_renameProperty('a');",
         "function JSCompiler_renameProperty() {};" +
         "JSCompiler_renameProperty('a');");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeJSCompiler_ObjectPropertyString
  public void testDoNotOptimizeJSCompiler_ObjectPropertyString() {
    this.modifyCallSites = true;
    test("function JSCompiler_ObjectPropertyString(a, b) {};" +
         "JSCompiler_ObjectPropertyString(window,'b');",
         "function JSCompiler_ObjectPropertyString() {};" +
         "JSCompiler_ObjectPropertyString(window,'b');");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeSetters
  public void testDoNotOptimizeSetters() {
    testSame("({set s(a) {}})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass1
  public void testRemoveInheritedClass1() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new a",
        "function a(){} new a");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass2
  public void testRemoveInheritedClass2() {
    test("function goog$inherits(){}" +
        "function goog$mixin(){}" +
        "function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "goog$inherits(b,a);" +
        "goog$mixin(c.prototype,b.prototype);",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass3
  public void testRemoveInheritedClass3() {
    testSame("function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass4
  public void testRemoveInheritedClass4() {
    testSame("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a);" +
        "function c(){}" +
        "goog$inherits(c,b); new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass5
  public void testRemoveInheritedClass5() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a);" +
        "function c(){}" +
        "goog$inherits(c,b); new b",
        "function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass6
  public void testRemoveInheritedClass6() {
    test("function goog$mixin(){}" +
        "function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "function d(){}" +
        "goog$mixin(b.prototype,a.prototype);" +
        "goog$mixin(c.prototype,a.prototype); new c;" +
        "goog$mixin(d.prototype,a.prototype)",
        "function goog$mixin(){}" +
        "function a(){}" +
        "function c(){}" +
        "goog$mixin(c.prototype,a.prototype); new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass7
  public void testRemoveInheritedClass7() {
    test("function goog$mixin(){}" +
        "function a(){alert(goog$mixin(a, a))}" +
        "function b(){}" +
        "goog$mixin(b.prototype,a.prototype); new a",
        "function goog$mixin(){}" +
        "function a(){alert(goog$mixin(a, a))} new a");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass8
  public void testRemoveInheritedClass8() {
    test("function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "b.inherits(a);c.mixin(b.prototype)",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass9
  public void testRemoveInheritedClass9() {
    testSame("function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "b.inherits(a);c.mixin(b.prototype);new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass10
  public void testRemoveInheritedClass10() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new a;" +
        "var c = a; var d = a.g; new b",
        "function goog$inherits(){}" +
        "function a(){} function b(){} goog$inherits(b,a); new a; new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass11
  public void testRemoveInheritedClass11() {
    testSame("function goog$inherits(){}" +
        "function goog$mixin(a,b){goog$inherits(a,b)}" +
        "function a(){}" +
        "function b(){}" +
        "goog$mixin(b.prototype,a.prototype);new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass12
  public void testRemoveInheritedClass12() {
    testSame("function goog$inherits(){}" +
        "function a(){}" +
        "var b = {};" +
        "goog$inherits(b.foo, a)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testReflectedMethods
  public void testReflectedMethods() {
    this.modifyCallSites = true;
    testSame(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue618_1
  public void testIssue618_1() {
    this.removeGlobal = false;
    testSame(
        "function f() {\n" +
        "  var a = [], b;\n" +
        "  a.push(b = []);\n" +
        "  b[0] = 1;\n" +
        "  return a;\n" +
        "}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue618_2
  public void testIssue618_2() {
    this.removeGlobal = false;
    testSame(
        "var b;\n" +
        "a.push(b = []);\n" +
        "b[0] = 1;\n");
  }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
