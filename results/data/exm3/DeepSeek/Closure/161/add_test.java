// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testArrayAccessCompoundAssignment() {
  testSame("[][0] += 1;");
  testSame("[][0] -= 1;");
  testSame("[][0] *= 1;");
  testSame("[][0] /= 1;");
  testSame("[][0] %= 1;");
  testSame("[][0] <<= 1;");
  testSame("[][0] >>= 1;");
  testSame("[][0] >>>= 1;");
  testSame("[][0] &= 1;");
  testSame("[][0] ^= 1;");
  testSame("[][0] |= 1;");
}
