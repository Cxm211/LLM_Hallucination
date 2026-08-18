// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldComparisonNaNRelational() {
    // NaN comparisons should not fold
    foldSame("NaN < 5");
    foldSame("NaN > 5");
    foldSame("NaN <= 5");
    foldSame("NaN >= 5");
  }
