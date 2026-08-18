// org/apache/commons/lang3/math/FractionTest.java
public void testReducedFactoryGcdOverflow() {
    try {
        Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        fail("Expecting ArithmeticException");
    } catch (ArithmeticException ex) {}
}
