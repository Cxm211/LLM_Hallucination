// com/google/javascript/jscomp/IntegrationTest.java
public void testIssueRemoval() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "function f() {\n" +
        "  var unused = 1;\n" +
        "  var used = 2;\n" +
        "  return used;\n" +
        "}";

    String result = "" +
        "function f() {\n" +
        "  var a = 2;\n" +
        "  return a;\n" +
        "}";

    test(options, code, result);
  }
