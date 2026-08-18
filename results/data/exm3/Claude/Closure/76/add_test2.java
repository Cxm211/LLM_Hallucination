// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testMultipleShortCircuitWithReturn() {
    // Test multiple short-circuits with return
    inFunction("var a; ((a = 1) && (a = 2)) || (a = 3); return a;");

    // Test HOOK in short-circuit with return
    inFunction("var a; (a = 1) ? ((a = 2) || (a = 3)) : (a = 4); return a;");
  }