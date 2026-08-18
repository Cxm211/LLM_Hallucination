// org/apache/commons/math/fraction/BigFractionTest.java
@Test
    public void testDoubleValueForLargeNegativeRatio() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigFraction largeNegative = new BigFraction(pow401.negate(), pow400);
        Assert.assertEquals(-10.0, largeNegative.doubleValue(), 1e-15);
    }
