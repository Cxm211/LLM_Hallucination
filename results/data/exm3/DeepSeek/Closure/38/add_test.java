// com/google/javascript/jscomp/CodePrinterTest.java
public void testLargePositiveIntegerScientific() {
    assertPrint("1000000000000000000", "1E18");
}
