// com/google/javascript/jscomp/IntegrationTest.java
public void testAnnotateCallsCastOverGet() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "/** @type {function} */ (obj['method'])();";
    String result = "obj.method();";
    test(options, code, result);
  }
