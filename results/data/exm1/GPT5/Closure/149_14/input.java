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

    createDefineReplacements(config.define, options);

    options.manageClosureDependencies = config.manageClosureDependencies;
    options.devMode = config.jscompDevMode;
    options.setCodingConvention(config.codingConvention);
    options.setSummaryDetailLevel(config.summaryDetailLevel);

    inputCharset = getInputCharset();

    if (config.jsOutputFile.length() > 0) {
      options.jsOutputFile = config.jsOutputFile;
    }

    if (config.createSourceMap.length() > 0) {
      options.sourceMapOutputPath = config.createSourceMap;
    }
    options.sourceMapDetailLevel = config.sourceMapDetailLevel;

    if (!config.variableMapInputFile.equals("")) {
      options.inputVariableMapSerialized =
          VariableMap.load(config.variableMapInputFile).toBytes();
    }

    if (!config.propertyMapInputFile.equals("")) {
      options.inputPropertyMapSerialized =
          VariableMap.load(config.propertyMapInputFile).toBytes();
    }
  }

  protected int doRun() throws FlagUsageException, IOException {
    Compiler.setLoggingLevel(Level.parse(config.loggingLevel));

    List<JSSourceFile> externsList = createExterns();
    JSSourceFile[] externs = new JSSourceFile[externsList.size()];
    externsList.toArray(externs);

    compiler = createCompiler();
    B options = createOptions();

    JSModule[] modules = null;
    Result result;

    setRunOptions(options);
    if (inputCharset == Charsets.UTF_8) {
      options.outputCharset = Charsets.US_ASCII;
    } else {
      options.outputCharset = inputCharset;
    }

    boolean writeOutputToFile = !options.jsOutputFile.isEmpty();
    if (writeOutputToFile) {
      out = toWriter(options.jsOutputFile, inputCharset.name());
    }

    List<String> jsFiles = config.js;
    List<String> moduleSpecs = config.module;
    if (!moduleSpecs.isEmpty()) {
      modules = createJsModules(moduleSpecs, jsFiles);
      result = compiler.compile(externs, modules, options);
    } else {
      List<JSSourceFile> inputList = createSourceInputs(jsFiles);
      JSSourceFile[] inputs = new JSSourceFile[inputList.size()];
      inputList.toArray(inputs);
      result = compiler.compile(externs, inputs, options);
    }

    int errCode = processResults(result, modules, options);
    // Close the output if we are writing to a file.
    if (writeOutputToFile) {
      ((Writer)out).close();
    }
    return errCode;
  }

  private Charset getInputCharset() throws FlagUsageException {
    if (!config.charset.isEmpty()) {
      if (!Charset.isSupported(config.charset)) {
        throw new FlagUsageException(config.charset +
            " is not a valid charset name.");
      }
      return Charset.forName(config.charset);
    }
    return Charsets.UTF_8;
  }

  protected CompilerOptions createOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel level = flags.compilation_level;
    level.setOptionsForCompilationLevel(options);
    if (flags.debug) {
      level.setDebugOptionsForCompilationLevel(options);
    }

    WarningLevel wLevel = flags.warning_level;
    wLevel.setOptionsForWarningLevel(options);
    for (FormattingOption formattingOption : flags.formatting) {
      formattingOption.applyToOptions(options);
    }

    options.closurePass = flags.process_closure_primitives;
    initOptionsFromFlags(options);
    return options;
  }

  private String toSource(Node n, SourceMap sourceMap) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(options.prettyPrint);
    builder.setLineBreak(options.lineBreak);
    builder.setSourceMap(sourceMap);
    builder.setSourceMapDetailLevel(options.sourceMapDetailLevel);

    Charset charset = options.outputCharset;
    builder.setOutputCharset(charset);

    return builder.build();
  }

  public boolean isExternExportsEnabled() {
    return externExports;
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl3
  public void testDuplicateStaticMethodDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl4
  public void testDuplicateStaticMethodDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl5
  public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl1
  public void testDuplicateStaticPropertyDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl2
  public void testDuplicateStaticPropertyDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl3
  public void testDuplicateStaticPropertyDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl4
  public void testDuplicateStaticPropertyDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl5
  public void testDuplicateStaticPropertyDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl6
  public void testDuplicateStaticPropertyDecl6() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = 'y';" +
        " goog.foo = 'x';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl7
  public void testDuplicateStaticPropertyDecl7() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl8
  public void testDuplicateStaticPropertyDecl8() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " function EventCopy() {}" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl9
  public void testDuplicateStaticPropertyDecl9() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function EventCopy() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateLocalVarDecl
  public void testDuplicateLocalVarDecl() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {  var x = ''; }",
        "variable x redefined with type string, " +
        "original definition at [testcode]:2 with type number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration1
  public void testStubFunctionDeclaration1() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.prototype.foo;",
        "(new f).foo",
        "function (this:f, number, string): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration2
  public void testStubFunctionDeclaration2() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.subclass;",
        "f.subclass",
        "function (this:f.subclass): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration3
  public void testStubFunctionDeclaration3() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.foo;",
        "f.foo",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration4
  public void testStubFunctionDeclaration4() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        "function (this:f): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration5
  public void testStubFunctionDeclaration5() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration6
  public void testStubFunctionDeclaration6() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo;",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration7
  public void testStubFunctionDeclaration7() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo = function() {};",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration8
  public void testStubFunctionDeclaration8() throws Exception {
    
    testFunctionType(
        " var f = function() {}; ",
        "f",
        createNullableType(U2U_CONSTRUCTOR_TYPE).
          restrictByNotNullOrUndefined().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration9
  public void testStubFunctionDeclaration9() throws Exception {
    testFunctionType(
        " var f; ",
        "f",
        "function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration10
  public void testStubFunctionDeclaration10() throws Exception {
    testFunctionType(
        " var f = function(x) {};",
        "f",
        "function (number): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNestedFunctionInference1
  public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        " function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeRedefinition
  public void testTypeRedefinition() throws Exception {
    testTypes("a={}; a.A = {ZOR:'b'};"
        + " a.A = function() {}",
        "variable a.A redefined with type function (this:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn1
  public void testIn1() throws Exception {
    testTypes("'foo' in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn2
  public void testIn2() throws Exception {
    testTypes("3 in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn3
  public void testIn3() throws Exception {
    testTypes("undefined in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn4
  public void testIn4() throws Exception {
    testTypes("Date in Object",
        "left side of 'in'\n" +
        "found   : function (this:Date, ?, ?, ?, ?, ?, ?, ?): string\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn5
  public void testIn5() throws Exception {
    testTypes("'x' in null",
        "'in' requires an object\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn6
  public void testIn6() throws Exception {
    testTypes(
        "" +
        "function g(x) {}" +
        "g(1 in {});",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn7
  public void testIn7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  return g(x.foo) in {};" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison2
  public void testComparison2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "if (a!==b) {}",
        "condition always evaluates to the same value\n" +
        "left : number\n" +
        "right: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison3
  public void testComparison3() throws Exception {
    
    testTypes("var a;" +
        "var b = a == null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison4
  public void testComparison4() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a == b");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison5
  public void testComparison5() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison6
  public void testComparison6() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a != b",
        "condition always evaluates to false\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison7
  public void testComparison7() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison8
  public void testComparison8() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null || a[1] == undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison9
  public void testComparison9() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison10
  public void testComparison10() throws Exception {
    testTypes(" var a = [];" +
        "a[0] === null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnumStaticMethod1
  public void testEnumStaticMethod1() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "Foo.method(true);",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnumStaticMethod2
  public void testEnumStaticMethod2() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "function f() { Foo.method(true); }",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum1
  public void testEnum1() throws Exception {
    testTypes("var a={BB:1,CC:2};\n" +
        "var d;d=a.BB;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum2
  public void testEnum2() throws Exception {
    testTypes("var a={b:1}",
        "enum key b must be a syntactic constant");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum3
  public void testEnum3() throws Exception {
    testTypes("var a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum4
  public void testEnum4() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum5
  public void testEnum5() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: (String|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum6
  public void testEnum6() throws Exception {
    testTypes("var a={BB:1,CC:2};\nvar d;d=a.BB;",
        "assignment\n" +
        "found   : a.<number>\n" +
        "required: Array");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum7
  public void testEnum7() throws Exception {
    testTypes("var a={AA:1,BB:2,CC:3};" +
        "var b=a.D;",
        "element D does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum8
  public void testEnum8() throws Exception {
    testTypes("var a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum9
  public void testEnum9() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum10
  public void testEnum10() throws Exception {
    testTypes(
        "" +
        "goog.K = { A : 3 };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum11
  public void testEnum11() throws Exception {
    testTypes(
        "" +
        "goog.K = { 502 : 3 };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum12
  public void testEnum12() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum13
  public void testEnum13() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;",
        "incompatible enum element types\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum14
  public void testEnum14() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.FOO;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum15
  public void testEnum15() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.BAR;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum16
  public void testEnum16() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum17
  public void testEnum17() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum18
  public void testEnum18() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum19
  public void testEnum19() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: E.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum20
  public void testEnum20() throws Exception {
    testTypes(" var E = {A: 1, B: 2}; var x = []; x[E.A] = 0;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum21
  public void testEnum21() throws Exception {
    Node n = parseAndTypeCheck(
        " var E = {A : 'a', B : 'b'};\n" +
        " function f(x) { return x; }");
    Node nodeX = n.getLastChild().getLastChild().getLastChild().getLastChild();
    JSType typeE = nodeX.getJSType();
    assertFalse(typeE.isObject());
    assertFalse(typeE.isNullable());
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum22
  public void testEnum22() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum23
  public void testEnum23() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum24
  public void testEnum24() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<(Object|null)>\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum25
  public void testEnum25() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum26
  public void testEnum26() throws Exception {
    testTypes("var a = {};  a.B = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum27
  public void testEnum27() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A == x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum28
  public void testEnum28() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A.B == x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum29
  public void testEnum29() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum30
  public void testEnum30() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum31
  public void testEnum31() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: A.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum32
  public void testEnum32() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum34
  public void testEnum34() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f(x) { return x == A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum35
  public void testEnum35() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return a.b.C; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum36
  public void testEnum36() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return 1; }",
              "inconsistent return type\n" +
              "found   : number\n" +
              "required: a.b.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum37
  public void testEnum37() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.a = {};" +
        " var b = goog.a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum38
  public void testEnum38() throws Exception {
    testTypes(
        " var MyEnum = {};" +
        " function f(x) {}",
        "Parse error. Cycle detected in inheritance chain " +
        "of type MyEnum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum39
  public void testEnum39() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum40
  public void testEnum40() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum1
  public void testAliasedEnum1() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum2
  public void testAliasedEnum2() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum3
  public void testAliasedEnum3() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum4
  public void testAliasedEnum4() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum5
  public void testAliasedEnum5() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : YourEnum.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse1
  public void testBackwardsEnumUse1() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse2
  public void testBackwardsEnumUse2() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};",
        "inconsistent return type\n" +
        "found   : MyEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse3
  public void testBackwardsEnumUse3() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse4
  public void testBackwardsEnumUse4() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "inconsistent return type\n" +
        "found   : YourEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse5
  public void testBackwardsEnumUse5() throws Exception {
    testTypes(
        " function f() { return MyEnum.BAR; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsConstructor1
  public void testBackwardsConstructor1() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var Foo = function(x) {};",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsConstructor2
  public void testBackwardsConstructor2() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var YourFoo = function(x) {};" +
        "" +
        "var Foo = YourFoo;",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMinimalConstructorAnnotation
  public void testMinimalConstructorAnnotation() throws Exception {
    testTypes("function Foo(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends1
  public void testGoodExtends1() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends2
  public void testGoodExtends2() throws Exception {
    testTypes("function derived() {}\n" +
        "function base() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends3
  public void testGoodExtends3() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends4
  public void testGoodExtends4() throws Exception {
    
    
    
    Node n = parseAndTypeCheck(
        "var goog = {};\n" +
        "goog.Base = function(){};\n" +
        "goog.Derived = function(){};\n");
    Node subTypeName = n.getLastChild().getLastChild().getFirstChild();
    assertEquals("goog.Derived", subTypeName.getQualifiedName());

    FunctionType subCtorType =
        (FunctionType) subTypeName.getNext().getJSType();
    assertEquals("goog.Derived", subCtorType.getInstanceType().toString());

    JSType superType = subCtorType.getPrototype().getImplicitPrototype();
    assertEquals("goog.Base", superType.toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends5
  public void testGoodExtends5() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends6
  public void testGoodExtends6() throws Exception {
    testFunctionType(
        CLOSURE_DEFS +
        "function base() {}\n" +
        " " +
        "  base.prototype.foo = function() { return 1; };\n" +
        "function derived() {}\n" +
        "goog.inherits(derived, base);",
        "derived.superClass_.foo",
        "function (this:base): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends7
  public void testGoodExtends7() throws Exception {
    testFunctionType(
        "Function.prototype.inherits = function(x) {};" +
        "function base() {}\n" +
        "function derived() {}\n" +
        "derived.inherits(base);",
        "(new derived).constructor",
        "function (this:derived): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends8
  public void testGoodExtends8() throws Exception {
    testTypes(" function Sub() {}" +
        " function f() { return (new Sub()).foo; }" +
        " function Base() {}" +
        " Base.prototype.foo = true;",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends9
  public void testGoodExtends9() throws Exception {
    testTypes(
        " function Super() {}" +
        "Super.prototype.foo = function() {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends10
  public void testGoodExtends10() throws Exception {
    testTypes(
        " function Super() {}" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " function foo() { return new Sub(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends11
  public void testGoodExtends11() throws Exception {
    testTypes(
        " function Super() {}" +
        " Super.prototype.foo = function(x) {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        "(new Sub()).foo(0);",
        "actual parameter 1 of Super.prototype.foo " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends1
  public void testBadExtends1() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n",
        "Parse error. Unknown type not_base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends2
  public void testBadExtends2() throws Exception {
    testTypes("function base() {\n" +
        "\n" +
        "this.baseMember = new Number(4);\n" +
        "}\n" +
        "function derived() {}\n" +
        "\n" +
        "function foo(x){ }\n" +
        "var y;\n" +
        "foo(y.baseMember);\n",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends3
  public void testBadExtends3() throws Exception {
    testTypes("function base() {}",
        "@extends used without @constructor or @interface for base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLateExtends
  public void testLateExtends() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function Foo() {}\n" +
        "Foo.prototype.foo = function() {};\n" +
        "function Bar() {}\n" +
        "goog.inherits(Foo, Bar);\n",
        "Missing @extends tag on type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatch
  public void testSuperclassMatch() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatchWithMixin
  public void testSuperclassMatchWithMixin() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Baz = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.mixin = function(y){};" +
        "Bar.inherits(Foo);\n" +
        "Bar.mixin(Baz);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch1
  public void testSuperclassMismatch1() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch2
  public void testSuperclassMismatch2() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function(){};\n" +
        " var Bar = function(){};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass1
  public void testSuperClassDefinedAfterSubClass1() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " function Base() {}" +
        " " +
        "function foo(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass2
  public void testSuperClassDefinedAfterSubClass2() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " " +
        "function foo(x) { return x; }" +
        " function Base() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment1
  public void testDirectPrototypeAssignment1() throws Exception {
    testTypes(
        " function Base() {}" +
        "Base.prototype.foo = 3;" +
        " function A() {}" +
        "A.prototype = new Base();" +
        " function foo() { return (new A).foo; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment2
  public void testDirectPrototypeAssignment2() throws Exception {
    
    
    testTypes(
        " function Base() {}" +
        " function A() {}" +
        "A.prototype = new Base();" +
        "A.prototype.foo = 3;" +
        " function foo() { return (new Base).foo; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements1
  public void testGoodImplements1() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements2
  public void testGoodImplements2() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements3
  public void testGoodImplements3() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements1
  public void testBadImplements1() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}",
        "Parse error. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements2
  public void testBadImplements2() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "@implements used without @constructor or @interface for f");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends1
  public void testBadInterfaceExtends1() throws Exception {
    testTypes("function A() {}",
        "Parse error. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends2
  public void testBadInterfaceExtends2() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends3
  public void testBadInterfaceExtends3() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends4
  public void testBadInterfaceExtends4() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends5
  public void testBadInterfaceExtends5() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsAConstructor
  public void testBadImplementsAConstructor() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsNonInterfaceType
  public void testBadImplementsNonInterfaceType() throws Exception {
    testTypes("function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsNonObjectType
  public void testBadImplementsNonObjectType() throws Exception {
    testTypes("function S() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment1
  public void testInterfaceAssignment1() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment2
  public void testInterfaceAssignment2() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;",
        "initializing variable\n" +
        "found   : T\n" +
        "required: I");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment3
  public void testInterfaceAssignment3() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment4
  public void testInterfaceAssignment4() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment5
  public void testInterfaceAssignment5() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment6
  public void testInterfaceAssignment6() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var i1 = new T();\n" +
        "var i2 = i1;\n",
        "initializing variable\n" +
        "found   : I1\n" +
        "required: I2");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment7
  public void testInterfaceAssignment7() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n" +
        "i1 = i2;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment8
  public void testInterfaceAssignment8() throws Exception {
    testTypes("var I = function() {};\n" +
        "var i;\n" +
        "var o = i;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment9
  public void testInterfaceAssignment9() throws Exception {
    testTypes("var I = function() {};\n" +
        "function f() { return null; }\n" +
        "var i = f();\n",
        "initializing variable\n" +
        "found   : (I|null)\n" +
        "required: I");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment10
  public void testInterfaceAssignment10() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment11
  public void testInterfaceAssignment11() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2|T)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment12
  public void testInterfaceAssignment12() throws Exception {
    testTypes("var I = function() {};\n" +
              "var T1 = function() {};\n" +
              "var T2 = function() {};\n" +
              "function f() { return new T2(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment13
  public void testInterfaceAssignment13() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "function Super() {};\n" +
        "Super.prototype.foo = " +
        "function() { return new T(); };\n" +
        "function Sub() {}\n" +
        "Sub.prototype.foo = " +
        "function() { return new T(); };\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop1
  public void testGetprop1() throws Exception {
    testTypes("function foo(){foo().bar;}",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess1
  public void testArrayAccess1() throws Exception {
    testTypes("var a = []; var b = a['hi'];");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess2
  public void testArrayAccess2() throws Exception {
    testTypes("var a = []; var b = a[[1,2]];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess3
  public void testArrayAccess3() throws Exception {
    testTypes("var bar = [];" +
        "function baz(){};" +
        "var foo = bar[baz()];",
        "array access\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess4
  public void testArrayAccess4() throws Exception {
    testTypes("function foo(){};var bar = foo()[foo()];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess6
  public void testArrayAccess6() throws Exception {
    testTypes("var bar = null[1];",
        "only arrays or objects can be accessed\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess7
  public void testArrayAccess7() throws Exception {
    testTypes("var bar = void 0; bar[0];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess8
  public void testArrayAccess8() throws Exception {
    
    
    testTypes("var bar = void 0; bar[0]; bar[1];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess
  public void testPropAccess() throws Exception {
    testTypes("var f = function(x) {\n" +
        "var o = String(x);\n" +
        "if (typeof o['a'] != 'undefined') { return o['a']; }\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess2
  public void testPropAccess2() throws Exception {
    testTypes("var bar = void 0; bar.baz;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess3
  public void testPropAccess3() throws Exception {
    
    
    testTypes("var bar = void 0; bar.baz; bar.bax;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess4
  public void testPropAccess4() throws Exception {
    testTypes(" function f(x) { return x['hi']; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase1
  public void testSwitchCase1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "switch(a){case b:;}",
        "case expression doesn't match switch\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase2
  public void testSwitchCase2() throws Exception {
    testTypes("var a = null; switch (typeof a) { case 'foo': }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar1
  public void testVar1() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("var a = null");

    assertEquals(createUnionType(STRING_TYPE, NULL_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar2
  public void testVar2() throws Exception {
    testTypes(" var a = function(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar3
  public void testVar3() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = 3;");

    assertEquals(NUMBER_TYPE, p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar4
  public void testVar4() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var a = 3; a = 'string';");

    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar5
  public void testVar5() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';" +
        "var a = goog.foo;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar6
  public void testVar6() throws Exception {
    testTypes(
        "function f() {" +
        "  return function() {" +
        "    " +
        "    var a = 7;" +
        "  };" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar7
  public void testVar7() throws Exception {
    testTypes("var a, b;",
        "declaration of multiple variables with shared type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar8
  public void testVar8() throws Exception {
    testTypes("var a, b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar9
  public void testVar9() throws Exception {
    testTypes("var a;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar10
  public void testVar10() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar11
  public void testVar11() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar12
  public void testVar12() throws Exception {
    testTypes("var foo = 'abc', " +
        "bar = 5;",
        new String[] {
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date",
        "initializing variable\n" +
        "found   : number\n" +
        "required: RegExp"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar13
  public void testVar13() throws Exception {
    
    testTypes("var a,a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar14
  public void testVar14() throws Exception {
    testTypes(" function f() { var x; return x; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar15
  public void testVar15() throws Exception {
    testTypes("" +
        "function f() { var x = x || {}; return x; }",
        "inconsistent return type\n" +
        "found   : {}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign1
  public void testAssign1() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign2
  public void testAssign2() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign3
  public void testAssign3() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 4;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign4
  public void testAssign4() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssignInference
  public void testAssignInference() throws Exception {
    testTypes(
        "" +
        "function f(x) {" +
        "  var y = null;" +
        "  y = x[0];" +
        "  if (y == null) { return 4; } else { return 6; }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr1
  public void testOr1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b || undefined;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr2
  public void testOr2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b || undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr3
  public void testOr3() throws Exception {
    testTypes("var a;" +
        "var c = a || 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr4
  public void testOr4() throws Exception {
     testTypes("var x;x=null || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr5
  public void testOr5() throws Exception {
     testTypes("var x;x=undefined || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd1
  public void testAnd1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b && undefined;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd2
  public void testAnd2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b && undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd3
  public void testAnd3() throws Exception {
    testTypes("var a;" +
        "var c = a && undefined;",
        "initializing variable\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd4
  public void testAnd4() throws Exception {
    testTypes("function f(x){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(y) }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd5
  public void testAnd5() throws Exception {
    testTypes("function f(x,y){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(x, y) }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd6
  public void testAnd6() throws Exception {
    testTypes("function f(x){};\n" +
        "var x;\n" +
        "if (x && f(x)) { f(x) }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd7
  public void testAnd7() throws Exception {
    
    
    
    
    testTypes("var x; if (x && x) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHook
  public void testHook() throws Exception {
    testTypes("function foo(){ var x=foo()?a:b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType1
  public void testHookRestrictsType1() throws Exception {
    testTypes("" +
        "function f() { return null;}" +
        " var a = f();" +
        "" +
        "var b = a ? a : 'default';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType2
  public void testHookRestrictsType2() throws Exception {
    testTypes("" +
        "var a = null;" +
        "" +
        "var b = a ? null : a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType3
  public void testHookRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = (!a) ? a : null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType4
  public void testHookRestrictsType4() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a != null ? a : true;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType5
  public void testHookRestrictsType5() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? a : undefined;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType6
  public void testHookRestrictsType6() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? 5 : a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType7
  public void testHookRestrictsType7() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == undefined ? 5 : a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWhileRestrictsType1
  public void testWhileRestrictsType1() throws Exception {
    testTypes(" function g(x) {}" +
        "\n" +
        "function f(x) {\n" +
        "while (x) {\n" +
        "if (g(x)) { x = 1; }\n" +
        "x = x-1;\n}\n}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : number\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWhileRestrictsType2
  public void testWhileRestrictsType2() throws Exception {
    testTypes("\n" +
        "function f(x) {\nvar y = 0;" +
        "while (x) {\n" +
        "y = x;\n" +
        "x = x-1;\n}\n" +
        "return y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions1
  public void testHigherOrderFunctions1() throws Exception {
    testTypes(
        "var f;" +
        "f(true);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions2
  public void testHigherOrderFunctions2() throws Exception {
    testTypes(
        "var f;" +
        "var a = f();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions3
  public void testHigherOrderFunctions3() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions4
  public void testHigherOrderFunctions4() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias1
  public void testConstructorAlias1() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " Foo.prototype.bar = 3;" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return (new FooAlias()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias2
  public void testConstructorAlias2() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " FooAlias.prototype.bar = 3;" +
        " function foo() { " +
        "  return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias3
  public void testConstructorAlias3() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " Foo.prototype.bar = 3;" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return (new FooAlias()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias4
  public void testConstructorAlias4() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "var FooAlias = Foo;" +
        " FooAlias.prototype.bar = 3;" +
        " function foo() { " +
        "  return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias5
  public void testConstructorAlias5() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new Foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias6
  public void testConstructorAlias6() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias7
  public void testConstructorAlias7() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias8
  public void testConstructorAlias8() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function(x) {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias9
  public void testConstructorAlias9() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function(x) {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias10
  public void testConstructorAlias10() throws Exception {
    testTypes(
        " var Foo = function(x) {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure1
  public void testClosure1() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDef(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure2
  public void testClosure2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure3
  public void testClosure3() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDefAndNotNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure4
  public void testClosure4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDef(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure5
  public void testClosure5() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure6
  public void testClosure6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDefAndNotNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure7
  public void testClosure7() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "goog.asserts = {};" +
        " goog.asserts.assert = function(x) { return x; };" +
        " var a = foo();" +
        "" +
        "var b = goog.asserts.assert(a);",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn1
  public void testReturn1() throws Exception {
    testTypes("function foo(){ return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn2
  public void testReturn2() throws Exception {
    testTypes("function foo(){ return; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn3
  public void testReturn3() throws Exception {
    testTypes("function foo(){ return 'abc'; }",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn4
  public void testReturn4() throws Exception {
    testTypes("\n function a(){return new Array();}",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn5
  public void testReturn5() throws Exception {
    testTypes("function n(n){return};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn6
  public void testReturn6() throws Exception {
    testTypes(
        "" +
        "function a(opt_a) { return opt_a }",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn7
  public void testReturn7() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn8
  public void testReturn8() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.prototype.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn1
  public void testInferredReturn1() throws Exception {
    testTypes(
        "function f() {}  function g(x) {}" +
        "g(f());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn2
  public void testInferredReturn2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() {}; " +
        " function g(x) {}" +
        "g((new Foo()).bar());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn3
  public void testInferredReturn3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() {}; " +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.bar = function() { return 3; }; ",
        "mismatch of the bar property type and the type of the property " +
        "it overrides from superclass Foo\n" +
        "original: function (this:Foo): undefined\n" +
        "override: function (this:SubFoo): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn4
  public void testInferredReturn4() throws Exception {
    
    
    
    testTypes(
        "var x = function() {};" +
        "x =  (function() { return 3; });",
        "assignment\n" +
        "found   : function (): number\n" +
        "required: function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn5
  public void testInferredReturn5() throws Exception {
    
    testTypes(
        "" +
        "function f() {" +
        "  var x = function() {};" +
        "  x =  (function() { return 3; });" +
        "  return x();" +
        "}",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn6
  public void testInferredReturn6() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "  var x = function() {};" +
        "  if (f()) " +
        "    x =  " +
        "        (function() { return 3; });" +
        "  return x();" +
        "}",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn7
  public void testInferredReturn7() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        "Foo.prototype.bar = function(x) { return 3; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn8
  public void testInferredReturn8() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam1
  public void testInferredParam1() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function f(x) {}" +
        "Foo.prototype.bar = function(y) { f(y); };",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam2
  public void testInferredParam2() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { f(x); }",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam3
  public void testInferredParam3() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { f(x); }; (new SubFoo()).bar();",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam4
  public void testInferredParam4() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { f(x); }; (new SubFoo()).bar();",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam5
  public void testInferredParam5() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.bar = " +
        "    function(x, y) { f(x); }; (new SubFoo()).bar();",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam6
  public void testInferredParam6() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.bar = " +
        "    function(x, y) { f(y); };",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenReturn1
  public void testOverriddenReturn1() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = " +
        "    function() { return {}; };" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function() { return new Foo(); }",
        "inconsistent return type\n" +
        "found   : Foo\n" +
        "required: (SubFoo|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenReturn2
  public void testOverriddenReturn2() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = " +
        "    function() { return new SubFoo(); };" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function() { return new SubFoo(); }",
        "mismatch of the bar property type and the type of the " +
        "property it overrides from superclass Foo\n" +
        "original: function (this:Foo): (SubFoo|null)\n" +
        "override: function (this:SubFoo): (Foo|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis1
  public void testThis1() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){};" +
        "goog.A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis2
  public void testThis2() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "" +
        "goog.A.prototype.n = function() { return this.foo };",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis3
  public void testThis3() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "  this.foo = 5;" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis4
  public void testThis4() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "goog.A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis5
  public void testThis5() throws Exception {
    testTypes("function h() { return this }",
        "inconsistent return type\n" +
        "found   : Date\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis6
  public void testThis6() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.A = function(){ return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis7
  public void testThis7() throws Exception {
    testTypes("function A(){};" +
        "A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis8
  public void testThis8() throws Exception {
    testTypes("function A(){" +
        "  this.foo = null;" +
        "};" +
        "A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis9
  public void testThis9() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        " A.bar = function() { return this.foo; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis10
  public void testThis10() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        "" +
        "A.bar = function() { return this.foo; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis1
  public void testGlobalThis1() throws Exception {
    testTypes(" function Window() {}" +
        " " +
        "Window.prototype.alert = function(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of Window.prototype.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis2
  public void testGlobalThis2() throws Exception {
    testTypes(" function Bindow() {}" +
        " " +
        "Bindow.prototype.alert = function(msg) {};" +
        "this.alert = 3;" +
        "(new Bindow()).alert(this.alert)",
        "actual parameter 1 of Bindow.prototype.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis3
  public void testGlobalThis3() throws Exception {
    testTypes(
        " " +
        "function alert(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of this.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis4
  public void testGlobalThis4() throws Exception {
    testTypes(
        " " +
        "var alert = function(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of this.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis5
  public void testGlobalThis5() throws Exception {
    testTypes(
        "function f() {" +
        "   " +
        "  var alert = function(msg) {};" +
        "}" +
        "this.alert(3);",
        "Property alert never defined on this");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis6
  public void testGlobalThis6() throws Exception {
    testTypes(
        " " +
        "var alert = function(msg) {};" +
        "var x = 3;" +
        "x = 'msg';" +
        "this.alert(this.x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType1
  public void testControlFlowRestrictsType1() throws Exception {
    testTypes(" function f() { return null; }" +
        " var a = f();" +
        " var b = new String('foo');" +
        " var c = null;" +
        "if (a) {" +
        "  b = a;" +
        "} else {" +
        "  c = a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType2
  public void testControlFlowRestrictsType2() throws Exception {
    testTypes(" function f() { return null; }" +
        " var a = f();" +
        " var b = 'foo';" +
        " var c = null;" +
        "if (a) {" +
        "  b = a;" +
        "} else {" +
        "  c = a;" +
        "}",
        "assignment\n" +
        "found   : (null|string)\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType3
  public void testControlFlowRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = 'foo';" +
        "if (a) {" +
        "  b = a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType4
  public void testControlFlowRestrictsType4() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "a && f(a);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType5
  public void testControlFlowRestrictsType5() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "a || f(a);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType6
  public void testControlFlowRestrictsType6() throws Exception {
    testTypes(" function f(x) {}" +
        " var a;" +
        "a && f(a);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType7
  public void testControlFlowRestrictsType7() throws Exception {
    testTypes(" function f(x) {}" +
        " var a;" +
        "a && f(a);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType8
  public void testControlFlowRestrictsType8() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "if (a || f(a)) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType9
  public void testControlFlowRestrictsType9() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || x == 1) { return 1; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase3
  public void testSwitchCase3() throws Exception {
    testTypes("" +
        "var a = new String('foo');" +
        "switch (a) { case 'A': }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase4
  public void testSwitchCase4() throws Exception {
    testTypes("" +
        "var a = 'foo';" +
        "switch (a) { case 'A':break; case null:break; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase5
  public void testSwitchCase5() throws Exception {
    testTypes("" +
        "var a = new String('foo');" +
        "switch (a) { case 'A':break; case null:break; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase6
  public void testSwitchCase6() throws Exception {
    testTypes("" +
        "var a = new Number(5);" +
        "switch (a) { case 5:break; case null:break; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase7
  public void testSwitchCase7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  switch (3) { case g(x.foo): return 3; }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase8
  public void testSwitchCase8() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  switch (g(x.foo)) { case 3: return 3; }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck1
  public void testNoTypeCheck1() throws Exception {
    testTypes("function foo() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck2
  public void testNoTypeCheck2() throws Exception {
    testTypes("var foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck3
  public void testNoTypeCheck3() throws Exception {
    testTypes("var foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck4
  public void testNoTypeCheck4() throws Exception {
    testTypes("var foo;" +
        "foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck5
  public void testNoTypeCheck5() throws Exception {
    testTypes("var foo;" +
        "foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck6
  public void testNoTypeCheck6() throws Exception {
    testTypes("var foo;" +
        "foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck7
  public void testNoTypeCheck7() throws Exception {
    testTypes("var foo;" +
        "foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck8
  public void testNoTypeCheck8() throws Exception {
    testTypes(" var foo;" +
        "var bar = 3;  function f(x) {} f(bar);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck9
  public void testNoTypeCheck9() throws Exception {
    testTypes(" function g() { }" +
        "  var a = 1",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck10
  public void testNoTypeCheck10() throws Exception {
    testTypes(" function g() { }" +
        " function h() { var a = 1}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck11
  public void testNoTypeCheck11() throws Exception {
    testTypes(" function g() { }" +
        " function h() { var a = 1}"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck12
  public void testNoTypeCheck12() throws Exception {
    testTypes(" function g() { }" +
        "function h() { var a = 1}"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck13
  public void testNoTypeCheck13() throws Exception {
    testTypes(" function g() { }" +
        "function h() { var a = 1;" +
        " var b = 1}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck14
  public void testNoTypeCheck14() throws Exception {
    testTypes(" function g() { }" +
        "g(1,2,3)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplicitCast
  public void testImplicitCast() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;",
             "(new Element).innerHTML = new Array();", null, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplicitCastSubclassAccess
  public void testImplicitCastSubclassAccess() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;" +
             "" +
             "function DIVElement() {};",
             "(new DIVElement).innerHTML = new Array();", null, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplicitCastNotInExterns
  public void testImplicitCastNotInExterns() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;" +
             "(new Element).innerHTML = new Array();",
             new String[] {
               "Illegal annotation on innerHTML. @implicitCast may only be " +
               "used in externs.",
               "assignment to property innerHTML of Element\n" +
               "found   : Array\n" +
               "required: string"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumberNode
  public void testNumberNode() throws Exception {
    Node n = typeCheck(Node.newNumber(0));

    assertEquals(NUMBER_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringNode
  public void testStringNode() throws Exception {
    Node n = typeCheck(Node.newString("hello"));

    assertEquals(STRING_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanNodeTrue
  public void testBooleanNodeTrue() throws Exception {
    Node trueNode = typeCheck(new Node(Token.TRUE));

    assertEquals(BOOLEAN_TYPE, trueNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanNodeFalse
  public void testBooleanNodeFalse() throws Exception {
    Node falseNode = typeCheck(new Node(Token.FALSE));

    assertEquals(BOOLEAN_TYPE, falseNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedNode
  public void testUndefinedNode() throws Exception {
    Node p = new Node(Token.ADD);
    Node n = Node.newString(Token.NAME, "undefined");
    p.addChildToBack(n);
    p.addChildToBack(Node.newNumber(5));
    typeCheck(p);

    assertEquals(VOID_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumberAutoboxing
  public void testNumberAutoboxing() throws Exception {
    testTypes("var a = 4;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: (Number|null)");
  }
