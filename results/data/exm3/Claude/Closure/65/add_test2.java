// com/google/javascript/jscomp/CodePrinterTest.java
public void testMultipleZeros() {
  assertPrint("var x ='\\0\\0';", "var x=\"\\0\\0\"");
  assertPrint("var x ='\\01\\02';", "var x=\"\\0001\\0002\"");
}