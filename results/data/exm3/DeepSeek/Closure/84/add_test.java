// com/google/javascript/jscomp/parsing/ParserTest.java
public void testDestructuringAssignForbidden5() {
    parseError("{x, y} = foo();",
        "destructuring assignment forbidden",
        "invalid assignment target");
  }
