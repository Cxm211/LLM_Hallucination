// org/apache/commons/math/util/MathUtilsTest.java
public void testFactorialLargeValue() {
    try {
        MathUtils.factorial(21);
        fail("Expected ArithmeticException for factorial(21)");
    } catch (ArithmeticException ex) {
        // Expected
    }
}