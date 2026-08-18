// com/google/javascript/jscomp/CodePrinterTest.java
public void testZeroFollowedByNonDigit() {
  assertPrint("var x ='\\0a';", "var x=\"\\0a\"");
  assertPrint("var x ='\\0z';", "var x=\"\\0z\"");
  assertPrint("var x ='\\0 ';", "var x=\"\\0 \"");
}