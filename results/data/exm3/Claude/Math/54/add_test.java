// org/apache/commons/math/dfp/DfpTest.java
@Test
public void testZeroSignRoundTrip() {
    DfpField field = new DfpField(100);
    // Test round-trip conversion for negative zero
    Dfp negZero = field.newDfp(-0.0);
    double result = negZero.toDouble();
    Assert.assertEquals(-1, FastMath.copySign(1, result), MathUtils.EPSILON);
    // Test round-trip conversion for positive zero
    Dfp posZero = field.newDfp(+0.0);
    result = posZero.toDouble();
    Assert.assertEquals(+1, FastMath.copySign(1, result), MathUtils.EPSILON);
}