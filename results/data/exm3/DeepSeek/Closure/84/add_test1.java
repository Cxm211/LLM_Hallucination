// com/google/javascript/jscomp/parsing/ParserTest.java
public void testDestructuringAssignForbidden6() {
    parseError("[a] = bar();",
        "destructuring assignment forbidden",
        "invalid assignment target");
  }
