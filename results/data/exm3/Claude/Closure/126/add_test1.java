// com/google/javascript/jscomp/MinimizeExitPointsTest.java
public void testDontMinimizeExitsInTryFinally() throws Exception {
    foldSame("function f(){try{if(a())return;}finally{b();}}");
  }