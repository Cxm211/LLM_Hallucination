// com/google/javascript/jscomp/InlineCostEstimatorTest.java
public void testCostWithDifferentIdentifierLengths() {
  checkCost("x", "x");
  checkCost("ab", "xx");
  checkCost("abc", "xxx");
  checkCost("longIdentifierName", "xxxxxxxxxxxxxxxxxx");
  checkCost("x + y", "x+x");
  checkCost("foo + bar", "xxx+xxx");
  checkCost("a.bc", "x.xx");
  checkCost("short.veryLongPropertyName", "xxxxx.xxxxxxxxxxxxxxxxxxxx");
}