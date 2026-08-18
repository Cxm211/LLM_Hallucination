// com/google/javascript/jscomp/CodePrinterTest.java
public void testZeroAtEndOfString() {
  assertPrint("var x ='\\0';", "var x=\"\\0\"");
  assertPrint("var x ='abc\\0';", "var x=\"abc\\0\"");
}