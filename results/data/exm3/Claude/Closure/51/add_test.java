// com/google/javascript/jscomp/CodePrinterTest.java
public void testPositiveZero() {
  assertPrint("var x = 0.0;", "var x=0");
}