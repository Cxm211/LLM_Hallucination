// com/google/javascript/jscomp/CodePrinterTest.java
public void testNegativeZeroInExpression() {
  assertPrint("var x = 1 + -0.0;", "var x=1+-0.0");
}