// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testMayBeStringTernaryAdd() {
    foldSame("var a = (Math.random() ? '1' : 2) + 3;");
  }
