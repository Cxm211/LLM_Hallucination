// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldTypeofFunction() {
    foldSame("x = typeof function(){}");
  }