protected CompilerOptions createOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel level = flags.compilation_level;
    if (level == null) {
      level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    }
    level.setOptionsForCompilationLevel(options);
    if (flags.debug) {
      level.setDebugOptionsForCompilationLevel(options);
    }

    WarningLevel wLevel = flags.warning_level;
    if (wLevel == null) {
      wLevel = WarningLevel.DEFAULT;
    }
    wLevel.setOptionsForWarningLevel(options);
    for (FormattingOption formattingOption : flags.formatting) {
      formattingOption.applyToOptions(options);
    }
    if (flags.process_closure_primitives) {
      options.closurePass = true;
    }

    initOptionsFromFlags(options);
    return options;
  }