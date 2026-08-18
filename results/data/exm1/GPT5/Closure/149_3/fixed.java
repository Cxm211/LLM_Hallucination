// ===== FIXED com.google.javascript.jscomp.AbstractCommandLineRunner :: doRun() [lines 505-543] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-149-fixed/src/com/google/javascript/jscomp/AbstractCommandLineRunner.java =====
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

// ===== FIXED com.google.javascript.jscomp.AbstractCommandLineRunner :: setRunOptions(CompilerOptions) [lines 141-180] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-149-fixed/src/com/google/javascript/jscomp/AbstractCommandLineRunner.java =====
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

    options.outputCharset = getOutputCharset();
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

// ===== FIXED com.google.javascript.jscomp.CommandLineRunner :: createOptions() [lines 455-472] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-149-fixed/src/com/google/javascript/jscomp/CommandLineRunner.java =====
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
    return options;
  }

// ===== FIXED com.google.javascript.jscomp.Compiler :: toSource(Node, SourceMap) [lines 1355-1367] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-149-fixed/src/com/google/javascript/jscomp/Compiler.java =====
  private String toSource(Node n, SourceMap sourceMap) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(options.prettyPrint);
    builder.setLineBreak(options.lineBreak);
    builder.setSourceMap(sourceMap);
    builder.setSourceMapDetailLevel(options.sourceMapDetailLevel);

    Charset charset = options.outputCharset != null ?
        Charset.forName(options.outputCharset) : null;
    builder.setOutputCharset(charset);

    return builder.build();
  }
