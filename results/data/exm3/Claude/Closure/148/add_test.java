// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldTypeofRegex() {
    foldSame("x = typeof /regex/");
  }