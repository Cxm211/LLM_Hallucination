// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue620_AdditionalCase1() {
  assertPrint("alert(/ / / /);", "alert(/ / / /)");
}