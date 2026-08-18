// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldTypeofVoid() {
    fold("x = typeof void null", "x = \"undefined\"");
    fold("x = typeof void 'foo'", "x = \"undefined\"");
    fold("x = typeof void true", "x = \"undefined\"");
    foldSame("x = typeof void foo()");
  }
