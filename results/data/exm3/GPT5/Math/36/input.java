// buggy function
    public double doubleValue() {
        double result = numerator.doubleValue() / denominator.doubleValue();
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
        return result;
    }

    public float floatValue() {
        float result = numerator.floatValue() / denominator.floatValue();
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
        return result;
    }

// trigger testcase
// org/apache/commons/math/fraction/BigFractionTest.java::testDoubleValueForLargeNumeratorAndDenominator
@Test
    public void testDoubleValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.doubleValue(), 1e-15);
    }

// org/apache/commons/math/fraction/BigFractionTest.java::testFloatValueForLargeNumeratorAndDenominator
@Test
    public void testFloatValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.floatValue(), 1e-15);
    }
