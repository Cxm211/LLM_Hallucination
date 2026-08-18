// com/google/javascript/jscomp/IntegrationTest.java
public void testIssueDeadIfRemoval() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    level.setOptionsForCompilationLevel(options);
    WarningLevel warnings = WarningLevel.DEFAULT;
    warnings.setOptionsForWarningLevel(options);

    String code = "" +
        "function h() {\n" +
        "  if (false) {\n" +
        "    something();\n" +
        "  }\n" +
        "  return 0;\n" +
        "}";

    String result = "" +
        "function h() {\n" +
        "  return 0;\n" +
        "}";

    test(options, code, result);
  }
