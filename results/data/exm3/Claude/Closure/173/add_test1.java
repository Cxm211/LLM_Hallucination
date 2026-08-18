// com/google/javascript/jscomp/CodePrinterTest.java
public void testNestedNonAssociativeOps() {
    assertPrintSame("a*(b/c)");
    assertPrintSame("a*(b%c)");
    assertPrintSame("a/(b*c)");
    assertPrintSame("a-(b+c)");
    assertPrintSame("a+(b-c)");
  }