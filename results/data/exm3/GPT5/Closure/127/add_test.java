// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testDontRemoveBreakInFinallyLoop() throws Exception {
    testSame("function f(){ while(true){ try{} finally {break} x(); } }");
  }