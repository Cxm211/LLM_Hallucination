// com/google/javascript/jscomp/CodePrinterTest.java
public void testModuloMultiplyPrecedence() {
    assertPrintSame("3*(4%3)");
    assertPrintSame("(4%3)*5");
    assertPrintSame("3/(4%3)");
    assertPrintSame("3-(4%3)");
  }