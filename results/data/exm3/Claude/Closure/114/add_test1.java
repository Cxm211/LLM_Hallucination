// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testAssignInForLoopInit() {
  test("var x, y; for (x = function() { y; }; x; ) {}",
      "var y; for (var x = function() { y; }; x; ) {}");
}