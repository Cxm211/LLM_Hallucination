// org/apache/commons/lang3/math/FractionTest.java
public void testReduceWithZeroNumerator() {
    Fraction f = Fraction.getFraction(0, 50);
    Fraction result = f.reduce();
    assertEquals(0, result.getNumerator());
    assertEquals(1, result.getDenominator());
    
    f = Fraction.getFraction(0, 7);
    result = f.reduce();
    assertEquals(0, result.getNumerator());
    assertEquals(1, result.getDenominator());
}