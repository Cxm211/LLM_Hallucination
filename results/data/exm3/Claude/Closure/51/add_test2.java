// com/google/javascript/jscomp/CodePrinterTest.java
public void testLargeNumberExponentFormat() {
  assertPrint("var x = 100000;", "var x=1E5");
}