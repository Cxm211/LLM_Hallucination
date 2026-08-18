// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testCascadedRemovalOfUnlessUnconditonalJumps
test("function f(){return;return;}",
         "function f(){return;}");