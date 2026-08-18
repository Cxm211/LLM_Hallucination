// com/google/javascript/jscomp/CodePrinterTest.java::testZeroInMiddle
public void testZeroInMiddle() {
    assertPrint("var x='a\\0b';", "var x=\"a\\0b\"");
    assertPrint("var x='a\\x00b';", "var x=\"a\\0b\"");
    assertPrint("var x='a\\u0000b';", "var x=\"a\\0b\"");
  }