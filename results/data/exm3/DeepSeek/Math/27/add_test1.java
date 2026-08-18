// org/apache/commons/math3/fraction/FractionTest.java
@Test
    public void testMath835Negative() {
        final int numer = Integer.MIN_VALUE / 50;
        final int denom = 2;
        final double percentage = 100 * ((double) numer) / denom;
        final Fraction frac = new Fraction(numer, denom);
        Assert.assertEquals(percentage, frac.percentageValue(), Math.ulp(percentage));
    }
