// com/google/javascript/jscomp/CodePrinterTest.java
public void testUnicodeBoundary() {
  assertPrint("var x ='\\x7f';", "var x=\"\\u007f\"");
  assertPrint("var x ='\\x80';", "var x=\"\\u0080\"");
}