// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }