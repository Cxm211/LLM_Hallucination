// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldUnsignedRightShiftZeroBug() {
    fold("x = -3 >>> 0", "x = 4294967293");
    fold("x = -2147483648 >>> 0", "x = 2147483648");
}
