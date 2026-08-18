// org/apache/commons/math3/fraction/FractionTest.java
@Test
public void testMath835Negative() {
    final int numer = -Integer.MAX_VALUE / 99;
    final int denom = 1;
    final double percentage = 100 * ((double) numer) / denom;
    final Fraction frac = new Fraction(numer, denom);
    Assert.assertEquals(percentage, frac.percentageValue(), Math.ulp(percentage));
}