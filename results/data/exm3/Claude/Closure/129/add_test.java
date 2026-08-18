// com/google/javascript/jscomp/IntegrationTest.java
public void testCastEvalDirectEval() {
  CompilerOptions options = createCompilerOptions();
  CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
  level.setOptionsForCompilationLevel(options);
  WarningLevel warnings = WarningLevel.DEFAULT;
  warnings.setOptionsForWarningLevel(options);

  String code = "var x = /** @type {function()} */ (eval)('1+1');";
  String result = "var x=eval(\"1+1\");";
  test(options, code, result);
}