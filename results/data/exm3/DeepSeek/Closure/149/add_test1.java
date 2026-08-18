// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testCharSetExpansionWithModule() {
    args.add("--charset=UTF-8");
    args.add("--module=myModule:1");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }
