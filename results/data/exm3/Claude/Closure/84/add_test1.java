// com/google/javascript/jscomp/parsing/ParserTest.java
public void testDestructuringAssignForbidden6() {
  parseError("[] = foo();",
      "destructuring assignment forbidden",
      "invalid assignment target");
}