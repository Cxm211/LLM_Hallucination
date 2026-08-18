// org/apache/commons/math3/fraction/FractionTest.java
@Test
public void testPercentageValueWithBothLargeValues() {
    final int numer = Integer.MAX_VALUE / 2;
    final int denom = Integer.MAX_VALUE / 3;
    final double percentage = 100.0 * ((double) numer) / denom;
    final Fraction frac = new Fraction(numer, denom);
    Assert.assertEquals(percentage, frac.percentageValue(), Math.ulp(percentage));
}