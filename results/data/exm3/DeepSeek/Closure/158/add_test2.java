// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testWarningGuardOrderingWarningOff() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }
