final protected void setRunOptions(CompilerOptions options) throws FlagUsageException, IOException {
    DiagnosticGroups diagnosticGroups = getDiagnosticGroups();

    diagnosticGroups.setWarningLevels(options, config.jscompError, CheckLevel.ERROR);
    diagnosticGroups.setWarningLevels(options, config.jscompWarning, CheckLevel.WARNING);
    diagnosticGroups.setWarningLevels(options, config.jscompOff, CheckLevel.OFF);

    createDefineReplacements(config.define, options);

    options.setManageClosureDependencies(config.manageClosureDependencies);
    options.setDevMode(config.jscompDevMode);
    options.setCodingConvention(config.codingConvention);
    options.setSummaryDetailLevel(config.summaryDetailLevel);

    inputCharset = getInputCharset();

    if (config.jsOutputFile.length() > 0) {
      options.setJsOutputFile(config.jsOutputFile);
    }

    if (config.createSourceMap.length() > 0) {
      options.setSourceMapOutputPath(config.createSourceMap);
    }
    options.setSourceMapDetailLevel(config.sourceMapDetailLevel);

    if (!config.variableMapInputFile.equals("")) {
      options.setInputVariableMapSerialized(VariableMap.load(config.variableMapInputFile).toBytes());
    }

    if (!config.propertyMapInputFile.equals("")) {
      options.setInputPropertyMapSerialized(VariableMap.load(config.propertyMapInputFile).toBytes());
    }
  }