// com/google/javascript/jscomp/IntegrationTest.java
public void testAnnotateCallsCastOverEval() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "/** @type {Function} */ (eval)('1+1');";
    String result = "eval('1+1');";
    test(options, code, result);
  }
