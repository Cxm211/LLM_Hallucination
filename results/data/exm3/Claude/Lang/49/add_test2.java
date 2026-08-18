// org/apache/commons/lang/math/FractionTest.java
public void testReduceAlreadyReducedNegative() {
    Fraction f = Fraction.getFraction(-5, 7);
    Fraction result = f.reduce();
    assertEquals(-5, result.getNumerator());
    assertEquals(7, result.getDenominator());
    assertSame(f, result);
}