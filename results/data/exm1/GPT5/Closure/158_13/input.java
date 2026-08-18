// buggy code
  final protected void setRunOptions(CompilerOptions options)
      throws FlagUsageException, IOException {
    DiagnosticGroups diagnosticGroups = getDiagnosticGroups();

    diagnosticGroups.setWarningLevels(
        options, config.jscompError, CheckLevel.ERROR);
    diagnosticGroups.setWarningLevels(
        options, config.jscompWarning, CheckLevel.WARNING);
    diagnosticGroups.setWarningLevels(
        options, config.jscompOff, CheckLevel.OFF);

    createDefineOrTweakReplacements(config.define, options, false);

    options.setTweakProcessing(config.tweakProcessing);
    createDefineOrTweakReplacements(config.tweak, options, true);

    options.manageClosureDependencies = config.manageClosureDependencies;
    if (config.closureEntryPoints.size() > 0) {
      options.setManageClosureDependencies(config.closureEntryPoints);
    }
    options.devMode = config.jscompDevMode;
    options.setCodingConvention(config.codingConvention);
    options.setSummaryDetailLevel(config.summaryDetailLevel);

    outputCharset = options.outputCharset = getOutputCharset();
    inputCharset = getInputCharset();

    if (config.jsOutputFile.length() > 0) {
      options.jsOutputFile = config.jsOutputFile;
    }

    if (config.createSourceMap.length() > 0) {
      options.sourceMapOutputPath = config.createSourceMap;
    }
    options.sourceMapDetailLevel = config.sourceMapDetailLevel;
    options.sourceMapFormat = config.sourceMapFormat;

    if (!config.variableMapInputFile.equals("")) {
      options.inputVariableMapSerialized =
          VariableMap.load(config.variableMapInputFile).toBytes();
    }

    if (!config.propertyMapInputFile.equals("")) {
      options.inputPropertyMapSerialized =
          VariableMap.load(config.propertyMapInputFile).toBytes();
    }

    if (config.languageIn.length() > 0) {
      if (config.languageIn.equals("ECMASCRIPT5_STRICT") ||
          config.languageIn.equals("ES5_STRICT")) {
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
      } else if (config.languageIn.equals("ECMASCRIPT5") ||
          config.languageIn.equals("ES5")) {
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
      } else if (config.languageIn.equals("ECMASCRIPT3") ||
                 config.languageIn.equals("ES3")) {
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
      } else {
        throw new FlagUsageException("Unknown language `" + config.languageIn +
                                     "' specified.");
      }
    }

    options.acceptConstKeyword = config.acceptConstKeyword;
  }

    CommandLineConfig setJscompError(List<String> jscompError) {
      this.jscompError.clear();
      this.jscompError.addAll(jscompError);
      return this;
    }

    CommandLineConfig setJscompWarning(List<String> jscompWarning) {
      this.jscompWarning.clear();
      this.jscompWarning.addAll(jscompWarning);
      return this;
    }

    CommandLineConfig setJscompOff(List<String> jscompOff) {
      this.jscompOff.clear();
      this.jscompOff.addAll(jscompOff);
      return this;
    }

    CommandLineConfig setLanguageIn(String languageIn) {
      this.languageIn = languageIn;
      return this;
    }

        usage = "An output wrapper for a javascript module (optional). "
        + "The format is <name>:<wrapper>. The module name must correspond "
        + "with a module specified using --module. The wrapper must "
        + "contain %s as the code placeholder")
    private List<String> module_wrapper = Lists.newArrayList();

      public String getDefaultMetaVariable() {
        return null;
      }

  private void processFlagFile(PrintStream err)
            throws CmdLineException, IOException {
    List<String> argsInFile = Lists.newArrayList();
    File flagFileInput = new File(flags.flag_file);
    StringTokenizer tokenizer = new StringTokenizer(
        Files.toString(flagFileInput, Charset.defaultCharset()));

    while (tokenizer.hasMoreTokens()) {
        argsInFile.add(tokenizer.nextToken());
    }

    flags.flag_file = "";
    List<String> processedFileArgs
        = processArgs(argsInFile.toArray(new String[] {}));
    CmdLineParser parserFileArgs = new CmdLineParser(flags);
    parserFileArgs.parseArgument(processedFileArgs.toArray(new String[] {}));

    // Currently we are not supporting this (prevent direct/indirect loops)
    if (!flags.flag_file.equals("")) {
      err.println("ERROR - Arguments in the file cannot contain "
          + "--flagfile option.");
      isConfigValid = false;
    }
  }

  private void initConfigFromFlags(String[] args, PrintStream err) {

    List<String> processedArgs = processArgs(args);

    CmdLineParser parser = new CmdLineParser(flags);
    isConfigValid = true;
    try {
      parser.parseArgument(processedArgs.toArray(new String[] {}));
      // For contains --flagfile flag
      if (!flags.flag_file.equals("")) {
        processFlagFile(err);
      }
    } catch (CmdLineException e) {
      err.println(e.getMessage());
      isConfigValid = false;
    } catch (IOException ioErr) {
      err.println("ERROR - " + flags.flag_file + " read error.");
      isConfigValid = false;
    }

    if (flags.version) {
      ResourceBundle config = ResourceBundle.getBundle(configResource);
      err.println(
          "Closure Compiler (http://code.google.com/closure/compiler)\n" +
          "Version: " + config.getString("compiler.version") + "\n" +
          "Built on: " + config.getString("compiler.date"));
      err.flush();
    }

    if (!isConfigValid || flags.display_help) {
      isConfigValid = false;
      parser.printUsage(err);
    } else {
      getCommandLineConfig()
          .setPrintTree(flags.print_tree)
          .setComputePhaseOrdering(flags.compute_phase_ordering)
          .setPrintAst(flags.print_ast)
          .setPrintPassGraph(flags.print_pass_graph)
          .setJscompDevMode(flags.jscomp_dev_mode)
          .setLoggingLevel(flags.logging_level)
          .setExterns(flags.externs)
          .setJs(flags.js)
          .setJsOutputFile(flags.js_output_file)
          .setModule(flags.module)
          .setVariableMapInputFile(flags.variable_map_input_file)
          .setPropertyMapInputFile(flags.property_map_input_file)
          .setVariableMapOutputFile(flags.variable_map_output_file)
          .setCreateNameMapFiles(flags.create_name_map_files)
          .setPropertyMapOutputFile(flags.property_map_output_file)
          .setCodingConvention(flags.third_party ?
               new DefaultCodingConvention() :
               new ClosureCodingConvention())
          .setSummaryDetailLevel(flags.summary_detail_level)
          .setOutputWrapper(flags.output_wrapper)
          .setModuleWrapper(flags.module_wrapper)
          .setModuleOutputPathPrefix(flags.module_output_path_prefix)
          .setCreateSourceMap(flags.create_source_map)
          .setJscompError(flags.jscomp_error)
          .setJscompWarning(flags.jscomp_warning)
          .setJscompOff(flags.jscomp_off)
          .setDefine(flags.define)
          .setCharset(flags.charset)
          .setManageClosureDependencies(flags.manage_closure_dependencies)
          .setClosureEntryPoints(flags.closure_entry_point)
          .setOutputManifest(flags.output_manifest)
          .setAcceptConstKeyword(flags.accept_const_keyword)
          .setLanguageIn(flags.language_in);
    }
  }

  void setWarningLevels(CompilerOptions options,
      java.util.List<String> diagnosticGroups, CheckLevel level) {
    for (String name : diagnosticGroups) {
    DiagnosticGroup group = forName(name);
    Preconditions.checkNotNull(group, "No warning class for name: " + name);
    options.setWarningLevel(group, level);
    }
  }

// relevant test
// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects10
  public void testLocalizedSideEffects10() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "A.prototype.g = function() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.g(); return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects11
  public void testLocalizedSideEffects11() throws Exception {
    
    checkMarkedCalls(
        " function A() {}" +
        "A.prototype.update = function() { this.x = 1; };" +
        " function B() { " +
        "  this.a_ = new A();" +
        "}" +
        "B.prototype.updateA = function() {" +
        "  var b = this.a_;" +
        "  b.update();" +
        "};" +
        "var x = new B();" +
        "x.updateA();",
        ImmutableList.of("A", "B"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrG
  public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHook
  public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionForGorH
  public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGWithSideEffects
  public void testCallFunctionFOrGWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "function i(){ (g || f)() }\n" +
        "function j(){ (f || f)() }\n" +
        "function k(){ (g || g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g || g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHookWithSideEffects
  public void testCallFunctionFOrGViaHookWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "function i(){ (false ? g : f)() }\n" +
        "function j(){ (false ? f : f)() }\n" +
        "function k(){ (false ? g : g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g : g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallRegExpWithSideEffects
  public void testCallRegExpWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function k(){(/a/).exec('')}\n" +
        "k()";

    regExpHaveSideEffects = true;
    checkMarkedCalls(source, ImmutableList.<String>of());
    regExpHaveSideEffects = false;
    checkMarkedCalls(source, ImmutableList.<String>of(
        "REGEXP STRING exec", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction1
  public void testAnonymousFunction1() throws Exception {
    String source = "(function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "FUNCTION"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction2
  public void testAnonymousFunction2() throws Exception {
    String source = "(Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction3
  public void testAnonymousFunction3() throws Exception {
    String source = "var a = (Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction4
  public void testAnonymousFunction4() throws Exception {
    String source = "var a = (Error || function (){});" +
                    "a();";

    
    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testFunction
  public void testFunction() {
    String g = "function g(){}";
    String fAndG = "function f(){" + g + "}";
    String js = "var h=" + fAndG + ";h()";

    FunctionInformationMap.Builder expected =
        FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("testcode")
        .setLineNumber(1)
        .setModuleName("")
        .setSize(g.length())
        .setName("f::g")
        .setCompiledSource(g).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("testcode")
        .setLineNumber(1)
        .setModuleName("")
        .setSize(fAndG.length())
        .setName("f")
        .setCompiledSource(fAndG).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("")
        .setCompiledSource(js + ";").build());

    test(js, expected.build());
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testModule
  public void testModule() {
    String g = "function g(){}";
    String fAndG = "function f(){" + g + "}";
    String m0_js = "var h=" + fAndG + ";h()";
    String sum = "function(a,b){return a+b}";
    String m1_js = "var x=" + sum + "(1,2)";

    FunctionInformationMap.Builder expected =
        FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(g.length())
        .setName("f::g")
        .setCompiledSource(g).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(fAndG.length())
        .setName("f")
        .setCompiledSource(fAndG).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(2)
        .setSourceName("i1")
        .setLineNumber(1)
        .setModuleName("m1")
        .setSize(sum.length())
        .setName("<anonymous>")
        .setCompiledSource(sum).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m0")
        .setCompiledSource(m0_js + ";").build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m1")
        .setCompiledSource(m1_js + ";").build());

    test(CompilerTestCase.createModules(m0_js, m1_js), expected.build());
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testMotionPreservesOriginalSourceName
  public void testMotionPreservesOriginalSourceName() {
    String f = "function f(){}";
    String g = "function g(){}";

    String m0_before = f + g;
    String m1_before = "";

    JSModule[] modules = CompilerTestCase.createModules(m0_before, m1_before);
    Compiler compiler = compilerFor(modules);
    Node root = root(compiler);
    Node externsRoot = externs(root);
    Node mainRoot = main(root);

    String m0_after = f;
    String m1_after = g;
    Node nodeG = mainRoot.getFirstChild().getLastChild();
    mainRoot.getFirstChild().removeChild(nodeG);
    mainRoot.getLastChild().addChildrenToBack(nodeG.cloneTree());

    FunctionInformationMap.Builder expected =
      FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(g.length())
        .setName("f")
        .setCompiledSource(f).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m1")
        .setSize(g.length())
        .setName("g")
        .setCompiledSource(g).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m0")
        .setCompiledSource(m0_after + ";").build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m1")
        .setCompiledSource(m1_after + ";").build());

    test(compiler, externsRoot, mainRoot, expected.build());
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatch
  public void testRemoveTryCatch() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "var b;var a=1");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}",
         "var d;var c;var a=1;var b=2");
    test("try{var a=1;var b=2}catch(ex){}",
         "var a=1;var b=2");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryFinally
  public void testRemoveTryFinally() {
    test("try{var a=1;}finally{var c=3;}",
         "var a=1;var c=3");
    test("try{var a=1;var b=2}finally{var e=5;var f=6;}",
         "var a=1;var b=2;var e=5;var f=6");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatchFinally
  public void testRemoveTryCatchFinally() {
    test("try{var a=1;}catch(ex){var b=2;}finally{var c=3;}",
         "var b;var a=1;var c=3");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}finally{var e=5;" +
         "var f=6;}",
         "var d;var c;var a=1;var b=2;var e=5;var f=6");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveTryBlockContainingReturnStatement
  public void testPreserveTryBlockContainingReturnStatement() {
    testSame("function f(){var a;try{a=1;return}finally{a=2}}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveAnnotatedTryBlock
  public void testPreserveAnnotatedTryBlock() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "try{var a=1}catch(ex){var b=2}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryFinally
  public void testIfTryFinally() {
    test("if(x)try{y}finally{z}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryCatch
  public void testIfTryCatch() {
    test("if(x)try{y;z}catch(e){}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
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

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing
  public void testAliasing() {
    
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
           "var x=new e;x.alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "var x = new e; x.alias1()");
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

    test("var b=function(c){return arguments};b(1,2);b(3,4)",
         "var b=function(){return arguments};b(1,2);b(3,4)");

    
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

// com.google.javascript.jscomp.RenameLabelsTest::testRenameInFunction
  public void testRenameInFunction() {
    test("function x(){ Foo:a(); }",
         "function x(){ a(); }");
    test("function x(){ Foo:{ a(); break Foo; } }",
         "function x(){ a:{ a(); break a; } }");
    test("function x() { " +
            "Foo:{ " +
              "function goo() {" +
                "Foo: {" +
                  "a(); " +
                  "break Foo; " +
                "}" +
              "}" +
            "}" +
          "}",
          "function x(){function goo(){a:{ a(); break a; }}}");
    test("function x() { " +
          "Foo:{ " +
            "function goo() {" +
              "Foo: {" +
                "a(); " +
                "break Foo; " +
              "}" +
            "}" +
            "break Foo;" +
          "}" +
        "}",
        "function x(){a:{function goo(){a:{ a(); break a; }} break a;}}");
  }

// com.google.javascript.jscomp.RenameLabelsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("Foo:{a();}",
         "a();");
    test("Foo:{a(); break Foo;}",
         "a:{a(); break a;}");
    test("Foo:{Goo:a(); break Foo;}",
         "a:{a(); break a;}");
    test("Foo:{Goo:while(1){a(); continue Goo; break Foo;}}",
         "a:{b:while(1){a(); continue b;break a;}}");
    test("Foo:Goo:while(1){a(); continue Goo; break Foo;}",
         "a:b:while(1){a(); continue b;break a;}");

    test("Foo:Bar:X:{ break Bar; }",
         "a:{ break a; }");
    test("Foo:Bar:X:{ break Bar; break X; }",
         "a:b:{ break a; break b;}");
    test("Foo:Bar:X:{ break Bar; break Foo; }",
         "a:b:{ break b; break a;}");

    test("Foo:while (1){a(); break;}",
         "while (1){a(); break;}");

    
    test("Foo:{a(); while (1) break;}",
         "a(); while (1) break;");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameSimple
  public void testRenameSimple() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function Foo(a, b) {return a;} Foo();");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameGlobals
  public void testRenameGlobals() {
    testSame("var Foo; var Bar, y; function x() { Bar++; }");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameLocals
  public void testRenameLocals() {
    test("(function (v1, v2) {}); (function (v3, v4) {});",
         "(function (a, b) {}); (function (a, b) {});");
    test("function f1(v1, v2) {}; function f2(v3, v4) {};",
         "function f1(a, b) {}; function f2(a, b) {};");

  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameLocalsClashingWithGlobals
  public void testRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameNested
  public void testRenameNested() {
    test("function f1(v1, v2) { (function(v3, v4) {}) }",
         "function f1(a, b) { (function(c, d) {}) }");
    test("function f1(v1, v2) { function f2(v3, v4) {} }",
         "function f1(a, b) { function c(d, e) {} }");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithExterns1
  public void testRenameWithExterns1() {
    String externs = "var bar; function alert() {}";
    test(externs,
        "function foo(bar) { alert(bar); } foo(3)",
        "function foo(a) { alert(a); } foo(3)", null, null);
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithExterns2
  public void testRenameWithExterns2() {
    test("var a; function alert() {}",
        "function foo(bar) { alert(a);alert(bar); } foo(3);",
        "function foo(b) { alert(a);alert(b); } foo(3);",
        null, null);
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testDoNotRenameExportedName
  public void testDoNotRenameExportedName() {
    test("_foo()", "_foo()");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithNameOverlap
  public void testRenameWithNameOverlap() {
    test("function local() { var a = 1; var b = 2; b + b; }",
        "function local() { var b = 1; var a = 2; a + a; }");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithPrefix1
  public void testRenameWithPrefix1() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function Foo(a, b) {return a} Foo();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithPrefix2
  public void testRenameWithPrefix2() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {var v3 = v1 + v2; return v3;} Foo();",
         "function Foo(a, b) {var c = a + b; return c;} Foo();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithPrefix3
  public void testRenameWithPrefix3() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C," +
         "      D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,aa;"  +
         "  Foo();" +
         "} Bar();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypeProperties
  public void testPrototypeProperties() {
    test("Bar.prototype.getA = function(){}; bar.getA();" +
         "Bar.prototype.getB = function(){};",
         "Bar.prototype.a = function(){}; bar.a();" +
         "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys1
  public void testPrototypePropertiesAsObjLitKeys1() {
    test("Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
         "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys2
  public void testPrototypePropertiesAsObjLitKeys2() {
    testSame("Bar.prototype = {get 2(){}}; bar[2];");

    testSame("Bar.prototype = {get 'a'(){}}; bar['a'];");

    test("Bar.prototype = {get getA(){}}; bar.getA;",
         "Bar.prototype = {get a(){}}; bar.a;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys3
  public void testPrototypePropertiesAsObjLitKeys3() {
    testSame("Bar.prototype = {set 2(x){}}; bar[2];");

    testSame("Bar.prototype = {set 'a'(x){}}; bar['a'];");

    test("Bar.prototype = {set getA(x){}}; bar.getA;",
         "Bar.prototype = {set a(x){}}; bar.a;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeys1
  public void testMixedQuotedAndUnquotedObjLitKeys1() {
    test("Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
         "Bar = {a: function(){}, 'getB': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeys2
  public void testMixedQuotedAndUnquotedObjLitKeys2() {
    test("Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
         "Bar = {a: function(){}, 'getB': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testQuotedPrototypeProperty
  public void testQuotedPrototypeProperty() {
    testSame("Bar.prototype['getA'] = function(){}; bar['getA']();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNames
  public void testOverlappingOriginalAndGeneratedNames() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscores
  public void testRenamePropertiesWithLeadingUnderscores() {
    test("Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
         "Bar.prototype = {a: function(){}, b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObject
  public void testPropertyAddedToObject() {
    test("var foo = {}; foo.prop = '';",
         "var foo = {}; foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToFunction
  public void testPropertyAddedToFunction() {
    test("var foo = function(){}; foo.prop = '';",
         "var foo = function(){}; foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyOfObjectOfUnknownType
  public void testPropertyOfObjectOfUnknownType() {
    test("var foo = x(); foo.prop = '';",
         "var foo = x(); foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSetPropertyOfThis
  public void testSetPropertyOfThis() {
    test("this.prop = 'bar'",
         "this.a = 'bar'");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testReadPropertyOfThis
  public void testReadPropertyOfThis() {
    test("f(this.prop);",
         "f(this.a);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testObjectLiteralInLocalScope
  public void testObjectLiteralInLocalScope() {
    test("function x() { var foo = {prop1: 'bar', prop2: 'baz'}; }",
         "function x() { var foo = {a: 'bar', b: 'baz'}; }");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testIncorrectAttemptToAccessQuotedProperty
  public void testIncorrectAttemptToAccessQuotedProperty() {
    
    test("Bar.prototype = {'B': 0, 'getFoo': function(){}}; bar.getFoo();",
         "Bar.prototype = {'B': 0, 'getFoo': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSetQuotedPropertyOfThis
  public void testSetQuotedPropertyOfThis() {
    testSame("this['prop'] = 'bar';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testExternedPropertyName
  public void testExternedPropertyName() {
    test("Bar.prototype = {toString: function(){}, foo: 0}; bar.toString();",
         "Bar.prototype = {toString: function(){}, a: 0}; bar.toString();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testExternedPropertyNameDefinedByObjectLiteral
  public void testExternedPropertyNameDefinedByObjectLiteral() {
    test("function x() { var foo = google.gears.factory; }",
         "function x() { var foo = google.gears.factory; }");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames
  public void testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames() {
    test("Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
         "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSamePropertyNameQuotedAndUnquoted
  public void testSamePropertyNameQuotedAndUnquoted() {
    test("Bar.prototype.prop = function(){}; y = {'prop': 0};",
         "Bar.prototype.a = function(){}; y = {'prop': 0};");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("Bar = function(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA(); bar.getA();",
         "Bar = function(){}; Bar.a = function(){}; " +
         "Bar.prototype.a = function(){}; Bar.a(); bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall1
  public void testRenamePropertiesFunctionCall1() {
    test("var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall2
  public void testRenamePropertiesFunctionCall2() {
    test("var foo = {myProp: 0}; " +
         "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
         "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
         "var foo = {a: 0}; f('b.a.c'); " +
         "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs1
  public void testRemoveRenameFunctionStubs1() {
    test("function JSCompiler_renameProperty(x) { return x; }",
         "");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs2
  public void testRemoveRenameFunctionStubs2() {
    test("function JSCompiler_renameProperty(x) { return x; }" +
         "var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testGeneratePseudoNames
  public void testGeneratePseudoNames() {
    generatePseudoNames = true;
    test("var foo={}; foo.bar=1; foo['abc']=2",
         "var foo={}; foo.$bar$=1; foo['abc']=2");
    generatePseudoNames = false;
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testModules
  public void testModules() {
    String module1Js = "function Bar(){} Bar.prototype.getA=function(x){};" +
                       "var foo;foo.getA(foo);foo.doo=foo;foo.bloo=foo;";

    String module2Js = "function Far(){} Far.prototype.getB=function(x){};" +
                       "var too;too.getB(too);too.woo=too;too.bloo=too;";

    String module3Js = "function Car(){} Car.prototype.getC=function(x){};" +
                       "var noo;noo.getC(noo);noo.zoo=noo;noo.cloo=noo;";

    JSModule module1 = new JSModule("m1");
    module1.add(JSSourceFile.fromCode("input1", module1Js));

    JSModule module2 = new JSModule("m2");
    module2.add(JSSourceFile.fromCode("input2", module2Js));

    JSModule module3 = new JSModule("m3");
    module3.add(JSSourceFile.fromCode("input3", module3Js));

    JSModule[] modules = new JSModule[] { module1, module2, module3 };
    Compiler compiler = compileModules("", modules);

    Result result = compiler.getResult();
    assertTrue(result.success);

    assertEquals("function Bar(){}Bar.prototype.b=function(x){};" +
                 "var foo;foo.b(foo);foo.f=foo;foo.a=foo;",
                 compiler.toSource(module1));

    assertEquals("function Far(){}Far.prototype.c=function(x){};" +
                 "var too;too.c(too);too.g=too;too.a=too;",
                 compiler.toSource(module2));

    
    
    
    
    
    
    assertEquals("function Car(){}Car.prototype.d=function(x){};" +
                 "var noo;noo.d(noo);noo.h=noo;noo.e=noo;",
                 compiler.toSource(module3));
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAffinity
  public void testPropertyAffinity() {
    
    
    useAffinity = true;
    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
         "function f1() { foo.z; foo.z; foo.z; foo.y}" +
         "function f2() {                      foo.x}",

         "var foo={};foo.c=1;foo.b=2;foo.a=3;" +
         "function f1() { foo.a; foo.a; foo.a; foo.b}" +
         "function f2() {                      foo.c}");

    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
        "function f1() { foo.z; foo.z; foo.z; foo.y}" +
        "function f2() { foo.z; foo.z; foo.z; foo.x}",

        "var foo={};foo.b=1;foo.c=2;foo.a=3;" +
        "function f1() { foo.a; foo.a; foo.a; foo.c}" +
        "function f2() { foo.a; foo.a; foo.a; foo.b}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAffinityOff
  public void testPropertyAffinityOff() {
    useAffinity = false;
    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
         "function f1() { foo.z; foo.z; foo.z; foo.y}" +
         "function f2() {                      foo.x}",

         "var foo={};foo.b=1;foo.c=2;foo.a=3;" +
         "function f1() { foo.a; foo.a; foo.a; foo.c}" +
         "function f2() {                      foo.b}");

    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
        "function f1() { foo.z; foo.z; foo.z; foo.y}" +
        "function f2() { foo.z; foo.z; foo.z; foo.x}",

        "var foo={};foo.b=1;foo.c=2;foo.a=3;" +
        "function f1() { foo.a; foo.a; foo.a; foo.c}" +
        "function f2() { foo.a; foo.a; foo.a; foo.b}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesStable
  public void testPrototypePropertiesStable() {
    testStableRenaming(
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}",
        "Bar.prototype.get = function(){}; bar.get();" +
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.c = function(){}; bar.c();" +
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeysStable
  public void testPrototypePropertiesAsObjLitKeysStable() {
    testStableRenaming(
        "Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
        "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();",
        "Bar.prototype = {getB: function(){},getA: function(){}}; bar.getB();",
        "Bar.prototype = {b: function(){},a: function(){}}; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeysStable
  public void testMixedQuotedAndUnquotedObjLitKeysStable() {
    testStableRenaming(
        "Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
        "Bar = {a: function(){}, 'getB': function(){}}; bar.a();",
        "Bar = {get: function(){}, getA: function(){}, 'getB': function(){}};" +
        "bar.getA();bar.get();",
        "Bar = {b: function(){}, a: function(){}, 'getB': function(){}};" +
        "bar.a();bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNamesStable
  public void testOverlappingOriginalAndGeneratedNamesStable() {
    testStableRenaming(
        "Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
        "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();",
        "Bar.prototype = {c: function(){}, b: function(){}, a: function(){}};" +
        "bar.b();",
        "Bar.prototype = {c: function(){}, a: function(){}, b: function(){}};" +
        "bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStableWithTrickyExternsChanges
  public void testStableWithTrickyExternsChanges() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
    prevUsedPropertyMap = renameProperties.getPropertyMap();
    String externs = EXTERNS + "prop.b;";
    test(externs,
         "Bar.prototype = {new_f: function(){}, b: function(){}, " +
         "a: function(){}};bar.b();",
         "Bar.prototype = {c:function(){}, b:function(){}, a:function(){}};" +
         "bar.b();", null, null);
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscoresStable
  public void testRenamePropertiesWithLeadingUnderscoresStable() {
    testStableRenaming(
        "Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, b: 0}; bar.a();",
        "Bar.prototype = {_getA: function(){}, _c: 1, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, c: 1,  b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObjectStable
  public void testPropertyAddedToObjectStable() {
    testStableRenaming("var foo = {}; foo.prop = '';",
                       "var foo = {}; foo.a = '';",
                       "var foo = {}; foo.prop = ''; foo.a='';",
                       "var foo = {}; foo.a = ''; foo.b='';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable
  public void testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable() {
    testStableRenaming(
        "Bar.prototype.foo = function(){}; Bar.prototype['b'] = 0; bar.foo();",
        "Bar.prototype.a = function(){}; Bar.prototype['b'] = 0; bar.a();",
        "Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
        "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCallStable
  public void testRenamePropertiesFunctionCallStable() {
    testStableRenaming(
        "var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;",
        "var bar = {newProp: 0}; var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var bar = {f: 0}; var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenamePrototypes1
  public void testRenamePrototypes1() {
    test("Bar.prototype={'getFoo':function(){},2:function(){}}",
         "Bar.prototype={'a':function(){},2:function(){}}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenamePrototypes2
  public void testRenamePrototypes2() {
    
    test("Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
         "Bar.prototype.getBaz=function(){}",
         "Bar.prototype.a=function(){};Bar.a(b);" +
         "Bar.prototype.b=function(){}");
    test("Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
         "Bar.prototype['getBaz']=function(){}",
         "Bar.prototype['a']=function(){};Bar.a(b);" +
         "Bar.prototype['b']=function(){}");
    test("Bar.prototype={'getFoo':function(){},2:function(){}}",
         "Bar.prototype={'a':function(){},2:function(){}}");
    test("Bar.prototype={'getFoo':function(){}," +
         "'getBar':function(){}};b.getFoo()",
         "Bar.prototype={'a':function(){}," +
         "'b':function(){}};b.a()");

    test("Bar.prototype={'B':function(){}," +
         "'getBar':function(){}};b.getBar()",
         "Bar.prototype={'b':function(){}," +
         "'a':function(){}};b.a()");

    
    test("Bar.prototype={'a':function(){}," +
         "'b':function(){}};b.b()",
         "Bar.prototype={'b':function(){}," +
         "'a':function(){}};b.a()");

    
    test("Bar.prototype={'_getFoo':function(){}," +
         "'getBar':function(){}};b._getFoo()",
         "Bar.prototype={'_getFoo':function(){}," +
         "'a':function(){}};b._getFoo()");

    
    test("Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={'toString':function(){}," +
         "'a':function(){}};b.toString()");

    
    test("Bar.prototype.foo=function(){}" +
         ";bar.foo();bar.a",
         "Bar.prototype.b=function(){}" +
         ";bar.b();bar.a");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenamePrototypesWithGetOrSet
  public void testRenamePrototypesWithGetOrSet() {
    
    
    
    
    
    
    test("Bar.prototype={get getFoo(){}}",
         "Bar.prototype={get a(){}}");
    test("Bar.prototype={get getFoo(){}}; a.getFoo;",
         "Bar.prototype={get a(){}}; a.a;");

    
    
    
    
    
    test("Bar.prototype={set getFoo(x){}}",
         "Bar.prototype={set a(x){}}");
    test("Bar.prototype={set getFoo(x){}}; a.getFoo;",
         "Bar.prototype={set a(x){}}; a.a;");

    
    test("Bar.prototype={get a(){}," +
         "get b(){}};b.b()",
         "Bar.prototype={get b(){}," +
         "get a(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenameProperties
  public void testRenameProperties() {
    test("var foo; foo.prop_='bar'", "var foo;foo.a='bar'");
    test("this.prop_='bar'", "this.a='bar'");
    test("this.prop='bar'", "this.prop='bar'");
    test("this['prop_']='bar'", "this['a']='bar'");
    test("this['prop']='bar'", "this['prop']='bar'");
    test("var foo={prop1_: 'bar',prop2_: 'baz'};",
         "var foo={a:'bar',b:'baz'}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testBoth
  public void testBoth() {
    test("Bar.prototype.getFoo_=function(){};Bar.getFoo_(b);" +
         "Bar.prototype.getBaz_=function(){}",
         "Bar.prototype.a=function(){};Bar.a(b);" +
         "Bar.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testPropertyNameThatIsBothObjLitKeyAndPrototypeProperty
  public void testPropertyNameThatIsBothObjLitKeyAndPrototypeProperty() {
    
    
    
    
    test("x.prototype.myprop=function(){};y={myprop:0};z.myprop",
         "x.prototype.myprop=function(){};y={myprop:0};z.myprop");

    
    
    
    test("x.prototype.myprop_=function(){};y={myprop_:0};z.myprop_",
         "x.prototype.a=function(){};y={a:0};z.a");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testModule
  public void testModule() {
    JSModule[] modules = createModules(
        "function Bar(){} var foo; Bar.prototype.getFoo_=function(x){};" +
        "foo.getFoo_(foo);foo.doo_=foo;foo.bloo_=foo;",
        "function Far(){} var too; Far.prototype.getGoo_=function(x){};" +
        "too.getGoo_(too);too.troo_=too;too.bloo_=too;");

    test(modules, new String[] {
        "function Bar(){}var foo; Bar.prototype.a=function(x){};" +
        "foo.a(foo);foo.d=foo;foo.c=foo;",
        "function Far(){}var too; Far.prototype.b=function(x){};" +
        "too.b(too);too.e=too;too.c=too;"
    });
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple1
  public void testStableSimple1() {
    testStable(
        "Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
        "Bar.prototype.getBaz=function(){}",
        "Bar.prototype.a=function(){};Bar.a(b);" +
        "Bar.prototype.b=function(){}",
        "Bar.prototype.getBar=function(){};Bar.getBar(b);" +
        "Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
        "Bar.prototype.getBaz=function(){}",
        "Bar.prototype.c=function(){};Bar.c(b);" +
        "Bar.prototype.a=function(){};Bar.a(b);" +
        "Bar.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple2
  public void testStableSimple2() {
    testStable(
        "Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
        "Bar.prototype['getBaz']=function(){}",
        "Bar.prototype['a']=function(){};Bar.a(b);" +
        "Bar.prototype['b']=function(){}",
        "Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
        "Bar.prototype['getBar']=function(){};" +
        "Bar.prototype['getBaz']=function(){}",
        "Bar.prototype['a']=function(){};Bar.a(b);" +
        "Bar.prototype['c']=function(){};" +
        "Bar.prototype['b']=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple3
  public void testStableSimple3() {
    testStable(
        "Bar.prototype={'getFoo':function(){}," +
        "'getBar':function(){}};b.getFoo()",
        "Bar.prototype={'a':function(){}, 'b':function(){}};b.a()",
        "Bar.prototype={'getFoo':function(){}," +
        "'getBaz':function(){},'getBar':function(){}};b.getFoo()",
        "Bar.prototype={'a':function(){}, " +
        "'c':function(){}, 'b':function(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableOverlap
  public void testStableOverlap() {
    testStable(
        "Bar.prototype={'a':function(){},'b':function(){}};b.b()",
        "Bar.prototype={'b':function(){},'a':function(){}};b.a()",
        "Bar.prototype={'a':function(){},'b':function(){}};b.b()",
        "Bar.prototype={'b':function(){},'a':function(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableTrickyExternedMethods
  public void testStableTrickyExternedMethods() {
    test("Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={'toString':function(){}," +
         "'a':function(){}};b.toString()");
    prevUsedRenameMap = renamePrototypes.getPropertyMap();
    String externs = EXTERNS + "prop.a;";
    test(externs,
         "Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={'toString':function(){}," +
         "'b':function(){}};b.toString()", null, null);
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStable
  public void testStable(String input1, String expected1,
                         String input2, String expected2) {
    test(input1, expected1);
    prevUsedRenameMap = renamePrototypes.getPropertyMap();
    test(input2, expected2);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameSimple
  public void testRenameSimple() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("var Foo; var Bar, y; function x() { Bar++; }",
         "var a; var b, c; function d() { b++; }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocals
  public void testRenameLocals() {
    test("(function (v1, v2) {}); (function (v3, v4) {});",
        "(function (a, b) {}); (function (a, b) {});");
    test("function f1(v1, v2) {}; function f2(v3, v4) {};",
        "function c(a, b) {}; function d(a, b) {};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameRedeclaredGlobals
  public void testRenameRedeclaredGlobals() {
    test("function f1(v1, v2) {f1()};" +
         "" +
         "function f1(v3, v4) {f1()};",
         "function a(b, c) {a()};" +
         "function a(b, c) {a()};");

    localRenamingOnly = true;

    test("function f1(v1, v2) {f1()};" +
        "" +
        "function f1(v3, v4) {f1()};",
        "function f1(a, b) {f1()};" +
        "function f1(a, b) {f1()};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions1
  public void testRecursiveFunctions1() {
    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var a = function a(b, c) {" +
         "  a(b, c);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var walk = function walk(a, b) {" +
         "  walk(a, b);" +
         "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions2
  public void testRecursiveFunctions2() {
    preserveFunctionExpressionNames = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var c = function walk(a, b) {" +
         "  walk(a, b);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
        "  walk(node, aFunction);" +
        "};",
        "var walk = function walk(a, b) {" +
        "  walk(a, b);" +
        "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocalsClashingWithGlobals
  public void testRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
        "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameNested
  public void testRenameNested() {
    test("function f1(v1, v2) { (function(v3, v4) {}) }",
         "function a(b, c) { (function(d, e) {}) }");
    test("function f1(v1, v2) { function f2(v3, v4) {} }",
         "function a(b, c) { function d(e, f) {} }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testBleedingRecursiveFunctions1
  public void testBleedingRecursiveFunctions1() {
    
    
    
    test("var x = function a(x) { return x ? 1 : a(1); };" +
         "var y = function b(x) { return x ? 2 : b(2); };",
         "var c = function b(a) { return a ? 1 : b(1); };" +
         "var e = function d(a) { return a ? 2 : d(2); };");
  }

// com.google.javascript.jscomp.RenameVarsTest::testBleedingRecursiveFunctions2
  public void testBleedingRecursiveFunctions2() {
    test("function f() {" +
         "  var x = function a(x) { return x ? 1 : a(1); };" +
         "  var y = function b(x) { return x ? 2 : b(2); };" +
         "}",
         "function d() {" +
         "  var e = function b(a) { return a ? 1 : b(1); };" +
         "  var f = function a(c) { return c ? 2 : a(2); };" +
         "}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testBleedingRecursiveFunctions3
  public void testBleedingRecursiveFunctions3() {
    test("function f() {" +
         "  var x = function a(x) { return x ? 1 : a(1); };" +
         "  var y = function b(x) { return x ? 2 : b(2); };" +
         "  var z = function c(x) { return x ? y : c(2); };" +
         "}",
         "function f() {" +
         "  var g = function c(a) { return a ? 1 : c(1); };" +
         "  var d = function a(b) { return b ? 2 : a(2); };" +
         "  var h = function b(e) { return e ? d : b(2); };" +
         "}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns1
  public void testRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns2
  public void testRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testDoNotRenameExportedName
  public void testDoNotRenameExportedName() {
    test("_foo()", "_foo()");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithNameOverlap
  public void testRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix1
  public void testRenameWithPrefix1() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
        "function PRE_(a, b) {return a} PRE_();");
    prefix = DEFAULT_PREFIX;

  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix2
  public void testRenameWithPrefix2() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {var v3 = v1 + v2; return v3;} Foo();",
        "function PRE_(a, b) {var c = a + b; return c;} PRE_();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix3
  public void testRenameWithPrefix3() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

        "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testNamingBasedOnOrderOfOccurrence
  public void testNamingBasedOnOrderOfOccurrence() {
    test("var q,p,m,n,l,k; " +
             "(function (r) {}); try { } catch(s) {}; var t = q + q;",
         "var a,b,c,d,e,f; " +
             "(function(g) {}); try { } catch(h) {}; var i = a + a;"
         );
    test("(function(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z," +
         "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,$){});" +
         "var a4,a3,a2,a1,b4,b3,b2,b1,ab,ac,ad,fg;function foo(){};",
         "(function(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$){});" +
         "var aa,ba,ca,da,ea,fa,ga,ha,ia,ja,ka,la;function ma(){};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimple
  public void testStableRenameSimple() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c", "L 2", "d");
    testRenameMapUsingOldMap("function Foo(v1, v2, v3) {return v1;} Foo();",
         "function a(b, c, d) {return b;} a();", expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameGlobals
  public void testStableRenameGlobals() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d");
    testRenameMap("var Foo; var Bar, y; function x() { Bar++; }",
                  "var a; var b, c; function d() { b++; }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d", "Baz", "f", "L 0" , "e");
    testRenameMapUsingOldMap(
        "var Foo, Baz; var Bar, y; function x(R) { return R + Bar++; }",
        "var a, f; var b, c; function d(e) { return e + b++; }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPointlesslyAnonymousFunctions
  public void testStableRenameWithPointlesslyAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b");
    testRenameMap("(function (v1, v2) {}); (function (v3, v4) {});",
                  "(function (a, b) {}); (function (a, b) {});",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b", "L 2", "c");
    testRenameMapUsingOldMap("(function (v0, v1, v2) {});" +
                             "(function (v3, v4) {});",
                             "(function (a, b, c) {});" +
                             "(function (a, b) {});",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameLocalsClashingWithGlobals
  public void testStableRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
    previouslyUsedMap = renameVars.getVariableMap();
    test("function bar(){return;}function a(v1, v2) {return v1;} a();",
         "function d(){return;}function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameNested
  public void testStableRenameNested() {
    VariableMap expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e");
    testRenameMap("function f1(v1, v2) { (function(v3, v4) {}) }",
                  "function a(b, c) { (function(d, e) {}) }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e", "L 4", "f");
    testRenameMapUsingOldMap(
        "function f1(v1, v2) { (function(v3, v4, v5) {}) }",
        "function a(b, c) { (function(d, e, f) {}) }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns1
  public void testStableRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var bar, baz; foo(bar, baz);",
         "var a, b; foo(a, b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns2
  public void testStableRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var b = 5, catty = 9;", "var b = 5, c=9;", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithNameOverlap
  public void testStableRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
    previouslyUsedMap = renameVars.getVariableMap();
    test("var a = 1; var c, b = 2; b + b;",
         "var a = 1; var c, b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithAnonymousFunctions
  public void testStableRenameWithAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "foo", "b");
    testRenameMap("function foo(bar){return bar;}foo(function(h){return h;});",
                  "function b(a){return a}b(function(a){return a;})",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("foo", "b", "L 0", "a", "L 1", "c");
    testRenameMapUsingOldMap(
        "function foo(bar) {return bar;}foo(function(g,h) {return g+h;});",
        "function b(a){return a}b(function(a,c){return a+c;})",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleExternsChanges
  public void testStableRenameSimpleExternsChanges() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "b", "L 1", "c", "L 2", "a");
    String externs = "var Foo;";
    testRenameMapUsingOldMap(externs,
                             "function Foo(v1, v2, v0) {return v1;} Foo();",
                             "function Foo(b, c, a) {return b;} Foo();",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleLocalNameExterned
  public void testStableRenameSimpleLocalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var b;";
    test(externs, "function Foo(v1, v2) {return v1;} Foo(b);",
         "function a(d, c) {return d;} a(b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleGlobalNameExterned
  public void testStableRenameSimpleGlobalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var Foo;";
    test(externs, "function Foo(v1, v2, v0) {return v1;} Foo();",
         "function Foo(b, c, a) {return b;} Foo();", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix1AndUnstableLocalNames
  public void testStableRenameWithPrefix1AndUnstableLocalNames() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function PRE_(a, b) {return a} PRE_();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "PRE_";
    test("function Foo(v0, v1, v2) {return v1} Foo();",
         "function PRE_(a, b, c) {return b} PRE_();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix2
  public void testStableRenameWithPrefix2() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Baz() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function ab() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testContrivedExampleWhereConsistentRenamingIsWorse
  public void testContrivedExampleWhereConsistentRenamingIsWorse() {
    previouslyUsedMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");

    test("function Foo(v1, v2) {return v1;} Foo();",
         "function LongString(b, c) {return b;} LongString();");

    previouslyUsedMap = renameVars.getVariableMap();
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");
    assertVariableMapsEqual(expectedVariableMap, previouslyUsedMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportSimpleSymbolReservesName
  public void testExportSimpleSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a', x);",
         "var a, b; a.exportSymbol('a', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a', x);",
         "var b, c; b.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportComplexSymbolReservesName
  public void testExportComplexSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var a, b; a.exportSymbol('a.b', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var b, c; b.exportSymbol('a.b', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportToNonStringDoesntExplode
  public void testExportToNonStringDoesntExplode() {
    withClosurePass = true;
    test("var goog, a, b; goog.exportSymbol(a, b);",
         "var a, b, c; a.exportSymbol(b, c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport1
  public void testDollarSignSuperExport1() {
    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,    a,        b){}");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,a,b){}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport2
  public void testDollarSignSuperExport2() {
    boolean normalizedExpectedJs = false;
    super.enableNormalize(false);

    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,    a,         b){};" +
            "var d = function($super,    a){};");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,   a,    b         ){};" +
            "var d = function($super,a){};");

    super.disableNormalize();
  }

// com.google.javascript.jscomp.RenameVarsTest::testPseudoNames
  public void testPseudoNames() {
    generatePseudoNames = false;
    
    test("var foo = function(a, b, c){}",
         "var d = function(a, b, c){}");

    generatePseudoNames = true;
    test("var foo = function(a, b, c){}",
         "var $foo$$ = function($a$$, $b$$, $c$$){}");

    test("var a = function(a, b, c){}",
         "var $a$$ = function($a$$, $b$$, $c$$){}");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testDoNotUseReplacementMap
  public void testDoNotUseReplacementMap() {
    useReplacementMap = false;
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'goog-footer-active'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'goog-colorswatch-disabled'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('active-buttonbar')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithUnknownStringLiterals
  public void testOneArgWithUnknownStringLiterals() {
    test("var x = goog.getCssName('unknown')",
         "var x = 'unknown'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('ooo')",
         "el.className = 'ooo'", null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('ab'))",
         "setClass('ab')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithSimpleStringLiterals
  public void testOneArgWithSimpleStringLiterals() {
    test("var x = goog.getCssName('buttonbar')",
         "var x = 'b'");
    test("el.className = goog.getCssName('colorswatch')",
         "el.className = 'c'");
    test("setClass(goog.getCssName('elephant'))",
         "setClass('e')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("buttonbar", 1)
        .put("colorswatch", 1)
        .put("elephant", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNames
  public void testOneArgWithCompositeClassNames() {
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'g-f-a'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'g-c-d'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('a-b')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNamesFull
  public void testOneArgWithCompositeClassNamesFull() {
    renamingMap = getFullMap();

    test("var x = goog.getCssName('long-prefix')",
         "var x = 'h'");
    test("var x = goog.getCssName('long-prefix-suffix1')",
         "var x = 'h-i'");
    test("var x = goog.getCssName('unrelated')",
         "var x = 'l'");
    test("var x = goog.getCssName('unrelated-word')",
         "var x = 'k'");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNamesWithUnknownParts
  public void testOneArgWithCompositeClassNamesWithUnknownParts() {
    test("var x = goog.getCssName('goog-header-active')",
         "var x = 'goog-header-active'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('goog-colorswatch-focussed')",
         "el.className = 'goog-colorswatch-focussed'",
         null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('inactive-buttonbar'))",
         "setClass('inactive-buttonbar')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArgsWithStringLiterals
  public void testTwoArgsWithStringLiterals() {
    test("var x = goog.getCssName('header', 'active')",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("el.className = goog.getCssName('footer', window)",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName('buttonbar', 'disabled'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName(goog.getCssName('buttonbar'), 'active'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArsWithVariableFirstArg
  public void testTwoArsWithVariableFirstArg() {
    test("var x = goog.getCssName(baseClass, 'active')",
         "var x = baseClass + '-a'");
    test("el.className = goog.getCssName(this.getClass(), 'disabled')",
         "el.className = this.getClass() + '-d'");
    test("setClass(goog.getCssName(BASE_CLASS, 'disabled'))",
         "setClass(BASE_CLASS + '-d')");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArgsWithVariableFirstArgFull
  public void testTwoArgsWithVariableFirstArgFull() {
    renamingMap = getFullMap();

    test("var x = goog.getCssName(baseClass, 'long-suffix')",
         "var x = baseClass + '-m'");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testZeroArguments
  public void testZeroArguments() {
    test("goog.getCssName()", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testManyArguments
  public void testManyArguments() {
    test("goog.getCssName('a', 'b', 'c')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNonStringArgument
  public void testNonStringArgument() {
    test("goog.getCssName(window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName([]);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName({});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);

    test("goog.getCssName(baseClass, window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, 555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, []);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, {});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNoSymbolMapStripsCallAndDoesntIssueWarnings
  public void testNoSymbolMapStripsCallAndDoesntIssueWarnings() {
    String input = "[goog.getCssName('test'), goog.getCssName(base, 'active')]";
    Compiler compiler = new Compiler();
    ErrorManager errorMan = new BasicErrorManager() {
      @Override protected void printSummary() {}
      @Override public void println(CheckLevel level, JSError error) {}
    };
    compiler.setErrorManager(errorMan);
    Node root = compiler.parseTestCode(input);
    useReplacementMap = false;
    ReplaceCssNames replacer = new ReplaceCssNames(compiler, null);
    replacer.process(null, root);
    assertEquals("[\"test\",base+\"-active\"]", compiler.toSource(root));
    assertEquals("There should be no errors", 0, errorMan.getErrorCount());
    assertEquals("There should be no warnings", 0, errorMan.getWarningCount());
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testAssign
  public void testAssign() {
    test("foo.bar = goog.events.getUniqueId('foo_bar')",
         "foo.bar = 'a'");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("foo = { bar : goog.events.getUniqueId('foo_bar')}",
         "foo = { bar : 'a' }");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testTwoNamespaces
  public void testTwoNamespaces() {
    test("foo.bar = goog.events.getUniqueId('foo_bar');\n"
         + "baz.blah = goog.place.getUniqueId('baz_blah');\n",
         "foo.bar = 'a';\n"
         + "baz.blah = 'a'\n");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testLocalCall
  public void testLocalCall() {
    testSame(new String[] {
          "function Foo() { goog.events.getUniqueId('foo'); }"
        },
        ReplaceIdGenerators.NON_GLOBAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testConditionalCall
  public void testConditionalCall() {
    testSame(new String[] {"if (x) foo = goog.events.getUniqueId('foo')"},
             ReplaceIdGenerators.CONDITIONAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testReplaceSimpleMessage
  public void testReplaceSimpleMessage() {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("Hi\nthere")
        .build());

    assertOutputEquals("var MSG_A = goog.getMsg('asdf');",
        "var MSG_A=\"Hi\\nthere\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testNameReplacement
  public void testNameReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    assertOutputEquals(
        "var MSG_B = goog.getMsg('asdf {$measly}', {measly: x});",
        "var MSG_B=\"One \"+(x+\" ph\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testGetPropReplacement
  public void testGetPropReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_C = goog.getMsg('${$amount}', {amount: a.b.amount});",
        "var MSG_C=a.b.amount");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionCallReplacement
  public void testFunctionCallReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_D")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_D = goog.getMsg('${$amount}', {amount: getAmt()});",
        "var MSG_D=getAmt()");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testMethodCallReplacement
  public void testMethodCallReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_E")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_E = goog.getMsg('${$amount}', {amount: obj.getAmt()});",
        "var MSG_E=obj.getAmt()");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testHookReplacement
  public void testHookReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_F")
        .appendStringPart("#")
        .appendPlaceholderReference("amount")
        .appendStringPart(".")
        .build());

    assertOutputEquals(
        "var MSG_F = goog.getMsg('${$amount}', {amount: (a ? b : c)});",
        "var MSG_F=\"#\"+((a?b:c)+\".\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testAddReplacement
  public void testAddReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_G")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_G = goog.getMsg('${$amount}', {amount: x + ''});",
        "var MSG_G=x+\"\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderValueReferencedTwice
  public void testPlaceholderValueReferencedTwice()  {
    registerMessage(new JsMessage.Builder("MSG_H")
        .appendPlaceholderReference("dick")
        .appendStringPart(", ")
        .appendPlaceholderReference("dick")
        .appendStringPart(" and ")
        .appendPlaceholderReference("jane")
        .build());

    assertOutputEquals(
        "var MSG_H = goog.getMsg('{$dick}{$jane}', {jane: x, dick: y});",
        "var MSG_H=y+(\", \"+(y+(\" and \"+x)))");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderNameInLowerCamelCase
  public void testPlaceholderNameInLowerCamelCase()  {
    registerMessage(new JsMessage.Builder("MSG_I")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amtEarned")
        .build());

    assertOutputEquals(
        "var MSG_I = goog.getMsg('${$amtEarned}', {amtEarned: x});",
        "var MSG_I=\"Sum: $\"+x");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testQualifiedMessageName
  public void testQualifiedMessageName()  {
    registerMessage(new JsMessage.Builder("MSG_J")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    assertOutputEquals(
        "a.b.c.MSG_J = goog.getMsg('asdf {$measly}', {measly: x});",
        "a.b.c.MSG_J=\"One \"+(x+\" ph\")");
  }
