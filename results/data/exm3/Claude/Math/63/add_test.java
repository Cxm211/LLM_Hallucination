// org/apache/commons/math/util/MathUtilsTest.java
public void testEqualsPositiveZeroNegativeZero() {
    assertFalse(MathUtils.equals(0.0d, -0.0d));
    assertFalse(MathUtils.equals(-0.0d, 0.0d));
    assertTrue(MathUtils.equals(0.0d, 0.0d));
    assertTrue(MathUtils.equals(-0.0d, -0.0d));
}