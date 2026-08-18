// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testWarningGuardOrderingErrorOff() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }
