// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testVersionFlagWithJs() {
    args.add("--version");
    args.add("--js");
    args.add("test.js");
    testSame("");
    String errOutput = new String(errReader.toByteArray());
    assertTrue(errOutput.contains("Closure Compiler"));
    assertTrue(errOutput.contains("Version:"));
  }
