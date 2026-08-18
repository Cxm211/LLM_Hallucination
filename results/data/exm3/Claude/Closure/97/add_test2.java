// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldBitShiftsAdditional3() {
  fold("x = -2147483647 >>> 1", "x = 2147483648");
}