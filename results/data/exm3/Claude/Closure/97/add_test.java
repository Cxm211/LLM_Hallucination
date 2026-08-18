// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldBitShiftsAdditional1() {
  fold("x = -2147483648 >>> 1", "x = 2147483648");
}