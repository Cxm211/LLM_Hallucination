// buggy code
    private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && n.getType() == Token.GETPROP
          ) {
        // We want to exclude the assignment itself from the usage list
        boolean isChainedProperty =
            n.getFirstChild().getType() == Token.GETPROP;

        if (isChainedProperty) {
          Node child = n.getFirstChild().getFirstChild().getNext();

          if (child.getType() == Token.STRING &&
              child.getString().equals("prototype")) {
            return true;
          }
        }
      }

      return false;
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
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && a>0);" +
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

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAnalyzePrototypeProperties
  public void testAnalyzePrototypeProperties() {
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.b = function(){};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "var x = new e; x.a()");

    
    test("function e(){}" +
           "e.prototype = {a: function(){}, b: function(){}};" +
           "var x=new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}};" +
           "var x = new e; x.a()");

    
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e; x.a()");
    test("function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing1
  public void testAliasing1() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x = new e; x.method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "var x = new e; x.method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x=new e; x.alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "var x = new e; x.alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing2
  public void testAliasing2() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "(new e).method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing3
  public void testAliasing3() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype['alias1'] = e.prototype.method1;" +
           "e.prototype['alias2'] = e.prototype.method2;",
         "function e(){}" +
           "e.prototype.method1=function(){};" +
           "e.prototype.method2=function(){};" +
           "e.prototype[\"alias1\"]=e.prototype.method1;" +
           "e.prototype[\"alias2\"]=e.prototype.method2;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing4
  public void testAliasing4() {
    
    test("function e(){}" +
           "e.prototype['alias1'] = e.prototype.method1 = function(){};" +
           "e.prototype['alias2'] = e.prototype.method2 = function(){};",
         "function e(){}" +
           "e.prototype[\"alias1\"]=e.prototype.method1=function(){};" +
           "e.prototype[\"alias2\"]=e.prototype.method2=function(){};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing5
  public void testAliasing5() {
    
    
    test("function e(){}" +
           "e.prototype.method1 = function(){this.method2()};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype['alias1'] = e.prototype.method1;",
         "function e(){}" +
           "e.prototype.method1=function(){this.method2()};" +
           "e.prototype.method2=function(){};" +
           "e.prototype[\"alias1\"]=e.prototype.method1;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing6
  public void testAliasing6() {
    
    
    test("function e(){}" +
           "e.prototype.method1 = function(){this.method2()};" +
           "e.prototype.method2 = function(){};" +
           
           "window['alias1'] = e.prototype.method1;",
         "function e(){}" +
           "e.prototype.method1=function(){this.method2()};" +
           "e.prototype.method2=function(){};" +
           "window['alias1']=e.prototype.method1;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing7
  public void testAliasing7() {
    
    
    testSame("function e(){}" +
           "e.prototype['alias1'] = e.prototype.method1 = " +
               "function(){this.method2()};" +
           "e.prototype.method2 = function(){};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testStatementRestriction
  public void testStatementRestriction() {
    test("function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testMethodsFromExternsFileNotExported
  public void testMethodsFromExternsFileNotExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.unused = function() {};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    String compiled =
        "function Foo(){}" +
        "var instance = new Foo;";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExternMethodsFromExternsFile
  public void testExternMethodsFromExternsFile() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.bar_ = function(){};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertyReferenceGraph
  public void testPropertyReferenceGraph() {
    
    
    String constructor = "function Foo() {}";
    String defA =
        "Foo.prototype.a = function() { Foo.superClass_.a.call(this); };";
    String defB = "Foo.prototype.b = function() { this.a(); };";
    String defC = "Foo.prototype.c = function() { " +
        "Foo.superClass_.c.call(this); this.b(); this.a(); };";
    String defD = "Foo.prototype.d = function() { this.c(); };";
    String defE = "Foo.prototype.e = function() { this.a(); this.f(); };";
    String defF = "Foo.prototype.f = function() { };";
    String fullClassDef = constructor + defA + defB + defC + defD + defE + defF;

    
    test(fullClassDef, "");

    
    String callA = "(new Foo()).a();";
    String callB = "(new Foo()).b();";
    String callC = "(new Foo()).c();";
    String callD = "(new Foo()).d();";
    String callE = "(new Foo()).e();";
    String callF = "(new Foo()).f();";
    test(fullClassDef + callA, constructor + defA + callA);
    test(fullClassDef + callB, constructor + defA + defB + callB);
    test(fullClassDef + callC, constructor + defA + defB + defC + callC);
    test(fullClassDef + callD, constructor + defA + defB + defC + defD + callD);
    test(fullClassDef + callE, constructor + defA + defE + defF + callE);
    test(fullClassDef + callF, constructor + defF + callF);

    test(fullClassDef + callA + callC,
         constructor + defA + defB + defC + callA + callC);
    test(fullClassDef + callB + callC,
         constructor + defA + defB + defC + callB + callC);
    test(fullClassDef + callA + callB + callC,
         constructor + defA + defB + defC + callA + callB + callC);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertiesDefinedWithGetElem
  public void testPropertiesDefinedWithGetElem() {
    testSame("function Foo() {} Foo.prototype['elem'] = function() {};");
    testSame("function Foo() {} Foo.prototype[1 + 1] = function() {};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testNeverRemoveImplicitlyUsedProperties
  public void testNeverRemoveImplicitlyUsedProperties() {
    testSame("function Foo() {} " +
             "Foo.prototype.length = 3; " +
             "Foo.prototype.toString = function() { return 'Foo'; }; " +
             "Foo.prototype.valueOf = function() { return 'Foo'; }; ");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertyDefinedInBranch
  public void testPropertyDefinedInBranch() {
    test("function Foo() {} if (true) Foo.prototype.baz = function() {};",
         "if (true);");
    test("function Foo() {} while (true) Foo.prototype.baz = function() {};",
         "while (true);");
    test("function Foo() {} for (;;) Foo.prototype.baz = function() {};",
         "for (;;);");
    test("function Foo() {} do Foo.prototype.baz = function() {}; while(true);",
         "do; while(true);");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testUsingAnonymousObjectsToDefeatRemoval
  public void testUsingAnonymousObjectsToDefeatRemoval() {
    String constructor = "function Foo() {}";
    String declaration = constructor + "Foo.prototype.baz = 3;";
    test(declaration, "");
    testSame(declaration + "var x = {}; x.baz = 5;");
    testSame(declaration + "var x = {baz: 5};");
    test(declaration + "var x = {'baz': 5};",
         "var x = {'baz': 5};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph
  public void testGlobalFunctionsInGraph() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() {}" +
        "Foo.prototype.baz = function() { y(); };",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph2
  public void testGlobalFunctionsInGraph2() {
    
    
    
    
    
    
    testSame(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { y(); };");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph3
  public void testGlobalFunctionsInGraph3() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { x(); };",
        "var x = function() { (new Foo).baz(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { x(); };");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph4
  public void testGlobalFunctionsInGraph4() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { Foo.prototype.baz = function() { y(); }; }",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph5
  public void testGlobalFunctionsInGraph5() {
    test(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };",
        "");

    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };",

        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph6
  public void testGlobalFunctionsInGraph6() {
    testSame(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };" +
        "(new Foo).methodB();");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph7
  public void testGlobalFunctionsInGraph7() {
    testSame(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "this.methodA();");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGetterBaseline
  public void testGetterBaseline() {
    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype = { " +
        "  methodA: function() {}," +
        "  methodB: function() { x(); }" +
        "};" +
        "function x() { (new Foo).methodA(); }",

        "function Foo() {}" +
        "Foo.prototype = { " +
        "  methodA: function() {}" +
        "};" +
        "function x() { (new Foo).methodA(); }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGetter1
  public void testGetter1() {
    test(
      "function Foo() {}" +
      "Foo.prototype = { " +
      "  get methodA() {}," +
      "  get methodB() { x(); }" +
      "};" +
      "function x() { (new Foo).methodA; }",

      "function Foo() {}" +
      "Foo.prototype = {};");

    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}," +
        "  get methodB() { x(); }" +
        "};" +
        "function x() { (new Foo).methodA; }",

        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}" +
        "};" +
        "function x() { (new Foo).methodA; }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGetter2
  public void testGetter2() {
    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}," +
        "  set methodA(a) {}," +
        "  get methodB() { x(); }," +
        "  set methodB(a) { x(); }" +
        "};" +
        "function x() { (new Foo).methodA; }",

        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}," +
        "  set methodA(a) {}" +
        "};" +
        "function x() { (new Foo).methodA; }");
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
