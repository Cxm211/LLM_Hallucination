// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue620_AdditionalCase2() {
  assertPrint("var x = / /;var y = / /;", "var x=/ /;var y=/ /");
}