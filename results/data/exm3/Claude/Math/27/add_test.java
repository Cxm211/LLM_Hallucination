// org/apache/commons/math3/fraction/FractionTest.java
@Test
public void testPercentageValueWithLargeDenominator() {
    final int numer = 1;
    final int denom = Integer.MAX_VALUE / 99;
    final double percentage = 100.0 * ((double) numer) / denom;
    final Fraction frac = new Fraction(numer, denom);
    Assert.assertEquals(percentage, frac.percentageValue(), Math.ulp(percentage));
}