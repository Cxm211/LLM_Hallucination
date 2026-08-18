// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldGetElemMultipleHoles() {
  fold("x = [,,,30][3]", "x = 30");
  fold("x = [,20,,40][1]", "x = 20");
  fold("x = [,,,][0]", "x = void 0");
  fold("x = [10,,,40][2]", "x = void 0");
}