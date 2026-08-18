// com/google/javascript/jscomp/CommandLineRunnerTest.java
  public void testES5StrictUseStrictSingleInput() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function"});
    String outputSource = compiler.toSource();
    assertTrue("Should start with 'use strict'", outputSource.startsWith("'use strict'"));
    assertEquals("Should only have one 'use strict'", -1, outputSource.substring(13).indexOf("'use strict'"));
  }