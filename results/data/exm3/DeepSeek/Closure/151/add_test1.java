// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testVersionFlagWithHelp() {
    args.add("--version");
    args.add("--help");
    testSame("");
    String errOutput = new String(errReader.toByteArray());
    assertTrue(errOutput.contains("Closure Compiler"));
    assertTrue(errOutput.contains("Version:"));
    assertFalse(errOutput.contains("Usage:"));
  }
