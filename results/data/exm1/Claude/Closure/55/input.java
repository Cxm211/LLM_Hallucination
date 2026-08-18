// buggy code
  private static boolean isReduceableFunctionExpression(Node n) {
    return NodeUtil.isFunctionExpression(n);
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
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && a>0);" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue504
  public void testIssue504() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("void function() { alert('hi'); }();",
         "alert('hi');", CheckSideEffects.USELESS_CODE_ERROR);
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

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst1
  public void testReplaceReturnConst1() {
    String source = "a.prototype.foo = function() {return \"foobar\"}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(\"foobar\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst2
  public void testReplaceReturnConst2() {
    checkCompilesToSame("a.prototype.foo = function() {return foobar}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst3
  public void testReplaceReturnConst3() {
    String source = "a.prototype.foo = function() {return void 0;}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(void 0)",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter1
  public void testReplaceGetter1() {
    String source = "a.prototype.foo = function() {return this.foo_}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    GET_HELPER,
                    "a.prototype.foo = JSCompiler_get(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter2
  public void testReplaceGetter2() {
    checkCompilesToSame("a.prototype.foo = function() {return}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter1
  public void testReplaceSetter1() {
    String source = "a.prototype.foo = function(v) {this.foo_ = v}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter2
  public void testReplaceSetter2() {
    String source = "a.prototype.foo = function(v, v2) {this.foo_ = v}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter3
  public void testReplaceSetter3() {
    checkCompilesToSame("a.prototype.foo = function() {this.foo_ = v}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter4
  public void testReplaceSetter4() {
    checkCompilesToSame(
        "a.prototype.foo = function(v, v2) {this.foo_ = v2}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction1
  public void testReplaceEmptyFunction1() {
    String source = "a.prototype.foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "a.prototype.foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction2
  public void testReplaceEmptyFunction2() {
    checkCompilesToSame("function foo() {}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction3
  public void testReplaceEmptyFunction3() {
    String source = "var foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "var foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction1
  public void testReplaceIdentityFunction1() {
    String source = "a.prototype.foo = function(a) {return a}";
    checkCompilesToSame(source, 2);
    checkCompilesTo(source,
                    IDENTITY_HELPER,
                    "a.prototype.foo = JSCompiler_identityFn()",
                    3);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction2
  public void testReplaceIdentityFunction2() {
    checkCompilesToSame("a.prototype.foo = function(a) {return a + 1}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testIssue538
  public void testIssue538() {
    checkCompilesToSame(      "\n" +
        "WebInspector.Setting = function() {}\n" +
        "WebInspector.Setting.prototype = {\n" +
        "    get name0(){return this._name;},\n" +
        "    get name1(){return this._name;},\n" +
        "    get name2(){return this._name;},\n" +
        "    get name3(){return this._name;},\n" +
        "    get name4(){return this._name;},\n" +
        "    get name5(){return this._name;},\n" +
        "    get name6(){return this._name;},\n" +
        "    get name7(){return this._name;},\n" +
        "    get name8(){return this._name;},\n" +
        "    get name9(){return this._name;},\n" +
        "}", 1);
  }
