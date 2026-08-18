// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldGetElemAdditional() {
    // Test empty slot with side effect in another element
    foldSame("x = [, foo()][0]");
    // Test multiple side effects in other elements
    foldSame("x = [foo(), bar(), 0][2]");
  }
