// com/google/javascript/jscomp/MinimizeExitPointsTest.java::testDontRemoveBreakInTryFinally
public void testDontRemoveBreakInTryFinally() throws Exception {
    foldSame("function f() {b:try{throw 9} finally {break b} return 1;}");
    foldSame("function f(){ while(1){ try{throw 9} finally {continue} x(); } }");
  }