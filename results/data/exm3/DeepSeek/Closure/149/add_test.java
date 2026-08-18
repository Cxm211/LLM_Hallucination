// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCharSetExpansionWithOutputFile() {
    args.add("--charset=UTF-8");
    args.add("--js_output_file=out.js");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }
