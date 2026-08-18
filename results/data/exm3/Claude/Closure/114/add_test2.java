// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testAssignInForLoopCondition() {
  test("var x, y; for (; x = function() { y; }; ) {}",
      "var y; for (; var x = function() { y; }; ) {}");
}