// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testInvalidBooleanArgument() {
    lastArg = "--debug invalid";
    testSame("");
    String err = new String(errReader.toByteArray());
    assertTrue("Expected error message about invalid boolean value",
        err.contains("boolean") || err.contains("Invalid"));
  }
