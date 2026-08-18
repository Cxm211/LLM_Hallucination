// com/google/javascript/jscomp/CodePrinterTest.java
public void testGetterWithDecimalKey() {
    assertPrint(
      "var x = {get 1.5() {return 1}}",
      "var x={get \"1.5\"(){return 1}}");
  }