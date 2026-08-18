// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java::testFoldBitShifts
fold("x = -2147483648 >>> 0", "x = 2147483648");