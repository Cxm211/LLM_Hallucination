// com/google/javascript/jscomp/CodePrinterTest.java
public void testIssue942_multipleProperties() {
    assertPrint("var x = {0: 1, 2: 3, 10: 4};", "var x={0:1,2:3,10:4}");
  }
