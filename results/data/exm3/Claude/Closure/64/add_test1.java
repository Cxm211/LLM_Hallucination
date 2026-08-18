// com/google/javascript/jscomp/CommandLineRunnerTest.java
  public void testES5StrictUseStrictWithEmptyFirstInput() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"", "var x = f.function"});
    String outputSource = compiler.toSource();
    int firstUseStrict = outputSource.indexOf("'use strict'");
    assertTrue("Should contain 'use strict'", firstUseStrict >= 0);
    assertEquals("Should only have one 'use strict'", -1, outputSource.substring(firstUseStrict + 12).indexOf("'use strict'"));
  }