// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldComparisonNaNEquality() {
    // NaN equality with a number should not fold
    foldSame("NaN == 5");
    foldSame("NaN != 5");
    foldSame("NaN === 5");
    foldSame("NaN !== 5");
  }
