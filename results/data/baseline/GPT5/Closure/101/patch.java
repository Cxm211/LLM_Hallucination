  protected CompilerOptions createOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel level = (flags.compilation_level != null)
        ? flags.compilation_level
        : CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    if (flags.debug) {
      level.setDebugOptionsForCompilationLevel(options);
    }

    WarningLevel wLevel = (flags.warning_level != null)
        ? flags.warning_level
        : WarningLevel.DEFAULT;
    wLevel.setOptionsForWarningLevel(options);
    if (flags.formatting != null) {
      for (FormattingOption formattingOption : flags.formatting) {
        if (formattingOption != null) {
          formattingOption.applyToOptions(options);
        }
      }
    }
    if (flags.process_closure_primitives) {
      options.closurePass = true;
    }

    initOptionsFromFlags(options);
    return options;
  }
