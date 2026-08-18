// org/apache/commons/lang/math/FractionTest.java
public void testReduceLargeNumeratorWithGcd() {
    Fraction f = Fraction.getFraction(1000, 2500);
    Fraction result = f.reduce();
    assertEquals(2, result.getNumerator());
    assertEquals(5, result.getDenominator());
}