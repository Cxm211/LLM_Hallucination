// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java::testInExpression2
inFunction(
        "var a; (a) && (a = 2)",
        "var a; a && 2");