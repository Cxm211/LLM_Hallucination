// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue942_LeadingZero() {
  assertPrint("var x = {01: 1};", "var x={\"01\":1}");
}