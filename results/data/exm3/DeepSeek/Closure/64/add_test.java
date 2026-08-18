// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testES5StrictUseStrictWithDelimiter() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    args.add("--print_input_delimiter");
    args.add("--input_delimiter=// Input %num%: %name%");
    Compiler compiler = compile(new String[] {"var x = 1;", "var y = 2;", "var z = 3;"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
    assertEquals(outputSource.substring(13).indexOf("'use strict'"), -1);
  }
