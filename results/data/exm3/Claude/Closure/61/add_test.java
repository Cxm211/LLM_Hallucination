// com/google/javascript/jscomp/PeepholeRemoveDeadCodeTest.java
public void testCallMathWithArgs() {
    test("Math.max(1, 2);", "");
  }