// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testVersionFlagNotSetDoesNotPrintVersion() {
    args.add("--help");
    testSame("");
    String output = new String(errReader.toByteArray());
    assertTrue(!output.contains("Closure Compiler (http://code.google.com/p/closure/compiler)") || output.contains("Usage:"));
  }