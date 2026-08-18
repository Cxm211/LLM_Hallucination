// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testBreakWithNextBreak() {
    test("switch (a) { case 1: break; break; }",
         "switch (a) { case 1: }");
  }
