// com/google/javascript/jscomp/CompilerRunnerTest.java
public void testIssue115_shadowingInNestedScope() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.SIMPLE_OPTIMIZATIONS);
    CompilerRunner.FLAG_warning_level.setForTest(
        WarningLevel.VERBOSE);
    test("function f() { " +
         "  if (true) { var arguments = 5; } " +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  if (true) { var arguments = 5; } " +
         "  return arguments[0]; " +
         "}");
  }