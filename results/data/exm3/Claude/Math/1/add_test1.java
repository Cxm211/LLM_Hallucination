// org/apache/commons/math3/fraction/FractionTest.java
@Test
public void testDigitLimitConvergenceEdgeCase() throws ConvergenceException {
    assertFraction(1, 3, new Fraction(0.3333333334, 10));
    assertFraction(2, 3, new Fraction(0.6666666667, 10));
    assertFraction(1, 4, new Fraction(0.2500000001, 5));
}