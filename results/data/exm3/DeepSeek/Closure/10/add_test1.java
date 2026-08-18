// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testMayBeStringTwoTernaryAdd() {
    foldSame("var a = (Math.random() ? '1' : 2) + (Math.random() ? '3' : 4);");
  }
