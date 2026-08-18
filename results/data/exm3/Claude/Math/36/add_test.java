// org/apache/commons/math/fraction/BigFractionTest.java
@Test
public void testDoubleValueForSmallNumeratorAndLargeDenominator() {
    final BigInteger pow400 = BigInteger.TEN.pow(400);
    final BigFraction small = new BigFraction(BigInteger.ONE, pow400);
    Assert.assertEquals(1e-400, small.doubleValue(), 0.0);
}