// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testNoFlagsDoesNotPrintVersion() {
    testSame("");
    String output = new String(errReader.toByteArray());
    assertFalse(output.contains("Closure Compiler (http://code.google.com/p/closure/compiler)"));
  }