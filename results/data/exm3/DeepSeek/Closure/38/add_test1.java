// com/google/javascript/jscomp/CodePrinterTest.java
public void testLargeNegativeIntegerScientific() {
    assertPrint("-1000000000000000000", "-1E18");
}
