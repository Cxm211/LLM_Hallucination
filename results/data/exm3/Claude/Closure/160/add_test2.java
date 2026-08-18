// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCheckSymbolsWithCheckSymbolsOption() {
    args.add("--check_symbols");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }