// buggy code
  private void findCalledFunctions(
      Node node, Set<String> changed) {
    Preconditions.checkArgument(changed != null);
    // For each referenced function, add a new reference
    if (node.getType() == Token.CALL) {
      Node child = node.getFirstChild();
      if (child.getType() == Token.NAME) {
        changed.add(child.getString());
      }
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
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

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction1
  public void testInlineEmptyFunction1() {
    
    test("function foo(){}" +
        "foo();",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction2
  public void testInlineEmptyFunction2() {
    
    test("function foo(){}" +
        "foo(1, new Date, function(){});",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction3
  public void testInlineEmptyFunction3() {
    
    test("function foo(){}" +
        "foo();foo();foo();",
        "void 0;void 0;void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction4
  public void testInlineEmptyFunction4() {
    
    test("function foo(){}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction5
  public void testInlineEmptyFunction5() {
    
    
    allowBlockInlining = false;
    testSame("function foo(){}" +
        "foo(x());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions1
  public void testInlineFunctions1() {
    
    test("function foo(){ return 4 }" +
        "foo();",
        "4");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions2
  public void testInlineFunctions2() {
    
    
    test("var t;var AB=function(){return 4};" +
         "function BC(){return 6;}" +
         "CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
         "var t;CD=function(x){return x+5};x=CD(3);y=4;z=6"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions3
  public void testInlineFunctions3() {
    
    test("var t;var AB=function(){return 4};" +
        "function BC(){return 6;}" +
        "var CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
        "var t;x=3+5;y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions4
  public void testInlineFunctions4() {
    
    test("var t; var AB = function() { return 4 }; " +
        "function BC() { return 6; }" +
        "CD = 0;" +
        "CD = function(x) { return x + 5 }; x = CD(3); y = AB(); z = BC();",

        "var t;CD=0;CD=function(x){return x+5};x=CD(3);y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions5
  public void testInlineFunctions5() {
    
    test("var FOO_FN=function(x,y) { return \"de\" + x + \"nu\" + y };" +
         "var a = FOO_FN(\"ez\", \"ts\")",

         "var a=\"de\"+\"ez\"+\"nu\"+\"ts\"");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions6
  public void testInlineFunctions6() {
    
    test("function BAR_FN(x, y, z) { return z(foo(x + y)) }" +
         "alert(BAR_FN(1, 2, baz))",

         "alert(baz(foo(1+2)))");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions7
  public void testInlineFunctions7() {
    
    test("function FN(x,y,z){return x+x+y}" +
         "var b=FN(1,2,3)",

         "var b=1+1+2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions8
  public void testInlineFunctions8() {
    
    test("function MUL(x,y){return x*y}function ADD(x,y){return x+y}" +
         "var a=1+MUL(2,3);var b=2*ADD(3,4)",

         "var a=1+2*3;var b=2*(3+4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions9
  public void testInlineFunctions9() {
    
    test("function INC(x){return x++}" +
         "var y=INC(i)",
         "var y;{var x$$inline_1=i;" +
         "y=x$$inline_1++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions10
  public void testInlineFunctions10() {
    test("function INC(x){return x++}" +
         "var y=INC(i);y=INC(i)",
         "var y;" +
         "{var x$$inline_1=i;" +
         "y=x$$inline_1++}" +
         "{var x$$inline_4=i;" +
         "y=x$$inline_4++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions11
  public void testInlineFunctions11() {
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions12
  public void testInlineFunctions12() {
    
    allowBlockInlining = false;
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
    testSame("function f(x){return x}" +
         "var y=f(i++)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions13
  public void testInlineFunctions13() {
    
    test("function f(x){return x}" +
         "var y=f(i++)",
         "var y;{var x$$inline_1=i++;y=x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions14
  public void testInlineFunctions14() {
    
    test("function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=FOO(1);y=BAR(2)",

         "function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=1;y=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15a
  public void testInlineFunctions15a() {
    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "var d=c+function(a){return a+1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15b
  public void testInlineFunctions15b() {
    
    test("function foo(){var x;return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "function foo(){var x;return function(a){return a+1}}" +
         "var d=c+foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15c
  public void testInlineFunctions15c() {
    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function foo(){return function(a){return a+1}}" +
         "function _x(){ var d=c+foo() }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions16
  public void testInlineFunctions16() {
    
    testSame("function foo(b){return window.bar(function(){c(b)})}" +
             "var d=foo(e)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions17
  public void testInlineFunctions17() {
    
    testSame("function foo(x){return x*x+foo(3)}var bar=foo(4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions18
  public void testInlineFunctions18() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=foo(bar(1),e)",
         "var d=c+e");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions19
  public void testInlineFunctions19() {
    
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=foo(bar(1),e)",
        "var d;{d=c+e}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions20
  public void testInlineFunctions20() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=bar(foo(1,e));",
         "var d=c");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions21
  public void testInlineFunctions21() {
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=bar(foo(1,e))",
        "var d;{d=c}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions22
  public void testInlineFunctions22() {
    
    test("function plex(a){if(a) return 0;else return 1;}" +
         "function foo(a, b){return bar(a+b)}" +
         "function bar(d){return plex(d)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_plex_2:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_plex_2}" +
         "else{" +
         "d=1;break JSCompiler_inline_label_plex_2}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions23
  public void testInlineFunctions23() {
    
    test("function complex(a){if(a) return 0;else return 1;}" +
         "function bar(d){return complex(d)}" +
         "function foo(a, b){return bar(a+b)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_complex_2:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_complex_2" +
         "}else{" +
         "d=1;break JSCompiler_inline_label_complex_2" +
         "}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions24
  public void testInlineFunctions24() {
    
    testSame("function foo(x){return this}foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions25
  public void testInlineFunctions25() {
    testSame("function foo(){return arguments[0]}foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions26
  public void testInlineFunctions26() {
    
    testSame("function _foo(x){return x}_foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions27
  public void testInlineFunctions27() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: 1, z: foo(2)};",
        "var window={};" +
        "{" +
        "  var JSCompiler_inline_result$$0;" +
        "  window.bar++;" +
        "  JSCompiler_inline_result$$0 = 3;" +
        "}" +
        "var x = {y: 1, z: JSCompiler_inline_result$$0};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions28
  public void testInlineFunctions28() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: alert(), z: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$0 = alert();" +
        "{" +
        " var JSCompiler_inline_result$$1;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$1 = 3;}" +
        "var x = {" +
        "  y: JSCompiler_temp_const$$0," +
        "  z: JSCompiler_inline_result$$1" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions29
  public void testInlineFunctions29() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {a: alert(), b: alert2(), c: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$1 = alert();" +
        "var JSCompiler_temp_const$$0 = alert2();" +
        "{" +
        " var JSCompiler_inline_result$$2;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$2 = 3;}" +
        "var x = {" +
        "  a: JSCompiler_temp_const$$1," +
        "  b: JSCompiler_temp_const$$0," +
        "  c: JSCompiler_inline_result$$2" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions30
  public void testInlineFunctions30() {
    
    testSame("function foo(){ return eval() }" +
        "foo();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining1
  public void testMixedModeInlining1() {
    
    test("function foo(){return 1}" +
        "foo();",
        "1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining2
  public void testMixedModeInlining2() {
    
    
    test("function foo(){return 1}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining3
  public void testMixedModeInlining3() {
    
    test("function foo(){return 1}" +
        "foo();foo(x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining4
  public void testMixedModeInlining4() {
    
    
    test("function foo(){return 1}" +
        "foo();foo(x());" +
        "foo(1);foo(1,x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}" +
        "1;{var JSCompiler_inline_anon_param_4=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting1
  public void testMixedModeInliningCosting1() {
    

    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3)",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+4+5;" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+4+5");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting2
  public void testMixedModeInliningCosting2() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting3
  public void testMixedModeInliningCosting3() {
    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+10}" +
        "foo(1,2);" +
        "foo(2,3,x())",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+10;" +
        "{var JSCompiler_inline_anon_param_4=x();" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+10}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting4
  public void testMixedModeInliningCosting4() {
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+101}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified1
  public void testNoInlineIfParametersModified1() {
    
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified2
  public void testNoInlineIfParametersModified2() {
    test("function f(x){return (x)=1;}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified3
  public void testNoInlineIfParametersModified3() {
    
    test("function f(x){return x*=2}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1*=2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified4
  public void testNoInlineIfParametersModified4() {
    
    test("function f(x){return x?(x=2):0}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1?(" +
         "x$$inline_1=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified5
  public void testNoInlineIfParametersModified5() {
    
    test("function f(x,y){return x?(y=2):0}f(2,undefined)",
         "{var y$$inline_3=undefined;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified6
  public void testNoInlineIfParametersModified6() {
    test("function f(x,y){return x?(y=2):0}f(2)",
         "{var y$$inline_3=void 0;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified7
  public void testNoInlineIfParametersModified7() {
    
    test("function f(a){return++a<++a}f(1)",
         "{var a$$inline_1=1;" +
         "++a$$inline_1<" +
         "++a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified8
  public void testNoInlineIfParametersModified8() {
    
    test("function f(a){return a.x=2}f(o)", "o.x=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified9
  public void testNoInlineIfParametersModified9() {
    
    test("function f(a){return a[2]=2}f(o)", "o[2]=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution1
  public void testInlineNeverPartialSubtitution1() {
    test("function f(z){return x.y.z;}f(1)",
         "x.y.z");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution2
  public void testInlineNeverPartialSubtitution2() {
    test("function f(z){return x.y[z];}f(a)",
         "x.y[a]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverMutateConstants
  public void testInlineNeverMutateConstants() {
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverOverrideNewValues
  public void testInlineNeverOverrideNewValues() {
    test("function f(a){return++a<++a}f(1)",
        "{var a$$inline_1=1;" +
        "++a$$inline_1<++a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineMutableArgsReferencedOnce
  public void testInlineMutableArgsReferencedOnce() {
    test("function foo(x){return x;}foo([])", "[]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs1
  public void testNoInlineMutableArgs1() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo([])");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs2
  public void testNoInlineMutableArgs2() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs3
  public void testNoInlineMutableArgs3() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(true&&new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs4
  public void testNoInlineMutableArgs4() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo({})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs1
  public void testInlineBlockMutableArgs1() {
    test("function foo(x){x+x}foo([])",
         "{var x$$inline_1=[];" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs2
  public void testInlineBlockMutableArgs2() {
    test("function foo(x){x+x}foo(new Date)",
         "{var x$$inline_1=new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs3
  public void testInlineBlockMutableArgs3() {
    test("function foo(x){x+x}foo(true&&new Date)",
         "{var x$$inline_1=true&&new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs4
  public void testInlineBlockMutableArgs4() {
    test("function foo(x){x+x}foo({})",
         "{var x$$inline_1={};" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables1
  public void testShadowVariables1() {
    
    

    
    
    test("var a=0;" +
         "function foo(a){return 3+a}" +
         "function bar(){var a=foo(4)}" +
         "bar();",

         "var a=0;" +
         "{var a$$inline_1=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables2
  public void testShadowVariables2() {
    
    
    
    test("var a=0;" +
        "function foo(a){return 3+a}" +
        "function bar(){a=foo(4)}" +
        "bar()",

        "var a=0;" +
        "{a=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables3
  public void testShadowVariables3() {
    
    test("var a=0;" +
        "function foo(){var a=2;return 3+a}" +
        "function _bar(){a=foo()}",

        "var a=0;" +
        "function _bar(){{var a$$inline_1=2;" +
        "a=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables4
  public void testShadowVariables4() {
    
    
    test("var a=0;" +
         "function foo(){return 3+a}" +
         "function _bar(a){a=foo(4)+a}",

         "var a=0;function _bar(a$$1){" +
         "a$$1=" +
         "3+a+a$$1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables5
  public void testShadowVariables5() {
    
    
    allowBlockInlining = false;
    testSame("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables6
  public void testShadowVariables6() {
    test("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)}",

        "var a=0;function _bar(a$$2){{" +
        "var a$$inline_1=4;" +
        "a$$2=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables7
  public void testShadowVariables7() {
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables8
  public void testShadowVariables8() {
    
    test("var a=0;" +
         "function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "var a=0;" +
         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables9
  public void testShadowVariables9() {
    
    test("function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables10
  public void testShadowVariables10() {
    
    test("var a;function foo(){return a}" +
         "function _bar(){var a=foo()}",
         "var a;function _bar(){var a$$1=a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables11
  public void testShadowVariables11() {
    
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var a=foo();alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+a;" +
         "alert(a$$1)}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables12
  public void testShadowVariables12() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var a=foo(),b;alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+b," +
         "b$$1;" +
         "alert(a$$1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables13
  public void testShadowVariables13() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables14
  public void testShadowVariables14() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var c=foo(),b;alert(c)}",
         "var a=0;var b=1;" +
         "function _bar(){var c=a+b," +
         "b$$1;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables15
  public void testShadowVariables15() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c+a)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c+a)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables16
  public void testShadowVariables16() {
    
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables17
  public void testShadowVariables17() {
    test("var a=0;" +
         "function bar(){return a+a}" +
         "function foo(){return bar()}" +
         "function _goo(){var a=2;var x=foo();}",

         "var a=0;" +
         "function _goo(){var a$$1=2;var x=a+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables18
  public void testShadowVariables18() {
    test("var a=0;" +
        "function bar(){return a+a}" +
        "function foo(){var a=3;return bar()}" +
        "function _goo(){var a=2;var x=foo();}",

        "var a=0;" +
        "function _goo(){var a$$2=2;var x;" +
        "{var a$$inline_1=3;x=a+a}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining1
  public void testCostBasedInlining1() {
    testSame(
        "function foo(a){return a}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining2
  public void testCostBasedInlining2() {
    
    
    test(
        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return foo(1)}",

        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return 1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining3
  public void testCostBasedInlining3() {
    
    test(
        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return 1+2}" +
        "function _t2(){return 2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining4
  public void testCostBasedInlining4() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining5
  public void testCostBasedInlining5() {
    
    test(
        "function foo(a,b){return a+b+a+b}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2}" +
        "function _t2(){return 2+3+2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining6
  public void testCostBasedInlining6() {
    
    
    test(
        "function foo(a,b){return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2+1+2+1+2+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t2(){return 2+3+2+3+2+3+2+3+4+5+6+7+8+9+1+2+3+4+5}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining7
  public void testCostBasedInlining7() {
    
    testSame(
        "function foo(a,b){" +
        "    return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5+6}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining8
  public void testCostBasedInlining8() {
    
    
    
    
    
    
    
    allowBlockInlining = false;
    testSame("function f(a){return 1 + a + a;}" +
        "var a = f(f(1));");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining9
  public void testCostBasedInlining9() {
    
    
    
    test("function f(a){return 1 + a + a;}" +
         "var a = f(f(1));",
         "var a;" +
         "{var a$$inline_1=1+1+1;" +
         "a=1+a$$inline_1+a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining10
  public void testCostBasedInlining10() {
    
    
    
    allowBlockInlining = false;
    test("function f(a){return a + a;}" +
        "var a = f(f(1));",
        "var a= 1+1+(1+1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining11
  public void testCostBasedInlining11() {
    
    test("function f(a){return a + a;}" +
         "var a = f(f(1))",
         "var a;" +
         "{var a$$inline_1=1+1;" +
         "a=a$$inline_1+a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining12
  public void testCostBasedInlining12() {
    test("function f(a){return 1 + a + a;}" +
         "var a = f(1) + f(2);",

         "var a=1+1+1+(1+2+2)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex1
  public void testCostBasedInliningComplex1() {
    testSame(
        "function foo(a){a()}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex2
  public void testCostBasedInliningComplex2() {
    
    
    test(
        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){foo(x)}",

        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){{x()}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex3
  public void testCostBasedInliningComplex3() {
    
    test(
        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){{1+2}}" +
        "function _t2(){{2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex4
  public void testCostBasedInliningComplex4() {
    
    
    testSame(
        "function foo(a,b){a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex5
  public void testCostBasedInliningComplex5() {
    
    test(
        "function foo(a,b){a+b+a+b}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2}}" +
        "function _t2(){{2+3+2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex6
  public void testCostBasedInliningComplex6() {
    
    
    test(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2+1+2+1+2+4+5+6+7+8+9+1}}" +
        "function _t2(){{2+3+2+3+2+3+2+3+4+5+6+7+8+9+1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex7
  public void testCostBasedInliningComplex7() {
    
    testSame(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex8
  public void testCostBasedInliningComplex8() {
    
    testSame("function _f(a){1+a+a}" +
             "a=_f(1)+_f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex9
  public void testCostBasedInliningComplex9() {
    test("function f(a){1 + a + a;}" +
         "f(1);f(2);",
         "{1+1+1}{1+2+2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining1
  public void testDoubleInlining1() {
    allowBlockInlining = false;
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "getWindow(x)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining2
  public void testDoubleInlining2() {
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "{getWindow(x)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction1
  public void testNoInlineOfNonGlobalFunction1() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction2
  public void testNoInlineOfNonGlobalFunction2() {
    test("var g;function _f(){var g=function(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction3
  public void testNoInlineOfNonGlobalFunction3() {
    test("var g;function _f(){var g=function(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction4
  public void testNoInlineOfNonGlobalFunction4() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMaskedFunction
  public void testNoInlineMaskedFunction() {
    
    
    test("var g=function(){return 0};" +
         "function _f(g){return g()}",
         "function _f(g$$1){return g$$1()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineNonFunction
  public void testNoInlineNonFunction() {
    testSame("var g=3;function _f(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineCall
  public void testInlineCall() {
    test("function f(g) { return g.h(); } f('x');",
         "\"x\".h()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch1
  public void testInlineFunctionWithArgsMismatch1() {
    test("function f(g) { return g; } f();",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch2
  public void testInlineFunctionWithArgsMismatch2() {
    test("function f() { return 0; } f(1);",
         "0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch3
  public void testInlineFunctionWithArgsMismatch3() {
    test("function f(one, two, three) { return one + two + three; } f(1);",
         "1+void 0+void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch4
  public void testInlineFunctionWithArgsMismatch4() {
    test("function f(one, two, three) { return one + two + three; }" +
         "f(1,2,3,4,5);",
         "1+2+3");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testArgumentsWithSideEffectsNeverInlined1
  public void testArgumentsWithSideEffectsNeverInlined1() {
    allowBlockInlining = false;
    testSame("function f(){return 0} f(new goo());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testArgumentsWithSideEffectsNeverInlined2
  public void testArgumentsWithSideEffectsNeverInlined2() {
    allowBlockInlining = false;
    testSame("function f(g,h){return h+g}f(g(),h());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testOneSideEffectCallDoesNotRuinOthers
  public void testOneSideEffectCallDoesNotRuinOthers() {
    allowBlockInlining = false;
    test("function f(){return 0}f(new goo());f()",
         "function f(){return 0}f(new goo());0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall1
  public void testComplexInlineNoResultNoParamCall1() {
    test("function f(){a()}f()",
         "{a()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall2
  public void testComplexInlineNoResultNoParamCall2() {
   test("function f(){if (true){return;}else;} f();",
         "{JSCompiler_inline_label_f_0:{" +
             "if(true)break JSCompiler_inline_label_f_0;else;}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall3
  public void testComplexInlineNoResultNoParamCall3() {
    
    
    

    
    test("function f(){a();b();var z=1+1}function _foo(){f()}",
         "function _foo(){{a();b();var z$$inline_1=1+1}}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall1
  public void testComplexInlineNoResultWithParamCall1() {
    test("function f(x){a(x)}f(1)",
         "{a(1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall2
  public void testComplexInlineNoResultWithParamCall2() {
    test("function f(x,y){a(x)}var b=1;f(1,b)",
         "var b=1;{a(1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall3
  public void testComplexInlineNoResultWithParamCall3() {
    test("function f(x,y){if (x) y(); return true;}var b=1;f(1,b)",
         "var b=1;{if(1)b();true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline1
  public void testComplexInline1() {
    test("function f(){if (true){return;}else;} z=f();",
         "{JSCompiler_inline_label_f_0:" +
         "{if(true){z=void 0;" +
         "break JSCompiler_inline_label_f_0}else;z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline2
  public void testComplexInline2() {
    test("function f(){if (true){return;}else return;} z=f();",
         "{JSCompiler_inline_label_f_0:{if(true){z=void 0;" +
         "break JSCompiler_inline_label_f_0}else{z=void 0;" +
         "break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline3
  public void testComplexInline3() {
    test("function f(){if (true){return 1;}else return 0;} z=f();",
         "{JSCompiler_inline_label_f_0:{if(true){z=1;" +
         "break JSCompiler_inline_label_f_0}else{z=0;" +
         "break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline4
  public void testComplexInline4() {
    test("function f(x){a(x)} z = f(1)",
         "{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline5
  public void testComplexInline5() {
    test("function f(x,y){a(x)}var b=1;z=f(1,b)",
         "var b=1;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline6
  public void testComplexInline6() {
    test("function f(x,y){if (x) y(); return true;}var b=1;z=f(1,b)",
         "var b=1;{if(1)b();z=true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline7
  public void testComplexInline7() {
    test("function f(x,y){if (x) return y(); else return true;}" +
         "var b=1;z=f(1,b)",
         "var b=1;{JSCompiler_inline_label_f_4:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_4}else{z=true;" +
         "break JSCompiler_inline_label_f_4}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline8
  public void testComplexInline8() {
    test("function f(x){a(x)}var z=f(1)",
         "var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars1
  public void testComplexInlineVars1() {
    test("function f(){if (true){return;}else;}var z=f();",
         "var z;{JSCompiler_inline_label_f_0:{" +
         "if(true){z=void 0;break JSCompiler_inline_label_f_0}else;z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars2
  public void testComplexInlineVars2() {
    test("function f(){if (true){return;}else return;}var z=f();",
        "var z;{JSCompiler_inline_label_f_0:{" +
        "if(true){z=void 0;break JSCompiler_inline_label_f_0" +
        "}else{" +
        "z=void 0;break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars3
  public void testComplexInlineVars3() {
    test("function f(){if (true){return 1;}else return 0;}var z=f();",
         "var z;{JSCompiler_inline_label_f_0:{if(true){" +
         "z=1;break JSCompiler_inline_label_f_0" +
         "}else{" +
         "z=0;break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars4
  public void testComplexInlineVars4() {
    test("function f(x){a(x)}var z = f(1)",
         "var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars5
  public void testComplexInlineVars5() {
    test("function f(x,y){a(x)}var b=1;var z=f(1,b)",
         "var b=1;var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars6
  public void testComplexInlineVars6() {
    test("function f(x,y){if (x) y(); return true;}var b=1;var z=f(1,b)",
         "var b=1;var z;{if(1)b();z=true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars7
  public void testComplexInlineVars7() {
    test("function f(x,y){if (x) return y(); else return true;}" +
         "var b=1;var z=f(1,b)",
         "var b=1;var z;" +
         "{JSCompiler_inline_label_f_4:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_4" +
         "}else{" +
         "z=true;break JSCompiler_inline_label_f_4}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars8
  public void testComplexInlineVars8() {
    test("function f(x){a(x)}var x;var z=f(1)",
         "var x;var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars9
  public void testComplexInlineVars9() {
    test("function f(x){a(x)}var x;var z=f(1);var y",
         "var x;var z;{a(1);z=void 0}var y");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars10
  public void testComplexInlineVars10() {
    test("function f(x){a(x)}var x=blah();var z=f(1);var y=blah();",
          "var x=blah();var z;{a(1);z=void 0}var y=blah()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars11
  public void testComplexInlineVars11() {
    test("function f(x){a(x)}var x=blah();var z=f(1);var y;",
         "var x=blah();var z;{a(1);z=void 0}var y");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars12
  public void testComplexInlineVars12() {
    test("function f(x){a(x)}var x;var z=f(1);var y=blah();",
         "var x;var z;{a(1);z=void 0}var y=blah()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions1
  public void testComplexInlineInExpresssions1() {
    test("function f(){a()}var z=f()",
         "var z;{a();z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions2
  public void testComplexInlineInExpresssions2() {
    test("function f(){a()}c=z=f()",
         "{var JSCompiler_inline_result$$0;a();}" +
         "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions3
  public void testComplexInlineInExpresssions3() {
    test("function f(){a()}c=z=f()",
        "{var JSCompiler_inline_result$$0;a();}" +
        "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions4
  public void testComplexInlineInExpresssions4() {
    test("function f(){a()}if(z=f());",
        "{var JSCompiler_inline_result$$0;a();}" +
        "if(z=JSCompiler_inline_result$$0);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions5
  public void testComplexInlineInExpresssions5() {
    test("function f(){a()}if(z.y=f());",
         "var JSCompiler_temp_const$$0=z;" +
         "{var JSCompiler_inline_result$$1;a()}" +
         "if(JSCompiler_temp_const$$0.y=JSCompiler_inline_result$$1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoInline1
  public void testComplexNoInline1() {
    testSame("function f(){a()}while(z=f())continue");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoInline2
  public void testComplexNoInline2() {
    testSame("function f(){a()}do;while(z=f())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexSample
  public void testComplexSample() {
    String result = "" +
      "{{" +
      "var styleSheet$$inline_9=null;" +
      "if(goog$userAgent$IE)" +
        "styleSheet$$inline_9=0;" +
      "else " +
        "var head$$inline_10=0;" +
      "{" +
        "var element$$inline_11=" +
            "styleSheet$$inline_9;" +
        "var stylesString$$inline_12=a;" +
        "if(goog$userAgent$IE)" +
          "element$$inline_11.cssText=" +
              "stylesString$$inline_12;" +
        "else " +
        "{" +
          "var propToSet$$inline_13=" +
              "\"innerText\";" +
          "element$$inline_11[" +
              "propToSet$$inline_13]=" +
                  "stylesString$$inline_12" +
        "}" +
      "}" +
      "styleSheet$$inline_9" +
      "}}";

    test("var foo = function(stylesString, opt_element) { " +
        "var styleSheet = null;" +
        "if (goog$userAgent$IE)" +
          "styleSheet = 0;" +
        "else " +
          "var head = 0;" +
        "" +
        "goo$zoo(styleSheet, stylesString);" +
        "return styleSheet;" +
     " };\n " +

     "var goo$zoo = function(element, stylesString) {" +
        "if (goog$userAgent$IE)" +
          "element.cssText = stylesString;" +
        "else {" +
          "var propToSet = 'innerText';" +
          "element[propToSet] = stylesString;" +
        "}" +
      "};" +
      "(function(){foo(a,b);})();",
     result);
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexSampleNoInline
  public void testComplexSampleNoInline() {
    
    String result =
    "foo=function(stylesString,opt_element){" +
        "var styleSheet=null;" +
        "if(goog$userAgent$IE){" +
          "styleSheet=0" +
        "}else{" +
          "var head=0" +
         "}" +
         "{var JSCompiler_inline_element_0=styleSheet;" +
         "var JSCompiler_inline_stylesString_1=stylesString;" +
         "if(goog$userAgent$IE){" +
           "JSCompiler_inline_element_0.cssText=" +
           "JSCompiler_inline_stylesString_1" +
         "}else{" +
           "var propToSet=goog$userAgent$WEBKIT?\"innerText\":\"innerHTML\";" +
           "JSCompiler_inline_element_0[propToSet]=" +
           "JSCompiler_inline_stylesString_1" +
         "}}" +
        "return styleSheet" +
     "}";

    testSame(
      "foo=function(stylesString,opt_element){" +
        "var styleSheet=null;" +
        "if(goog$userAgent$IE)" +
          "styleSheet=0;" +
        "else " +
          "var head=0;" +
        "" +
        "goo$zoo(styleSheet,stylesString);" +
        "return styleSheet" +
     "};" +
     "goo$zoo=function(element,stylesString){" +
        "if(goog$userAgent$IE)" +
          "element.cssText=stylesString;" +
        "else{" +
          "var propToSet=goog$userAgent$WEBKIT?\"innerText\":\"innerHTML\";" +
          "element[propToSet]=stylesString" +
        "}" +
      "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoVarSub
  public void testComplexNoVarSub() {
    test(
        "function foo(x){" +
          "var x;" +
          "y=x" +
        "}" +
        "foo(1)",

        "{y=1}"
        );
   }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition1
  public void testComplexFunctionWithFunctionDefinition1() {
    test("function f(){call(function(){return})}f()",
         "{call(function(){return})}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition2
  public void testComplexFunctionWithFunctionDefinition2() {
    
    testSame("function f(a){call(function(){return})}f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition3
  public void testComplexFunctionWithFunctionDefinition3() {
    
    testSame("function f(){var a; call(function(){return})}f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposePlusEquals
  public void testDecomposePlusEquals() {
    test("function f(){a=1;return 1} var x = 1; x += f()",
        "var x = 1;" +
        "var JSCompiler_temp_const$$0 = x;" +
        "{var JSCompiler_inline_result$$1; a=1;" +
        " JSCompiler_inline_result$$1=1}" +
        "x = JSCompiler_temp_const$$0 + JSCompiler_inline_result$$1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposeFunctionExpressionInCall
  public void testDecomposeFunctionExpressionInCall() {
    test(
        "(function(map){descriptions_=map})(\n" +
           "function(){\n" +
              "var ret={};\n" +
              "ret[ONE]='a';\n" +
              "ret[TWO]='b';\n" +
              "return ret\n" +
           "}()\n" +
        ");",
        "{" +
        "var JSCompiler_inline_result$$0;" +
        "var ret$$inline_2={};\n" +
        "ret$$inline_2[ONE]='a';\n" +
        "ret$$inline_2[TWO]='b';\n" +
        "JSCompiler_inline_result$$0 = ret$$inline_2;\n" +
        "}" +
        "{" +
        "descriptions_=JSCompiler_inline_result$$0;" +
        "}"
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor1
  public void testInlineConstructor1() {
    test("function f() {} function _g() {f.call(this)}",
         "function _g() {void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor2
  public void testInlineConstructor2() {
    test("function f() {} f.prototype.a = 0; function _g() {f.call(this)}",
         "function f() {} f.prototype.a = 0; function _g() {void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor3
  public void testInlineConstructor3() {
    test("function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {f.call(this)}",
         "function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {{x.call(this)}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor4
  public void testInlineConstructor4() {
    test("function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {var t = f.call(this)}",
         "function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {var t; {x.call(this); t = void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining1
  public void testFunctionExpressionInlining1() {
    test("(function(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining2
  public void testFunctionExpressionInlining2() {
    test("(function(){foo()})()",
         "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining3
  public void testFunctionExpressionInlining3() {
    test("var a = (function(){return foo()})()",
         "var a = foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining4
  public void testFunctionExpressionInlining4() {
    test("var a; a = 1 + (function(){return foo()})()",
         "var a; a = 1 + foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining1
  public void testFunctionExpressionCallInlining1() {
    test("(function(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining2
  public void testFunctionExpressionCallInlining2() {
    test("(function(){foo(this)}).call(this)",
         "{foo(this)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining3
  public void testFunctionExpressionCallInlining3() {
    test("var a = (function(){return foo(this)}).call(this)",
         "var a = foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining4
  public void testFunctionExpressionCallInlining4() {
    test("var a; a = 1 + (function(){return foo(this)}).call(this)",
         "var a; a = 1 + foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining5
  public void testFunctionExpressionCallInlining5() {
    test("a:(function(){return foo()})()",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining6
  public void testFunctionExpressionCallInlining6() {
    test("a:(function(){return foo()}).call(this)",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining7
  public void testFunctionExpressionCallInlining7() {
    test("a:(function(){})()",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining8
  public void testFunctionExpressionCallInlining8() {
    test("a:(function(){}).call(this)",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining9
  public void testFunctionExpressionCallInlining9() {
    
    test("(function foo(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining10
  public void testFunctionExpressionCallInlining10() {
    
    test("(function foo(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11a
  public void testFunctionExpressionCallInlining11a() {
    
    test("((function(){return function(){foo()}})())();", "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11b
  public void testFunctionExpressionCallInlining11b() {
    
    testSame("((function(){var a; return function(){foo()}})())();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11c
  public void testFunctionExpressionCallInlining11c() {
    
    testSame("function _x() {" +
                "((function(){return function(){foo()}})())();" +
                "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining12
  public void testFunctionExpressionCallInlining12() {
    
    testSame("(function foo(){foo()})()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionOmega
  public void testFunctionExpressionOmega() {
    
    test("(function (f){f(f)})(function(f){f(f)})",
         "{var f$$inline_1=function(f$$1){f$$1(f$$1)};" +
          "{{f$$inline_1(f$$inline_1)}}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining1
  public void testLocalFunctionInlining1() {
    test("function _f(){ function g() {} g() }",
         "function _f(){ void 0 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining2
  public void testLocalFunctionInlining2() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining3
  public void testLocalFunctionInlining3() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining4
  public void testLocalFunctionInlining4() {
    test("function _f(){ function g() {return 1} return g() }",
         "function _f(){ return 1 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining5
  public void testLocalFunctionInlining5() {
    testSame("function _f(){ function g() {this;} g() }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining6
  public void testLocalFunctionInlining6() {
    testSame("function _f(){ function g() {this;} return g; }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly1
  public void testLocalFunctionInliningOnly1() {
    this.allowGlobalFunctionInlining = true;
    test("function f(){} f()", "void 0;");
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly2
  public void testLocalFunctionInliningOnly2() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("function f(){ function g() {return 1} return g() }; f();",
         "function f(){ return 1 }; f();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly3
  public void testLocalFunctionInliningOnly3() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ function g() {return 1} return g() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly4
  public void testLocalFunctionInliningOnly4() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ return (function() {return 1})() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionYCombinator
  public void testFunctionExpressionYCombinator() {
    testSame(
        "var factorial = ((function(M) {\n" +
        "      return ((function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                            })\n" +
        "               })\n" +
        "              (function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                           })\n" +
        "                 }));\n" +
        "     })\n" +
        "    (function(f) {\n" +
        "       return function(n) {\n" +
        "        if (n === 0)\n" +
        "          return 1;\n" +
        "        else\n" +
        "          return n * f(n - 1);\n" +
        "       };\n" +
        "     }));\n" +
        "\n" +
        "factorial(5)\n");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("function JSCompiler_renameProperty(x) {return x} " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testReplacePropertyFunction
  public void testReplacePropertyFunction() {
    
    
    test("function f(x) {return x} " +
         "foo(window, f); f(1)",
         "function f(x) {return x} " +
         "foo(window, f); 1");
    
    
    testSame("function f(x) {return x} " +
             "new JSCompiler_ObjectPropertyString(window, f); f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue423
  public void testIssue423() {
    test(
        "(function($) {\n" +
        "  $.fn.multicheck = function(options) {\n" +
        "    initialize.call(this, options);\n" +
        "  };\n" +
        "\n" +
        "  function initialize(options) {\n" +
        "    options.checkboxes = $(this).siblings(':checkbox');\n" +
        "    preload_check_all.call(this);\n" +
        "  }\n" +
        "\n" +
        "  function preload_check_all() {\n" +
        "    $(this).data('checkboxes');\n" +
        "  }\n" +
        "})(jQuery)",
        "(function($){" +
        "  $.fn.multicheck=function(options$$1){" +
        "    {" +
        "     options$$1.checkboxes=$(this).siblings(\":checkbox\");" +
        "     {" +
        "       $(this).data(\"checkboxes\")" +
        "     }" +
        "    }" +
        "  }" +
        "})(jQuery)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining1
  public void testCrossModuleInlining1() {
    test(createModuleChain(
             
             "function foo(){return f(1)+g(2)+h(3);}",
             
             "foo()"
             ),
         new String[] {
             
             "",
             
             "f(1)+g(2)+h(3);"
            }
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining2
  public void testCrossModuleInlining2() {
    testSame(createModuleChain(
                
                "foo()",
                
                "function foo(){return f(1)+g(2)+h(3);}"
                )
            );

    test(createModuleChain(
             
             "foo()",
             
             "function foo(){return f();}"
             ),
         new String[] {
             
             "f();",
             
             ""
            }
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining3
  public void testCrossModuleInlining3() {
    testSame(createModuleChain(
                
                "foo()",
                
                "function foo(){return f(1)+g(2)+h(3);}",
                
                "foo()"
                )
            );

    test(createModuleChain(
             
             "foo()",
             
             "function foo(){return f();}",
             
             "foo()"
             ),
         new String[] {
             
             "f();",
             
             "",
             
             "f();"
            }
         );
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
