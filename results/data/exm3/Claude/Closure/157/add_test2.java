// com/google/javascript/jscomp/CodePrinterTest.java
public void testSetterWithDecimalKey() {
    assertPrint(
      "var x = {set 1.5(y) {return 1}}",
      "var x={set \"1.5\"(y){return 1}}");
  }