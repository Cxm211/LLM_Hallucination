// org/apache/commons/lang3/math/FractionTest.java
public void testReducedFactoryGcdEvenMin() {
    Fraction f = Fraction.getReducedFraction(Integer.MIN_VALUE, 4);
    assertEquals(-536870912, f.getNumerator());
    assertEquals(1, f.getDenominator());
    f = Fraction.getReducedFraction(4, Integer.MIN_VALUE);
    assertEquals(-1, f.getNumerator());
    assertEquals(536870912, f.getDenominator());
}
