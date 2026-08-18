// com/google/javascript/jscomp/CommandLineRunnerTest.java::testSimpleModeLeavesUnusedParams
public void testSimpleModeLeavesUnusedParams_multiple() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    testSame("function f(a,b){return a}");
  }