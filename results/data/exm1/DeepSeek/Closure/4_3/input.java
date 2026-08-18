// buggy code
  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> enclosing) {
    // TODO(user): Investigate whether it is really necessary to keep two
    // different mechanisms for resolving named types, and if so, which order
    // makes more sense. Now, resolution via registry is first in order to
    // avoid triggering the warnings built into the resolution via properties.
    boolean resolved = resolveViaRegistry(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
    }

    if (resolved) {
      super.resolveInternal(t, enclosing);
      finishPropertyContinuations();
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    resolveViaProperties(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
    }

    super.resolveInternal(t, enclosing);
    if (isResolved()) {
      finishPropertyContinuations();
    }
    return registry.isLastGeneration() ?
        getReferencedType() : this;
  }

// relevant test
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn7
  public void testSourcePruningOn7() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "var COMPILED = false;",
         },
         new String[] {
          "var COMPILED = !1;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn8
  public void testSourcePruningOn8() {
    args.add("--only_closure_dependencies");
    args.add("--closure_entry_point=scotch");
    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "\n" +
          "var externVar;",
          "goog.provide('scotch'); var x = externVar;"
         },
         new String[] {
           "var scotch = {}, x = externVar;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoCompile
  public void testNoCompile() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "\n" +
          "goog.provide('x');\n" +
          "var dupeVar;",
          "var dupeVar;"
         },
         new String[] {
           "var dupeVar;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDependencySortingWhitespaceMode
  public void testDependencySortingWhitespaceMode() {
    args.add("--manage_closure_dependencies");
    args.add("--compilation_level=WHITESPACE_ONLY");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');\ngoog.require('hops');",
          "goog.provide('hops');",
         },
         new String[] {
          "goog.provide('hops');",
          "goog.provide('beer');\ngoog.require('hops');",
          "goog.require('beer');"
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testOnlyClosureDependenciesEmptyEntryPoints
  public void testOnlyClosureDependenciesEmptyEntryPoints() throws Exception {
    
    args.add("--use_only_custom_externs=true");

    args.add("--only_closure_dependencies=true");
    try {
      CommandLineRunner runner = createCommandLineRunner(new String[0]);
      runner.doRun();
      fail("Expected FlagUsageException");
    } catch (FlagUsageException e) {
      assertTrue(e.getMessage(),
          e.getMessage().contains("only_closure_dependencies"));
    }
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOnlyClosureDependenciesOneEntryPoint
  public void testOnlyClosureDependenciesOneEntryPoint() throws Exception {
    args.add("--only_closure_dependencies=true");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.require('beer'); var beerRequired = 1;",
          "goog.provide('beer');\ngoog.require('hops');\nvar beerProvided = 1;",
          "goog.provide('hops'); var hopsProvided = 1;",
          "goog.provide('scotch'); var scotchProvided = 1;",
          "goog.require('scotch');\nvar includeFileWithoutProvides = 1;",
          "\nvar COMPILED = false;",
         },
         new String[] {
           "var COMPILED = !1;",
           "var hops = {}, hopsProvided = 1;",
           "var beer = {}, beerProvided = 1;"
         });
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testOutputModuleGraphJson
  public void testOutputModuleGraphJson() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
        "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphJsonTo(
        lastCompiler.getModuleGraph(), builder);
    assertTrue(builder.toString().indexOf("transitive-dependencies") != -1);
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
        SourceFile.fromCode("externs", "myVar.property;"));
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5ChecksByDefault
  public void testES5ChecksByDefault() {
    testSame("var x = 3; delete x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5ChecksInVerbose
  public void testES5ChecksInVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { delete x; }", StrictModeCheck.DELETE_VARIABLE);
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
    test("function f(x) { delete x; }", StrictModeCheck.DELETE_VARIABLE);
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testTransformAMD
  public void testTransformAMD() {
    args.add("--transform_amd_modules");
    test("define({test: 1})", "exports = {test: 1}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessCJS
  public void testProcessCJS() {
    useStringComparison = true;
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    String expected = "var module$foo$bar={test:1};";
    test("exports.test = 1", expected);
    assertEquals(expected + "\n", outReader.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessCJSWithModuleOutput
  public void testProcessCJSWithModuleOutput() {
    useStringComparison = true;
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    args.add("--module=auto");
    setFilename(0, "foo/bar.js");
    test("exports.test = 1",
        "var module$foo$bar={test:1};");
    
    assertEquals("", outReader.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFormattingSingleQuote
  public void testFormattingSingleQuote() {
    testSame("var x = '';");
    assertEquals("var x=\"\";", lastCompiler.toSource());

    args.add("--formatting=SINGLE_QUOTES");
    testSame("var x = '';");
    assertEquals("var x='';", lastCompiler.toSource());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTransformAMDAndProcessCJS
  public void testTransformAMDAndProcessCJS() {
    useStringComparison = true;
    args.add("--transform_amd_modules");
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    test("define({foo: 1})",
        "var module$foo$bar={},module$foo$bar={foo:1};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleJSON
  public void testModuleJSON() {
    useStringComparison = true;
    args.add("--transform_amd_modules");
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    args.add("--output_module_dependencies=test.json");
    setFilename(0, "foo/bar.js");
    test("define({foo: 1})",
        "var module$foo$bar={},module$foo$bar={foo:1};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOutputSameAsInput
  public void testOutputSameAsInput() {
    args.add("--js_output_file=" + getFilename(0));
    test("", AbstractCommandLineRunner.OUTPUT_SAME_AS_INPUT_ERROR);
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
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode(
            "gin", "goog.provide('gin'); goog.require('tonic'); var gin = {};"),
        SourceFile.fromCode("tonic",
            "goog.provide('tonic'); goog.require('gin'); var tonic = {};"),
        SourceFile.fromCode(
            "mix", "goog.require('gin'); goog.require('tonic');"));
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    options.setManageClosureDependencies(true);
    Compiler compiler = new Compiler();
    compiler.init(ImmutableList.<SourceFile>of(), inputs, options);
    compiler.parseInputs();
    assertEquals(compiler.externAndJsRoot, compiler.jsRoot.getParent());
    assertEquals(compiler.externAndJsRoot, compiler.externsRoot.getParent());
    assertNotNull(compiler.externAndJsRoot);

    Node jsRoot = compiler.jsRoot;
    assertEquals(3, jsRoot.getChildCount());
  }

// com.google.javascript.jscomp.CompilerTest::testLocalUndefined
  public void testLocalUndefined() throws Exception {
    
    
    
    
    
    
    
    
    CompilerOptions options = new CompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    Compiler compiler = new Compiler();
    SourceFile externs = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js",
        "(function (undefined) { alert(undefined); })();");
    compiler.compile(externs, input, options);
  }

// com.google.javascript.jscomp.CompilerTest::testCommonJSProvidesAndRequire
  public void testCommonJSProvidesAndRequire() throws Exception {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("gin.js", "require('tonic')"),
        SourceFile.fromCode("tonic.js", ""),
        SourceFile.fromCode("mix.js", "require('gin'); require('tonic');"));
    List<String> entryPoints = Lists.newArrayList("module$mix");

    Compiler compiler = initCompilerForCommonJS(inputs, entryPoints);
    JSModuleGraph graph = compiler.getModuleGraph();
    assertEquals(4, graph.getModuleCount());
    List<CompilerInput> result = graph.manageDependencies(entryPoints,
        compiler.getInputsForTesting());
    assertEquals("[root]", result.get(0).getName());
    assertEquals("[module$tonic]", result.get(1).getName());
    assertEquals("[module$gin]", result.get(2).getName());
    assertEquals("tonic.js", result.get(3).getName());
    assertEquals("gin.js", result.get(4).getName());
    assertEquals("mix.js", result.get(5).getName());
  }

// com.google.javascript.jscomp.CompilerTest::testCommonJSMissingRequire
  public void testCommonJSMissingRequire() throws Exception {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("gin.js", "require('missing')"));
    Compiler compiler = initCompilerForCommonJS(
        inputs, ImmutableList.of("module$gin"));
    compiler.processAMDAndCommonJSModules();

    assertEquals(1, compiler.getErrorManager().getErrorCount());
    String error = compiler.getErrorManager().getErrors()[0].toString();
    assertTrue(
        "Unexpected error: " + error,
        error.contains(
            "required entry point \"module$missing\" never provided"));
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

    
    
    for (int i = 0; i < 3; ++i) {
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

// com.google.javascript.jscomp.ConstCheckTest::testConstAnnotation
  public void testConstAnnotation() {
    testError(" var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstSuppression
  public void testConstSuppression() {
    testSame("\n" +
             " var xyz = 1; xyz = 3;");
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

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith3
  public void testUseOfWith3() {
    testSame(
        "function f(expr, context) {\n" +
        "  try {\n" +
        "     with (context) {\n" +
        "      return eval('[' + expr + '][0]');\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    return null;\n" +
        "  }\n" +
        "};\n");
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

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testQuotedProps
  public void testQuotedProps() {
    testSame("({'':0})");
    testSame("({'1.0':0})");
    testSame("({'\u1d17A':0})");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::test5746867
  public void test5746867() {
    testSame("var a = { '$\\\\' : 5 };");
    testSame("var a = { 'x\\\\u0041$\\\\' : 5 };");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function f() { if (x) return; y(); }",
         "function f(){x||y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function f(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1a
  public void testFoldWithMarkers1a() {
    testSame("function f(){startMarker();if(x)return;endMarker()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function f() { if (x) return; y(); if (a) return; b(); }",
         "function f(){if(!x){y();a||b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function f(){startMarker(\"FOO\");startMarker(\"BAR\");" +
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

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid1
  public void testInvalid1() {
    test("startMarker() && true",
        "startMarker()", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid2
  public void testInvalid2() {
    test("false && endMarker()",
        "", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
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

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testContainingBlockPreservation
  public void testContainingBlockPreservation() {
    testSame("if(y){startMarker();x();endMarker()}");
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
    inFunction("var x; x=2; if(0&&(x=1)){}; x");

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

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentSamples
  public void testAssignmentSamples() {
    
    inFunction("var x = 2;");
    inFunction("var x = 2; x++;", "var x=2; void 0");
    inFunction("var x; x=x++;", "var x;x++");
    inFunction("var x; x+=1;", "var x;x+1");
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

    
    inFunction("var a; a = (a++)", "var a; a++"); 
    inFunction("var a; a = (++a)", "var a; ++a"); 

    inFunction("var a; a = (b = (a = 1))", "var a; b = 1");
    inFunction("var a; a = (b = (a *= 2))", "var a; b = a * 2");
    inFunction("var a; a = (b = (a++))", "var a; b=a++");
    inFunction("var a; a = (b = (++a))", "var a; b=++a");

    
    inFunction("var a,b; a = (b = (a = 1))", "var a,b; 1");
    inFunction("var a,b; a = (b = (a *= 2))", "var a,b; a * 2");
    inFunction("var a,b; a = (b = (a++))",
               "var a,b; a++"); 
    inFunction("var a,b; a = (b = (++a))",
               "var a,b; ++a"); 

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
    inFunction("extern = true;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297a
  public void testIssue297a() {
    testSame("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}; f('');");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297b
  public void testIssue297b() {
    test("function f() {" +
         " var x;" +
         " return (x='') && (x = x.substr(1));" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x='') && (x.substr(1));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297c
  public void testIssue297c() {
    test("function f() {" +
         " var x;" +
         " return (x=1) && (x = f(x));" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x=1) && f(x);" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297d
  public void testIssue297d() {
    test("function f(a) {" +
         " return (a=1) && (a = f(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) && (f(a));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297e
  public void testIssue297e() {
    test("function f(a) {" +
         " return (a=1) - (a = g(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) - (g(a));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297f
  public void testIssue297f() {
    test("function f(a) {" +
         " h((a=1) - (a = g(a)));" +
         "};",
         "function f(a) {" +
         " h((a=1) - (g(a)));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297g
  public void testIssue297g() {
    test("function f(a) {" +
         " var b = h((b=1) - (b = g(b)));" +
         " return b;" +
         "};",
         
         "function f(a) {" +
         " var b = h((b=1) - (b = g(b)));" +
         " return b;" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297h
  public void testIssue297h() {
    test("function f(a) {" +
         " var b = b=1;" +
         " return b;" +
         "};",
         
         "function f(a) {" +
         " var b = b = 1;" +
         " return b;" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInExpression1
  public void testInExpression1() {
    inFunction("var a; return a=(a=(a=3));", "var a; return 3;");
    inFunction("var a; return a=(a=(a=a));", "var a; return a;");
    inFunction("var a; return a=(a=(a=a+1)+1);", "var a; return a+1+1;");
    inFunction("var a; return a=(a=(a=f(a)+1)+1);", "var a; return f(a)+1+1;");
    inFunction("var a; return a=f(a=f(a=f(a)));", "var a; return f(f(f(a)));");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInExpression2
  public void testInExpression2() {
    
    
    inFunction(
        "var a; a = 1; if ((a = 2) || (a = 3) || (a)) {}",
        "var a; a = 1; if ((    2) || (a = 3) || (a)) {}");

    inFunction(
        "var a; (a = 1) || (a = 2)",
        "var a; 1 || 2");

    inFunction("var a; (a = 1) || (a = 2); return a");

    inFunction(
        "var a; a = 1; a ? a = 2 : a;",
        "var a; a = 1; a ?     2 : a;");

    inFunction("var a; a = 1; a ? a = 2 : a; return a");

    inFunction(
        "var a; a = 1; a ? a : a = 2;",
        "var a; a = 1; a ? a : 2;");

    inFunction("var a; a = 1; a ? a : a =2; return a");

    inFunction(
        "var a; (a = 1) ? a = 2 : a = 3;",
        "var a;      1  ?     2 :     3;");

    
    
    inFunction("var a; (a = 1) ? a = 2 : a = 3; return a");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384a
  public void testIssue384a() {
    inFunction(
            " var a, b;\n" +
            " if (f(b = true) || f(b = false))\n" +
            "   a = b;\n" +
            " else\n" +
            "   a = null;\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384b
  public void testIssue384b() {
    inFunction(
            " var a, b;\n" +
            " (f(b = true) || f(b = false)) ? (a = b) : (a = null);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384c
  public void testIssue384c() {
    inFunction(
            " var a, b;\n" +
            " (a ? f(b = true) : f(b = false)) && (a = b);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384d
  public void testIssue384d() {
    inFunction(
            " var a, b;\n" +
            " (f(b = true) || f(b = false)) && (a = b);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testForIn
  public void testForIn() {
    inFunction("var x = {}; for (var y in x) { y() }");
    inFunction("var x, y, z; x = {}; z = {}; for (y in x = z) { y() }",
               "var x, y, z;   ({}); z = {}; for (y in z)     { y() }");
    inFunction("var x, y, z; x = {}; z = {}; for (y[z=1] in z) { y() }",
               "var x, y, z;   ({}); z = {}; for (y[z=1] in z) { y() }");

    
    
    
    inFunction("var x, y, z; x = {}; z = {}; for (x in z) { x() }");
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

    
    test("function f(){ var a; for(; a < 2 ; a++) foo() }",
         "function f(){ for(var a; a < 2 ; a++) foo() }");
    testSame("function f(){ return; for(; a < 2 ; a++) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testForIn
  public void testForIn() {
    test("var a; for(a in b) foo()", "for (var a in b) foo()");
    testSame("a = 0; for(a in b) foo()");
    testSame("var a = 0; for(a in b) foo()");

    
    testSame("var a; a:for(a in b) foo()");
    testSame("var a; a:b:for(a in b) foo()");

    
    test("if(x){var a; for(a in b) foo()}",
         "if(x){for(var a in b) foo()}");

    
    testSame("init(); for(a in b) foo()");

    
    testSame("function f(){ return; for(a in b) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testInOperatorNotInsideFor
  public void testInOperatorNotInsideFor() {
    
    
    
    

    
    testSame("function f(){ var a; var i=\"length\" in a;" +
        "for(; a < 2 ; a++) foo() }");
    
    testSame("function f(){ var a; var i=(\"length\" in a);" +
        "for(; a < 2 ; a++) foo() }");
    
    
    test("function f(){" +
         "var b,a=0; for (var i=(\"length\" in b);a<2; a++) foo()}",
         "function f(){var b; var a=0;var i=(\"length\" in b);" +
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

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeObjectLiterals1
  public void testRewritePrototypeObjectLiterals1() throws Exception {
    test(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                       NoRewritePrototypeObjectLiteralsTestInput.CALL),
         "a.prototype={};" +
         "var JSCompiler_StaticMethods_foo=" +
         "function(JSCompiler_StaticMethods_foo$self){};" +
         "JSCompiler_StaticMethods_foo(o)");
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

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod2
  public void testRewriteImplementedMethod2() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod3
  public void testRewriteImplementedMethod3() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod4
  public void testRewriteImplementedMethod4() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethodInObj
  public void testRewriteImplementedMethodInObj() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {foo: function(args) {return args}};",
        "var o = new a;",
        "o.foo()");
    test(source,
        "function a(){}" +
        "a.prototype={};" +
        "var JSCompiler_StaticMethods_foo=" +
        "function(JSCompiler_StaticMethods_foo$self,args){return args};" +
        "var o=new a;" +
        "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet1
  public void testNoRewriteGet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return f}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet2
  public void testNoRewriteGet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return 1}};",
        "var o = new a;",
        "o.foo");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet1
  public void testNoRewriteSet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet2
  public void testNoRewriteSet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo = 1");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNotImplementedMethod
  public void testNoRewriteNotImplementedMethod() throws Exception {
    testSame(newlineJoin("function a(){}",
                         "var o = new a;",
                         "o.foo()"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testWrapper
  public void testWrapper() {
    testSame("(function() {})()");
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType1
  public void testOneType1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType2
  public void testOneType2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    String expected = "{a=[[Foo.prototype]]}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType3
  public void testOneType3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    String expected = "{a=[[Foo.prototype]]}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance2
  public void testPrototypeAndInstance2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "new Foo().a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes1
  public void testTwoTypes1() {
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes2
  public void testTwoTypes2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = {a: 0};"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = {Foo_prototype$a: 0};"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = {Bar_prototype$a: 0};"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes3
  public void testTwoTypes3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = { get Foo_prototype$a() {return  0},"
        + "                  set Foo_prototype$a(b) {} };\n"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = { get Bar_prototype$a() {return  0},"
        + "                  set Bar_prototype$a(b) {} };\n"
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType1
  public void testIgnoreUnknownType1() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{blah=[[Foo.prototype]]}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType2
  public void testIgnoreUnknownType2() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "\n"
        + "function Bar() {}\n"
        + "Bar.prototype.blah = 3;\n"
        + "\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "function Bar(){}Bar.prototype.blah=3;"
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
        + "Foo.function__new_Foo___undefined$a = 0;"
        + "Bar.function__new_Bar___undefined$a = 0;";

    testSets(false, js, output,
        "{a=[[function (new:Bar): undefined]," +
        " [function (new:Foo): undefined]]}");
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
        + " "
        + "var Foo = function() {};\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {};\n"
        + "Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
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
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    String ttOutput = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    testSets(false, js, output, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
    testSets(true, js, ttOutput, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects
  public void testEnumOfObjects() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter()\n"
        + "};\n"
        + "Enum.A.format();\n";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.Formatter_prototype$format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.Unrelated_prototype$format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter()\n"
        + "};\n"
        + "Enum.A.Formatter_prototype$format();\n";
    testSets(false, js, output,
        "{format=[[Formatter.prototype], [Unrelated.prototype]]}");

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects2
  public void testEnumOfObjects2() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = window.toString() ? Enum.A : Enum.B;\n"
        + "  formatter.format();\n"
        + "}";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = window.toString() ? Enum.A : Enum.B;\n"
        + "  formatter.format();\n"
        + "}";
    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects3
  public void testEnumOfObjects3() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + " var SubEnum = {\n"
        + "  C: Enum.A\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = SubEnum.C\n"
        + "  formatter.format();\n"
        + "}";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.Formatter_prototype$format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.Unrelated_prototype$format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + " var SubEnum = {\n"
        + "  C: Enum.A\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = SubEnum.C\n"
        + "  formatter.Formatter_prototype$format();\n"
        + "}";
    testSets(false, js, output,
        "{format=[[Formatter.prototype], [Unrelated.prototype]]}");
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
        + "function foo(foo$$1) {\n"
        + "  var x = foo$$1.Bar_prototype$a;\n"
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralReflected
  public void testObjectLiteralReflected() {
    String js = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "goog.reflect.object(F, {foo: 5});";
    String result = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "goog.reflect.object(F, {F_prototype$foo: 5});";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralLends
  public void testObjectLiteralLends() {
    String js = ""
        + "var mixin = function(x) { return x; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "mixin( ({foo: 5}));";
    String result = ""
        + "var mixin = function(x) { return x; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "mixin( ({F_prototype$foo: 5}));";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
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
        + "\n"
        + "Foo.prototype.f = function() {"
        + "  Foo.superClass_.f();"
        + "};\n"
        + " function Bar() {}\n"
        + "goog.inherits(Bar, Foo);\n"
        + "\n"
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoInterfacesWithSomeInheritance
  public void testTwoInterfacesWithSomeInheritance() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + "\n"
        + "function Bar() {};\n"
        + "Bar.prototype.a;\n"
        + "\n"
        + "var B = new Bar;"
        + "B.a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype, I2.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype], [Foo.prototype], "
                       + "[I.prototype], [I2.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInvalidatingInterface
  public void testInvalidatingInterface() {
    String js = ""
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + " function Bar() {}\n"
        + "\n"
        + "var i = new Bar;\n" 
        + ""
        + "function Foo() {};\n"
        + "\n"
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0;"
        + " function I() {};\n"
        + "I.prototype.a;\n";
    testSets(false, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING);
    testSets(true, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMultipleInterfaces
  public void testMultipleInterfaces() {
    String js = ""
        + " function I() {};\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + ""
        + "function Foo() {};\n"
        + ""
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I2.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I2.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceWithSupertypeImplementor
  public void testInterfaceWithSupertypeImplementor() {
    String js = ""
        + " function C() {}\n"
        + "C.prototype.foo = function() {};\n"
        + " function A (){}\n"
        + "A.prototype.foo = function() {};\n"
        + "\n"
        + "function B() {}\n"
        + " var b = new B();\n"
        + "b.foo();\n";
    testSets(false, js, "{foo=[[A.prototype, C.prototype]]}");
    testSets(true, js, "{foo=[[A.prototype], [C.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSuperInterface
  public void testSuperInterface() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function I2() {};\n"
        + ""
        + "function Foo() {};\n"
        + "\n"
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceUnionWithCtor
  public void testInterfaceUnionWithCtor() {
    String js = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;"
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, js, js,
        "{addEventListener=[[C.prototype, I.prototype, Impl.prototype]]}");

    
    
    String tightenedOutput = ""
        + "function I() {};\n"
        + "I.prototype.I_prototype$addEventListener;\n"
        + "function Impl() {};\n"
        + "Impl.prototype.C_prototype$addEventListener;"
        + "function C() {};\n"
        + "C.prototype.C_prototype$addEventListener;"
        + ""
        + "function f(x) { x.C_prototype$addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(true, js, tightenedOutput,
        "{addEventListener=[[C.prototype, Impl.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExternInterfaceUnionWithCtor
  public void testExternInterfaceUnionWithCtor() {
    String externs = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;";

    String js = ""
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMismatchInvalidation
  public void testMismatchInvalidation() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Bar;\n"
        + "F.a = 0;";

    testSets(false, "", js, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
    testSets(true, "", js, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testBadCast
  public void testBadCast() {
    String js = " function Foo() {};\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {};\n"
        + "Bar.prototype.a = 0;\n"
        + "var a =  (new Bar);\n"
        + "a.a = 4;";
    testSets(false, "", js, js, "{}",
             TypeValidator.INVALID_CAST,
             "invalid cast - must be a subtype or supertype\n"
             + "from: Bar\n"
             + "to  : Foo");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testDeterministicNaming
  public void testDeterministicNaming() {
    String js =
        "function A() {}\n"
        + "A.prototype.f = function() {return 'a';};\n"
        + "function B() {}\n"
        + "B.prototype.f = function() {return 'b';};\n"
        + "function C() {}\n"
        + "C.prototype.f = function() {return 'c';};\n"
        + "var ab = 1 ? new B : new A;\n"
        + "var n = ab.f();\n";

    String output =
        "function A() {}\n"
        + "A.prototype.A_prototype$f = function() { return'a'; };\n"
        + "function B() {}\n"
        + "B.prototype.A_prototype$f = function() { return'b'; };\n"
        + "function C() {}\n"
        + "C.prototype.C_prototype$f = function() { return'c'; };\n"
        + "var ab = 1 ? new B : new A; var n = ab.A_prototype$f();\n";

    for (int i = 0; i < 5; i++) {
      testSets(false, js, output,
          "{f=[[A.prototype, B.prototype], [C.prototype]]}");

      testSets(true, js, output,
          "{f=[[A.prototype, B.prototype], [C.prototype]]}");
    }
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteral
  public void testObjectLiteral() {
    String js = " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + "var F = ({ a: 'a' });\n";

    String output = "function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$a;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "var F = { Foo_prototype$a: 'a' };\n";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCustomInherits
  public void testCustomInherits() {
    String js = "Object.prototype.inheritsFrom = function(shuper) {\n" +
        "  \n" +
        "  function Inheriter() { }\n" +
        "  Inheriter.prototype = shuper.prototype;\n" +
        "  this.prototype = new Inheriter();\n" +
        "  this.superConstructor = shuper;\n" +
        "};\n" +
        "function Foo(var1, var2, strength) {\n" +
        "  Foo.superConstructor.call(this, strength);\n" +
        "}" +
        "Foo.inheritsFrom(Object);";

    String externs = "" +
        "function Function(var_args) {}" +
        "Function.prototype.call = function(var_args) {};";

    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionStaticProperty
  public void testSkipNativeFunctionStaticProperty() {
    String js = ""
      + "\n"
      + "function addSingletonGetter(ctor) { ctor.a; }\n"
      + " function Foo() {}\n"
      + "Foo.a = 0;"
      + " function Bar() {}\n"
      + "Bar.a = 0;";

    String output = ""
        + "function addSingletonGetter(ctor){ctor.a}"
        + "function Foo(){}"
        + "Foo.a=0;"
        + "function Bar(){}"
        + "Bar.a=0";

    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testErrorOnProtectedProperty
  public void testErrorOnProtectedProperty() {
    test("function addSingletonGetter(foo) { foo.foobar = 'a'; };", null,
         DisambiguateProperties.Warnings.INVALIDATION);
    assertTrue(getLastCompiler().getErrors()[0].toString().contains("foobar"));
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMismatchForbiddenInvalidation
  public void testMismatchForbiddenInvalidation() {
    test(" function F() {}" +
         " F.prototype.foobar = 3;" +
         " function g() { return new F(); }",
         null,
         DisambiguateProperties.Warnings.INVALIDATION);
    assertTrue(getLastCompiler().getErrors()[0].toString()
        .contains("Consider fixing errors"));
  }

// com.google.javascript.jscomp.ExpandJqueryAliasesTest::testJqueryFnAliasExpansion
  public void testJqueryFnAliasExpansion() {
    String setupCode = "var jQuery={};jQuery.fn=jQuery.prototype;";

    testSame(setupCode);

    test(setupCode + "jQuery.fn.foo='bar';",
        setupCode + "jQuery.prototype.foo='bar';");

    test(setupCode + "jQuerySub.fn.foo='bar';",
        setupCode + "jQuerySub.prototype.foo='bar';");
  }

// com.google.javascript.jscomp.ExpandJqueryAliasesTest::testJqueryExtendExpansion
  public void testJqueryExtendExpansion() {
    String setupCode = "var jQuery={},obj2={};";

    
    testSame(setupCode + "jQuery.extend()");

    
    
    testSame(setupCode + "jQuery.extend({})");

    
    test(setupCode + "jQuery.extend({a:'test'})",
        setupCode + "{jQuery.a = 'test';}");

    
    test(setupCode + "jQuery.fn=jQuery.prototype;" +
        "jQuery.fn.extend({a:'test', b:'test2'});",
        setupCode + "jQuery.fn=jQuery.prototype;" +
        "{jQuery.prototype.a = 'test'; jQuery.prototype.b = 'test2';}");

    
    test(setupCode + "jQuery.extend(obj2, {a:'test', b:'test2'});",
        setupCode + "{obj2=obj2||{}; obj2.a='test'; obj2.b='test2';}");

    
    
    test(setupCode + "jQuery.extend(jQuery,{a:'test', b:'test2'});",
        setupCode + "{jQuery = jQuery || {}; jQuery.a = 'test';" +
        "jQuery.b = 'test2';}");

    
    testSame(setupCode+"obj2.meth=function() { return { a:{} }; };" +
        "jQuery.extend(obj2.meth().a, {a: 'test'});");
  }

// com.google.javascript.jscomp.ExpandJqueryAliasesTest::testJqueryExpandedEachExpansion
  public void testJqueryExpandedEachExpansion() {
    String setupCode = "var jQuery={};" +
        "jQuery.expandedEach=function(vals, callback){};";

    testSame(setupCode);

    
    test(setupCode + "jQuery.expandedEach({'a': 1, 'b': 2, 'c': 8}," +
        "function(key, val) { var a = key; jQuery[key] = val; });",
        setupCode + "(function(){ var a = 'a'; jQuery.a = 1 })();" +
        "(function(){ var a = 'b'; jQuery.b = 2 })();" +
        "(function(){ var a = 'c'; jQuery.c = 8 })();");

    
    
    
    
    test(setupCode + "jQuery.expandedEach(['a', 'b', 'c']," +
        "function(key, val){ jQuery[val] = key; });",
        setupCode + "(function(){ jQuery.a = 0; })();" +
        "(function(){ jQuery.b = 1; })();" +
        "(function(){ jQuery.c = 2 })();");

    
    test(setupCode + "jQuery.expandedEach({'a': 1, 'b': 2, 'c': 8}," +
        "function(key, val) { var a = key; jQuery[key] = this; });",
        setupCode + "(function(){ var a = 'a'; jQuery.a = 1 })();" +
        "(function(){ var a = 'b'; jQuery.b = 2 })();" +
        "(function(){ var a = 'c'; jQuery.c = 8 })();");

    
    test(setupCode + "jQuery.expandedEach(['a', 'b', 'c']," +
        "function(key, val){ jQuery[this] = key; });",
        setupCode + "(function(){ jQuery.a = 0; })();" +
        "(function(){ jQuery.b = 1; })();" +
        "(function(){ jQuery.c = 2 })();");

    
    test(setupCode + "jQuery.expandedEach(['a'], function(key,val) {" +
        "jQuery[val] = key; (function(key) { jQuery[key] = 1;})('test'); })",
        setupCode + "(function(){ jQuery.a = 0;" +
         "(function(key){ jQuery[key] = 1})('test') })()");

    
    test(setupCode + "jQuery.expandedEach(['a'], function(key,val) {" +
        "jQuery[val] = key; (function(key) { var val = 2;" +
        "jQuery[key] = val;})('test');})",
        setupCode + "(function(){" +
        "jQuery.a=0;" +
        "(function(key){var val = 2; jQuery[key] = val;})('test')})()");

    
    test(setupCode + "jQuery.expandedEach(['a'], function(key,val) {" +
        "jQuery[val] = key; (function(key1) {" +
        "function key() {}; key();" +
        "})('test');})",
        setupCode + "(function(){" +
        "jQuery.a=0;(function(key1) {" +
        "function key() {}; key(); })('test')})()");

    
    test(setupCode + "alert(jQuery.expandedEach(['a']," +
        "function(key,val) { jQuery[val] = key;})[0])",
        setupCode + "alert((function(){" +
        "(function(){ jQuery.a = 0;})(); return ['a']})()[0]);");

    
    testSame(setupCode + "var a = ['a'];" +
        "jQuery.expandedEach(a, function(key,val){ jQuery[key]=val; })",
        INVALID_LIT_ERROR);

    
    testSame(setupCode + "var obj2={};" +
        "jQuery.expandedEach(['foo','bar'], function(i, name) {" +
        "obj2[ '[object ' + name + ']' ] = 'a';});", NAME_ERROR, true);

    
    testSame(setupCode + "var obj2={};" +
        "jQuery.expandedEach(['foo','bar'], function(i, name) {" +
        "obj2[i] = 1;});", USELESS_EACH_ERROR, false);
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitationTypes
  public void testExprExploitationTypes() {
    test("a = true; b = true",
         "b = a = true");
    test("a = !0; b = !0",
         "b = a = !0");
    test("a = !1; b = !1",
         "b = a = !1");
    test("a = void 0; b = void 0",
         "b = a = void 0");
    test("a = -Infinity; b = -Infinity",
         "b = a = -Infinity");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitationTypes2
  public void testExprExploitationTypes2() {
    test("a = !0; b = !0",
         "b = a = !0");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitation
  public void testExprExploitation() {
    test("a = null; b = null; var c = b",
         "var c = b = a = null");
    test("a = null; b = null",
         "b = a = null");
    test("a = undefined; b = undefined",
         "b = a = undefined");
    test("a = 0; b = 0", "b=a=0");
    test("a = 'foo'; b = 'foo'",
         "b = a = \"foo\"");
    test("a = c; b = c", "b=a=c");

    testSame("a = 0; b = 1");
    testSame("a = \"foo\"; b = \"foox\"");

    test("a = null; a && b;", "(a = null)&&b");
    test("a = null; a || b;", "(a = null)||b");

    test("a = null; a ? b : c;", "(a = null) ? b : c");

    test("a = null; this.foo = null;",
         "this.foo = a = null");
    test("function f(){ a = null; return null; }",
         "function f(){return a = null}");

    test("a = true; if (a) { foo(); }",
         "if (a = true) { foo() }");
    test("a = true; if (a && a) { foo(); }",
         "if ((a = true) && a) { foo() }");
    test("a = false; if (a) { foo(); }",
         "if (a = false) { foo() }");

    test("a = !0; if (a) { foo(); }",
        "if (a = !0) { foo() }");
    test("a = !0; if (a && a) { foo(); }",
        "if ((a = !0) && a) { foo() }");
    test("a = !1; if (a) { foo(); }",
        "if (a = !1) { foo() }");

    testSame("a = this.foo; a();");
    test("a = b; b = a;",
         "b = a = b");
    testSame("a = b; a.c = a");
    test("this.foo = null; this.bar = null;",
         "this.bar = this.foo = null");
    test("this.foo = null; this.bar = null; this.baz = this.bar",
         "this.baz = this.bar = this.foo = null");
    test("this.foo = null; a = null;",
         "a = this.foo = null");
    test("this.foo = null; a = this.foo;",
         "a = this.foo = null");
    test("a.b.c=null; a=null;",
         "a = a.b.c = null");
    testSame("a = null; a.b.c = null");
    test("(a=b).c = null; this.b = null;",
         "this.b = (a=b).c = null");
    testSame("if(x) a = null; else b = a");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testNestedExprExploitation
  public void testNestedExprExploitation() {
    test("this.foo = null; this.bar = null; this.baz = null;",
         "this.baz = this.bar = this.foo = null");

    test("a = 3; this.foo = a; this.bar = a; this.baz = 3;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = this.foo; this.baz = a;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = 3; this.baz = this.foo;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.baz = a = this.bar = a = this.foo = a = 3");

    test("a = 4; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 4; a = this.bar = a = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 4; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 3; a = 4; a = this.bar = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 4; this.baz = this.foo;",
         "this.bar = a = this.foo = a = 3; a = 4; this.baz = this.foo");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBug1840071
  public void testBug1840071() {
    
    
    test("a.b = a.x; if (a.x) {}", "if (a.b = a.x) {}");
    testSame("a.b = a.x; if (a.b) {}");
    test("a.b = a.c = a.x; if (a.x) {}", "if (a.b = a.c = a.x) {}");
    testSame("a.b = a.c = a.x; if (a.c) {}");
    testSame("a.b = a.c = a.x; if (a.b) {}");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBug2072343
  public void testBug2072343() {
    testSame("a = a.x;a = a.x");
    testSame("a = a.x;b = a.x");
    test("b = a.x;a = a.x", "a = b = a.x");
    testSame("a.x = a;a = a.x");
    testSame("a.b = a.b.x;a.b = a.b.x");
    testSame("a.y = a.y.x;b = a.y;c = a.y.x");
    test("a = a.x;b = a;c = a.x", "b = a = a.x;c = a.x");
    test("b = a.x;a = b;c = a.x", "a = b = a.x;c = a.x");
 }

// com.google.javascript.jscomp.ExploitAssignsTest::testBadCollapseIntoCall
  public void testBadCollapseIntoCall() {
    
    
    testSame("this.foo = function() {}; this.foo();");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBadCollapse
  public void testBadCollapse() {
    testSame("this.$e$ = []; this.$b$ = null;");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionsAreExported
  public void testFunctionsAreExported() {
    test(TEST_FUNCTIONS_WITH_NAMES,
        "function Foo(arg){}; "
        + "function setUp(arg3){} google_exportSymbol(\"setUp\",setUp);; "
        + "function tearDown(arg,arg2) {} "
        + "google_exportSymbol(\"tearDown\",tearDown);; "
        + "function testBar(arg){} google_exportSymbol(\"testBar\",testBar)"
    );
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testBasicTestFunctionsAreExported
  public void testBasicTestFunctionsAreExported() {
    test("function Foo() {function testA() {}}",
         "function Foo() {function testA(){}}");
    test("function setUp() {}",
         "function setUp(){} google_exportSymbol('setUp',setUp)");
    test("function setUpPage() {}",
         "function setUpPage(){} google_exportSymbol('setUpPage',setUpPage)");
    test("function tearDown() {}",
         "function tearDown(){} google_exportSymbol('tearDown',tearDown)");
    test("function tearDownPage() {}",
         "function tearDownPage(){} google_exportSymbol('tearDownPage'," +
         "tearDownPage)");
    test("function testBar() { function testB() {}}",
         "function testBar(){function testB(){}}"
             + "google_exportSymbol('testBar',testBar)");
    testSame("var testCase = {}; testCase.setUpPage = function() {}");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionExpressionsAreExported
  public void testFunctionExpressionsAreExported() {
    test("var Foo = function() {var testA = function() {}}",
         "var Foo = function() {var testA = function() {}}");
    test("var setUp = function() {}",
         "var setUp = function() {}; " +
         "google_exportSymbol('setUp',setUp)");
    test("var setUpPage = function() {}",
         "var setUpPage = function() {}; " +
         "google_exportSymbol('setUpPage',setUpPage)");
    test("var tearDown = function() {}",
         "var tearDown = function() {}; " +
         "google_exportSymbol('tearDown',tearDown)");
    test("var tearDownPage = function() {}",
         "var tearDownPage = function() {}; " +
         "google_exportSymbol('tearDownPage', tearDownPage)");
    test("var testBar = function() { var testB = function() {}}",
         "var testBar = function(){ var testB = function() {}}; " +
         "google_exportSymbol('testBar',testBar)");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionAssignmentsAreExported
  public void testFunctionAssignmentsAreExported() {
    test("Foo = {}; Foo.prototype.bar = function() {};",
         "Foo = {}; Foo.prototype.bar = function() {};");

    test("Foo = {}; Foo.prototype.setUpPage = function() {};",
         "Foo = {}; Foo.prototype.setUpPage = function() {};"
         + "google_exportProperty(Foo.prototype, 'setUpPage', "
         + "Foo.prototype.setUpPage);");

    test("Foo = {}; Foo.prototype.testBar = function() {};",
         "Foo = {}; Foo.prototype.testBar = function() {};"
         + "google_exportProperty(Foo.prototype, 'testBar', "
         + "Foo.prototype.testBar);");

    test("Foo = {}; Foo.prototype.testBar = function() "
         + "{ var testBaz = function() {}};",
         "Foo = {}; Foo.prototype.testBar = function() "
         + "{ var testBaz = function() {}};"
         + "google_exportProperty(Foo.prototype, 'testBar', "
         + "Foo.prototype.testBar);");

    test("Foo = {}; Foo.baz.prototype.testBar = function() "
         + "{ var testBaz = function() {}};",
         "Foo = {}; Foo.baz.prototype.testBar = function() "
         + "{ var testBaz = function() {}};"
         + "google_exportProperty(Foo.baz.prototype, 'testBar', "
         + "Foo.baz.prototype.testBar);");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbol
  public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolDefinedInVar
  public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportProperty
  public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a;\n" +
                    "a.b;\n" +
                    "\n" +
                    "a.b.cprop = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple
  public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a;\n" +
                    "\n" +
                    "a.b = function(p1) {\n};\n" +
                    "\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "a.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple2
  public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "\n" +
                    "var hello = {};\n" +
                    "hello.b;\n" +
                    "\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple3
  public void testExportMultiple3() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('prefix', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);",

                    "\n" +
                    "var prefix = function(p1) {\n};\n" +
                    "\n" +
                    "prefix.c = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol
  public void testExportNonStaticSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = {}; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "var foobar;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol2
  public void testExportNonStaticSymbol2() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = null; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c())",
                    "var foobar;\n");
  }
