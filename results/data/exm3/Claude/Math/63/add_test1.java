// org/apache/commons/math/util/MathUtilsTest.java
public void testEqualsNaN() {
    assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
    assertFalse(MathUtils.equals(Double.NaN, 1.0d));
    assertFalse(MathUtils.equals(1.0d, Double.NaN));
}