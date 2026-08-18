// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue942_MultiDigitNumber() {
  assertPrint("var x = {123: 1};", "var x={123:1}");
}