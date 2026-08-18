// com/google/javascript/jscomp/CodePrinterTest.java
public void testNegativeNumericKeys() {
    assertPrint("var x = {-123: 1};", "var x={-123:1}");
    assertPrint("var x = {-0: 1};", "var x={-0:1}");
    assertPrint("var x = {-0123: 1};", "var x={-83:1}");
    assertPrint("var x = {-0x10: 1};", "var x={-16:1}");
    assertPrint("var x = {-.2: 1};", "var x=\"-0.2\":1");
    assertPrint("var x = {-0.2: 1};", "var x=\"-0.2\":1");
  }
