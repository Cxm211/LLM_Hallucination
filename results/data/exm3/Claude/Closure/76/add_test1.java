// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testHookWithShortCircuit() {
    // Test HOOK condition with short-circuit
    inFunction(
        "var a; ((a = 1) || (a = 2)) ? (a = 3) : (a = 4);",
        "var a; (     1  ||      2 ) ?      3  :      4 ;");

    // Test HOOK branches with AND
    inFunction(
        "var a; a ? ((a = 1) && (a = 2)) : ((a = 3) && (a = 4));",
        "var a; a ? (     1  &&      2 ) : (     3  &&      4 );");
  }