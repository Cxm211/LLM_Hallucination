// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testContinueWithNextContinue() {
    test("while (true) { continue; continue; }",
         "while (true) { }");
  }
