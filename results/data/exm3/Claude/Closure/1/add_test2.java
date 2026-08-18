// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testAdvancedModeRemovesUnusedParams() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function foo(a, b, c) { return a; }",
         "function foo(a) { return a; }");
  }