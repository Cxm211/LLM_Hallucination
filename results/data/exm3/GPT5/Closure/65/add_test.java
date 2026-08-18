// com/google/javascript/jscomp/CodePrinterTest.java::testZero
public void testZero_multiple() {
    assertPrint("var x ='\\u0000\\u0000';", "var x=\"\\000\\000\"");
  }