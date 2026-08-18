// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testMayBeStringLogicalOrAdd() {
    foldSame("var a = ((Math.random() ? '1' : 2) || 3) + 4;");
  }
