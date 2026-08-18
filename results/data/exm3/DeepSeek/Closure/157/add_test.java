// com/google/javascript/jscomp/CodePrinterTest.java
public void testGetterInteger() {
    assertPrint("var x = {get 2() {return 2}}", "var x={get 2(){return 2}}");
  }
