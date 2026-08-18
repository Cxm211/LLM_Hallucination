// com/google/javascript/jscomp/parsing/ParserTest.java
public void testDestructuringAssignForbidden5() {
  parseError("[a, [b, c]] = foo();",
      "destructuring assignment forbidden",
      "invalid assignment target");
}