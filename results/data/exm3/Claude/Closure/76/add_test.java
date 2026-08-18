// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testNestedShortCircuit() {
    // Test nested AND within OR
    inFunction(
        "var a; ((a = 1) && (a = 2)) || (a = 3);",
        "var a; (     1  &&      2 ) ||      3 ;");

    // Test nested OR within AND
    inFunction(
        "var a; ((a = 1) || (a = 2)) && (a = 3);",
        "var a; (     1  ||      2 ) &&      3 ;");
  }