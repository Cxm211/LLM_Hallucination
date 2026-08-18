// com/google/javascript/jscomp/InlineVariablesTest.java
public void testFunctionInLoopAssignment() {
    testSame(
        "var y; for (var i=0; i<10; i++) { function h() { y = 2; } h(); }");
  }
