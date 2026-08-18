// com/google/javascript/jscomp/parsing/ParserTest.java::testDestructuringAssignForbiddenObject
public void testDestructuringAssignForbiddenObject() {
  parseError("{x} = foo();",
      "destructuring assignment forbidden",
      "invalid assignment target");
}
