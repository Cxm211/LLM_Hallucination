// org/apache/commons/lang3/math/FractionTest.java
public void testReducedFactoryWithOneOperand() {
    Fraction f = Fraction.getReducedFraction(1, 100);
    assertEquals(1, f.getNumerator());
    assertEquals(100, f.getDenominator());
    
    f = Fraction.getReducedFraction(100, 1);
    assertEquals(100, f.getNumerator());
    assertEquals(1, f.getDenominator());
    
    f = Fraction.getReducedFraction(-1, 50);
    assertEquals(-1, f.getNumerator());
    assertEquals(50, f.getDenominator());
}