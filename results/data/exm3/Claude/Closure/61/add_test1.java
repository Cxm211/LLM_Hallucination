// com/google/javascript/jscomp/PeepholeRemoveDeadCodeTest.java
public void testCallMathInExpression() {
    test("var x = (Math.abs(-5), 10);", "var x = 10;");
  }