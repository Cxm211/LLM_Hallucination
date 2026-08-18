// com/google/javascript/jscomp/CommandLineRunnerTest.java::testVersionFlagWithOtherArgs
public void testVersionFlagWithOtherArgs() {
  args.add("--version");
  args.add("--js");
  args.add("var x = 1;");
  testSame("");
  assertEquals(
      0,
      new String(errReader.toByteArray()).indexOf(
          "Closure Compiler (http://code.google.com/p/closure/compiler)\n" +
          "Version: HEAD\n" +
          "Built on:"));
}