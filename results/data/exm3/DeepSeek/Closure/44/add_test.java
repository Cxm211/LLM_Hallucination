// com/google/javascript/jscomp/CodePrinterTest.java
public void testForwardSlashSpace() {
    assertPrint("return/x/;", "return /x/;");
  }
