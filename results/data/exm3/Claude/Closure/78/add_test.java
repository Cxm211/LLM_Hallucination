// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldDivideByZero() {
  fold("x = 1 / 0", "x = Infinity");
  fold("x = -1 / 0", "x = -Infinity");
  fold("x = 0 / 0", "x = NaN");
}