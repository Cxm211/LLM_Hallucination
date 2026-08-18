// com/google/javascript/jscomp/CodePrinterTest.java
public void testSetterInteger() {
    assertPrint("var x = {set 2(y) {return y}}", "var x={set 2(y){return y}}");
  }
