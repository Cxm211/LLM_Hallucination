// buggy code
  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp());

      node = node.getFirstChild();
    if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return true;
  }

// relevant test
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

// com.google.javascript.jscomp.IntegrationTest::testManyAdds
  public void testManyAdds() {}

// com.google.javascript.jscomp.IntegrationTest::testPerfTracker
  public void testPerfTracker() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream outstream = new PrintStream(output);
    Compiler compiler = new Compiler(outstream);
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> inputs = Lists.newArrayList();
    List<SourceFile> externs = Lists.newArrayList();

    options.setTracerMode(TracerMode.ALL);
    inputs.add(SourceFile.fromCode("foo", "function fun(){}"));
    compiler.compile(externs, inputs, options);
    outstream.flush();
    outstream.close();
    Pattern p = Pattern.compile(
        ".*Summary:\npass,runtime,runs,changingRuns,reduction,gzReduction" +
        ".*TOTAL:" +
        "\nRuntime\\(ms\\): [0-9]+" +
        "\n#Runs: [0-9]+" +
        "\n#Changing runs: [0-9]+" +
        "\n#Loopable runs: [0-9]+" +
        "\n#Changing loopable runs: [0-9]+" +
        "\nEstimated Reduction\\(bytes\\): [0-9]+" +
        "\nEstimated GzReduction\\(bytes\\): [0-9]+" +
        "\nEstimated Size\\(bytes\\): [0-9]+" +
        "\nEstimated GzSize\\(bytes\\): [0-9]+" +
        "\nTotal Size\\(bytes\\): -?[0-9]+" +
        "\nTotal GzSize\\(bytes\\): -?[0-9]+" +
        "\n\nLog:\n" +
        "pass,runtime,runs,changingRuns,reduction,gzReduction,size,gzSize.*",
        Pattern.DOTALL);
    assertTrue(p.matcher(output.toString()).matches());
  }

// com.google.javascript.jscomp.IntegrationTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    String[] input1 = {"function f(z) { return z; }"};
    String[] input2 = {"function f(y) { return y; }"};
    CompilerOptions options = new CompilerOptions();
    Node out1 = parse(input1, options, false);
    Node out2 = parse(input2, options, false);
    assertFalse(out1.isEquivalentTo(out2));
  }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
