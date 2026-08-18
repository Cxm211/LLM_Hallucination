// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testEmptyObjectLiteralReassignment() {
    testLocal("var a = {x: 1, y: 2}; var b; b = a; a = {}", "var x = 1; var y = 2; var b; b = {x: x, y: y}; true");
  }