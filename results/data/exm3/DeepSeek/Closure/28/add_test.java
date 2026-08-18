// com/google/javascript/jscomp/InlineCostEstimatorTest.java
public void testCostIdentifiers() {
    checkCost("abc", "xx");
    checkCost("$", "xx");
    checkCost("_", "xx");
    checkCost("a123", "xx");
    checkCost("a.b.c", "xx.xx.xx");
  }
