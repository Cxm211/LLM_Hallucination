// com/google/javascript/jscomp/IntegrationTest.java
public void testIssue787_AdditionalTrigger1() {
  CompilerOptions options = createCompilerOptions();
  CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
  level.setOptionsForCompilationLevel(options);
  WarningLevel warnings = WarningLevel.DEFAULT;
  warnings.setOptionsForWarningLevel(options);

  String code = "" +
      "function test() {\n" +
      "  var x;\n" +
      "  if (cond) {\n" +
      "    x = function() {\n" +
      "      return x();\n" +
      "    };\n" +
      "  }\n" +
      "  return x;\n" +
      "}";

  String result = "" +
      "function test() {\n" +
      "  var a;\n" +
      "  cond && (a = function() {\n" +
      "    return a()\n" +
      "  });\n" +
      "  return a\n" +
      "}\n" +
      "";

  test(options, code, result);
}