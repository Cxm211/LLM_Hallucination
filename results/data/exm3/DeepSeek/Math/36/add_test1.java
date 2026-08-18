// org/apache/commons/math/fraction/BigFractionTest.java
@Test
    public void testFloatValueForLargeHalfRatio() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction largeHalf = new BigFraction(pow400, pow400.multiply(two));
        Assert.assertEquals(0.5f, largeHalf.floatValue(), 1e-15);
    }
