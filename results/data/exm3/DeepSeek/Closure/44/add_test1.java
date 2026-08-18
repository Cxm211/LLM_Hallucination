// com/google/javascript/jscomp/CodePrinterTest.java
public void testBackslashSpace() {
    assertPrint("var\\u0061 = 1;", "var\\u0061 = 1;");
  }
