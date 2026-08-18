// com/google/javascript/jscomp/CodePrinterTest.java
public void testNegativeZeroWithSpace() {
    assertPrint("x - -0.0", "x- -0.0");
  }
