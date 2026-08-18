// org/apache/commons/math/fraction/BigFractionTest.java
@Test
public void testFloatValueForSmallNumeratorAndLargeDenominator() {
    final BigInteger pow100 = BigInteger.TEN.pow(100);
    final BigFraction small = new BigFraction(BigInteger.ONE, pow100);
    Assert.assertEquals(1e-100f, small.floatValue(), 0.0f);
}