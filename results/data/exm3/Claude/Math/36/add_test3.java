// org/apache/commons/math/fraction/BigFractionTest.java
@Test
public void testFloatValueForLargeNumeratorAndSmallDenominator() {
    final BigInteger pow100 = BigInteger.TEN.pow(100);
    final BigFraction large = new BigFraction(pow100, BigInteger.ONE);
    Assert.assertTrue(Float.isInfinite(large.floatValue()));
}