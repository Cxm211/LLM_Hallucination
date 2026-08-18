// com/google/javascript/jscomp/CodePrinterTest.java
public void testDecimalNumber() {
  assertPrint("var x = 3.14159;", "var x=3.14159");
}