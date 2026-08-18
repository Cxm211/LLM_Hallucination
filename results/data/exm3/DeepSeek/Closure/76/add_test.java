// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testHookBranchSkip() {
    inFunction("var a, b; b ? (a = 1) : (a);", "var a, b; b ? 1 : (a);");
  }
