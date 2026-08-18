// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testDontRemoveContinueInFinallyLoop() throws Exception {
    testSame("function f(){ while(true){ try{} finally {continue} x(); } }");
  }