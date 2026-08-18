// org/apache/commons/lang/math/FractionTest.java
public void testReduceNegativeZero() {
    Fraction f = Fraction.getFraction(0, -100);
    Fraction result = f.reduce();
    assertEquals(0, result.getNumerator());
    assertEquals(1, result.getDenominator());
    assertSame(result, Fraction.ZERO);
}