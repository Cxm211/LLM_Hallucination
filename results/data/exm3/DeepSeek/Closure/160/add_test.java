// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCheckSymbolsOverrideForQuietWarning() {
    args.add("--warning_level=QUIET");
    args.add("--jscomp_warning=undefinedVars");
    testWarnings("x = 3;", VarCheck.UNDEFINED_VAR);
  }
