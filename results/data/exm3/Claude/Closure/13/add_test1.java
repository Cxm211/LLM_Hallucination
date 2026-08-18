// com/google/javascript/jscomp/IntegrationTest.java
public void testIssue787_AdditionalTrigger2() {
  CompilerOptions options = createCompilerOptions();
  CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
  level.setOptionsForCompilationLevel(options);
  WarningLevel warnings = WarningLevel.DEFAULT;
  warnings.setOptionsForWarningLevel(options);

  String code = "" +
      "function outer() {\n" +
      "  var a, b, c;\n" +
      "  if (flag1) {\n" +
      "    a = ext1;\n" +
      "    b = function() { return a(); };\n" +
      "  }\n" +
      "  if (flag2) {\n" +
      "    c = function() { return b(); };\n" +
      "  }\n" +
      "  return c;\n" +
      "}";

  String result = "" +
      "function outer() {\n" +
      "  var a, b, c;\n" +
      "  flag1 && (a = ext1, b = function() {\n" +
      "    return a()\n" +
      "  });\n" +
      "  flag2 && (c = function() {\n" +
      "    return b()\n" +
      "  });\n" +
      "  return c\n" +
      "}\n" +
      "";

  test(options, code, result);
}