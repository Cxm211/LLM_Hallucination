// org/apache/commons/math/util/MathUtilsTest.java
public void testEqualsInfinity() {
    assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    assertTrue(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
    assertFalse(MathUtils.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
    assertFalse(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    assertFalse(MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d));
    assertFalse(MathUtils.equals(1.0d, Double.POSITIVE_INFINITY));
}