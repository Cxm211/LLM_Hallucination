// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCharSetExpansionISO88591() {
    args.add("--charset=ISO-8859-1");
    testSame("");
    assertEquals("ISO-8859-1", lastCompiler.getOptions().outputCharset);
  }