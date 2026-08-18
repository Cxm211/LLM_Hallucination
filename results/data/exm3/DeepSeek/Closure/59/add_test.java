// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCheckGlobalThisOffWithMultipleBranches() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=checkTypes");
    args.add("--language_in=ECMASCRIPT5_STRICT");
    args.add("--check_symbols=false");
    args.add("--jscomp_off=checkVariables");
    testSame("function f() { this.a = 3; }");
  }
