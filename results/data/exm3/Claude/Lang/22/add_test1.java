// org/apache/commons/lang3/math/FractionTest.java
public void testReducedFactoryWithPowersOfTwo() {
    Fraction f = Fraction.getReducedFraction(16, 64);
    assertEquals(1, f.getNumerator());
    assertEquals(4, f.getDenominator());
    
    f = Fraction.getReducedFraction(128, 256);
    assertEquals(1, f.getNumerator());
    assertEquals(2, f.getDenominator());
    
    f = Fraction.getReducedFraction(-32, 128);
    assertEquals(-1, f.getNumerator());
    assertEquals(4, f.getDenominator());
}