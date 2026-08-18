// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDefineFlag_DoubleQuotedString() {
    args.add("--define=FOO=\"hello\"");
    test("/** @define {string} */ var FOO = \"a\";",
         "var FOO = \"hello\";");
  }