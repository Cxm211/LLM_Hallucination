// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldGetElemBoundaryConditions() {
  fold("x = [100][0]", "x = 100");
  fold("x = [5,15,25,35,45][4]", "x = 45");
  fold("x = [1,2,3,4,5][5]", "",
      PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
}