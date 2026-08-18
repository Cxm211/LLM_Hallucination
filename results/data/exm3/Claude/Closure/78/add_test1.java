// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldModuloByZero() {
  fold("x = 1 % 0", "x = NaN");
  fold("x = -1 % 0", "x = NaN");
  fold("x = 0 % 0", "x = NaN");
}