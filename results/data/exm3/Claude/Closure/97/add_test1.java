// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldBitShiftsAdditional2() {
  fold("x = -2147483648 >>> 0", "x = 2147483648");
}