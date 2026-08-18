// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCheckSymbolsNotSuppressedWhenWarningSet() {
    args.add("--jscomp_warning=undefinedVars");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }