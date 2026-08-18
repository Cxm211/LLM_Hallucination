// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue620_AdditionalCase3() {
  assertPrint("return / // /;", "return/ // /");
}