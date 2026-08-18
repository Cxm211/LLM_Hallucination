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
      options.setOutputCharset(Charsets.US_ASCII);
    } else {
      options.setOutputCharset(inputCharset);
    }

    boolean writeOutputToFile = !options.getJsOutputFile().isEmpty();
    if (writeOutputToFile) {
      out = toWriter(options.getJsOutputFile(), inputCharset.name());
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
    if (writeOutputToFile) {
      ((Writer)out).close();
    }
    return errCode;
  }