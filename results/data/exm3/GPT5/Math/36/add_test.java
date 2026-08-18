// org/apache/commons/math/fraction/BigFractionTest.java
@Test
public void testDoubleValueForPowerOfTwoRatio() {
    final BigInteger num = BigInteger.ONE.shiftLeft(2000);
    final BigInteger den = BigInteger.ONE.shiftLeft(1999);
    final BigFraction f = new BigFraction(num, den);
    Assert.assertEquals(2.0, f.doubleValue(), 0.0);
}
