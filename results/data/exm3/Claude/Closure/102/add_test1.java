// com/google/javascript/jscomp/CompilerRunnerTest.java
public void testIssue115_multipleShadowing() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.SIMPLE_OPTIMIZATIONS);
    CompilerRunner.FLAG_warning_level.setForTest(
        WarningLevel.VERBOSE);
    test("function f() { " +
         "  var arguments = 1; " +
         "  function g() { var arguments = 2; return arguments; } " +
         "  return arguments; " +
         "}",
         "function f() { " +
         "  arguments = 1; " +
         "  function g() { arguments = 2; return arguments; } " +
         "  return arguments; " +
         "}");
  }