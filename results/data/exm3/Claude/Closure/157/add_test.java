// com/google/javascript/jscomp/CodePrinterTest.java
public void testObjectLitNumericKeysWithDecimals() {
    assertPrint("var x={1.5:1}", "var x={\"1.5\":1}");
    assertPrint("var x={'2.5':1}", "var x={\"2.5\":1}");
    assertPrint("var x={3.0:1}", "var x={3:1}");
  }