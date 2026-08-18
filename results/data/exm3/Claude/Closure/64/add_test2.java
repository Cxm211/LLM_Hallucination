// com/google/javascript/jscomp/CommandLineRunnerTest.java
  public void testNonStrictModeNoUseStrict() {
    Compiler compiler = compile(new String[] {"var x = f.function", "var y = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("Should not contain 'use strict'", -1, outputSource.indexOf("'use strict'"));
  }