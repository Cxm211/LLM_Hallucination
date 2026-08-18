// org/apache/commons/math/fraction/BigFractionTest.java
@Test
public void testDoubleValueForLargeNumeratorAndSmallDenominator() {
    final BigInteger pow400 = BigInteger.TEN.pow(400);
    final BigFraction large = new BigFraction(pow400, BigInteger.ONE);
    Assert.assertTrue(Double.isInfinite(large.doubleValue()));
}