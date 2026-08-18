// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDefineFlag_SingleQuoteInsideDoubleQuotes() {
    args.add("--define=BAR=\"it's\"");
    test("/** @define {string} */ var BAR = \"a\";",
         "var BAR = \"it's\";");
  }