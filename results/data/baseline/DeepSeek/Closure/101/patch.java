protected CompilerOptions createOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel level = flags.compilation_level;
    level.setOptionsForCompilationLevel(options);
    if (flags.debug != null && flags.debug) {
      level.setDebugOptionsForCompilationLevel(options);
    }

    WarningLevel wLevel = flags.warning_level;
    wLevel.setOptionsForWarningLevel(options);
    if (flags.formatting != null) {
      for (FormattingOption formattingOption : flags.formatting) {
        formattingOption.applyToOptions(options);
      }
    }
    if (Boolean.TRUE.equals(flags.process_closure_primitives)) {
      options.closurePass = true;
    }

    initOptionsFromFlags(options);
    return options;
  }