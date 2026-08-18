// com/google/javascript/jscomp/IntegrationTest.java
public void testNestedCastFunctionCall() {
  CompilerOptions options = createCompilerOptions();
  CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
  level.setOptionsForCompilationLevel(options);
  WarningLevel warnings = WarningLevel.DEFAULT;
  warnings.setOptionsForWarningLevel(options);

  String code = "var result = /** @type {function():number} */ (/** @type {function():number} */ (obj.method))();";
  String result = "var result=obj.method();";
  test(options, code, result);
}