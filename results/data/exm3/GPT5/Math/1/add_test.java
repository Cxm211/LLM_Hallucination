// org/apache/commons/math3/fraction/BigFractionTest.java
@Test(expected=FractionConversionException.class)
public void testDigitLimitConstructorNegativeOverflow() throws ConvergenceException {
    new BigFraction(-2147483649.0, 9);
}