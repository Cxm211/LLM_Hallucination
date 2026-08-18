  protected int doRun() throws FlagUsageException, IOException {
    Compiler.setLoggingLevel(Level.parse(config.loggingLevel));

    List<JSSourceFile> externsList = createExterns();
    JSSourceFile[] externs = new JSSourceFile[externsList.size()];
    externsList.toArray(externs);

    compiler = createCompiler();
    this.options = createOptions();

    JSModule[] modules = null;
    Result result;

    setRunOptions(this.options);
    if (inputCharset == Charsets.UTF_8) {
      this.options.outputCharset = Charsets.US_ASCII;
    } else {
      this.options.outputCharset = inputCharset;
    }

    boolean writeOutputToFile = this.options.jsOutputFile != null && !this.options.jsOutputFile.isEmpty();
    if (writeOutputToFile) {
      out = toWriter(this.options.jsOutputFile, inputCharset.name());
    }

    List<String> jsFiles = config.js;
    List<String> moduleSpecs = config.module;
    if (!moduleSpecs.isEmpty()) {
      modules = createJsModules(moduleSpecs, jsFiles);
      result = compiler.compile(externs, modules, this.options);
    } else {
      List<JSSourceFile> inputList = createSourceInputs(jsFiles);
      JSSourceFile[] inputs = new JSSourceFile[inputList.size()];
      inputList.toArray(inputs);
      result = compiler.compile(externs, inputs, this.options);
    }

    int errCode = processResults(result, modules, this.options);
    // Close the output if we are writing to a file.
    if (writeOutputToFile) {
      ((Writer)out).close();
    }
    return errCode;
  }