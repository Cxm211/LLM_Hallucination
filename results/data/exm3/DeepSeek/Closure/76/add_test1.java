// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testAndLeftOperandRead() {
    inFunction("var a; (a = 1) && (a);");
  }
