// org/apache/commons/math/util/MathUtilsTest.java
public void testEqualsDoubleNaN() {
    assertFalse(MathUtils.equals(Double.NaN, Double.NaN));
}
