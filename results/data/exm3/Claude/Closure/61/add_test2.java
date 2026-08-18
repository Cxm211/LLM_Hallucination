// com/google/javascript/jscomp/PeepholeRemoveDeadCodeTest.java
public void testCallMathCos() {
    test("Math.cos(3.14);", "");
  }