// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCheckGlobalThisOffWithDefaultLevel() {
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }