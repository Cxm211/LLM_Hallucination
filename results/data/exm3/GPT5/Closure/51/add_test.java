// com/google/javascript/jscomp/CodePrinterTest.java::testIssue582_variantNegativeZeroAfterMinus
public void testIssue582_variantNegativeZeroAfterMinus() {
    assertPrint("x=y- -0.0;", "x=y- -0.0");
  }