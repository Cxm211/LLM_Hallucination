// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue942_SingleDigitZero() {
  assertPrint("var x = {0: 1};", "var x={0:1}");
}