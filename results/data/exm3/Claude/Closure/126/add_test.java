// com/google/javascript/jscomp/MinimizeExitPointsTest.java
public void testDontMinimizeExitsInTryCatchFinally() throws Exception {
    foldSame("function f(){try{if(a())return;}catch(e){}finally{b();}}");
  }