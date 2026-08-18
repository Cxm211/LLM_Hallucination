// com/google/javascript/jscomp/IntegrationTest.java
public void testCastFreeCall() {
  CompilerOptions options = createCompilerOptions();
  CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
  level.setOptionsForCompilationLevel(options);
  WarningLevel warnings = WarningLevel.DEFAULT;
  warnings.setOptionsForWarningLevel(options);

  String code = "var result = /** @type {function():number} */ (myFunc)();";
  String result = "var result=myFunc();";
  test(options, code, result);
}