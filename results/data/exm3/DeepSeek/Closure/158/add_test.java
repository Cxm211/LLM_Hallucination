// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testWarningGuardOrderingErrorWarning() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }
