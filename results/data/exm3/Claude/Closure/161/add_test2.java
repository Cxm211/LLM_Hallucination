// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testArrayAccessNonAssignment() {
  test("var x = [1, 2, 3][0];", "var x = 1;");
  test("var x = [1, 2, 3][1];", "var x = 2;");
}