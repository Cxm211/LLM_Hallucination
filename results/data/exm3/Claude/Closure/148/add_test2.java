// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldTypeofName() {
    foldSame("x = typeof someVariable");
  }