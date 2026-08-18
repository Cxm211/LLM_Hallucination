// org/apache/commons/math/dfp/DfpTest.java
@Test
public void testSignedZeroUnderflow() {
    DfpField field = new DfpField(100);
    Assert.assertEquals(-1, FastMath.copySign(1, field.newDfp(-1e-400).toDouble()), MathUtils.EPSILON);
    Assert.assertEquals(+1, FastMath.copySign(1, field.newDfp(+1e-400).toDouble()), MathUtils.EPSILON);
}