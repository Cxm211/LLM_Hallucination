// com/google/javascript/jscomp/CodePrinterTest.java
  public void testNumericKeysAdditionalCoverage() {
    // Test "0" - edge case, should be a simple number
    assertPrint("var x = {0: 1};", "var x={0:1}");
    assertPrint("var x = {'0': 1};", "var x={\"0\":1}");
    
    // Test single-digit non-zero
    assertPrint("var x = {5: 1};", "var x={5:1}");
    
    // Test multi-digit without leading zeros
    assertPrint("var x = {123: 1};", "var x={123:1}");
    
    // Test string with multiple leading zeros
    assertPrint("var x = {'00': 1};", "var x={\"00\":1}");
  }