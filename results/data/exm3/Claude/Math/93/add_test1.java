// org/apache/commons/math/util/MathUtilsTest.java
public void testFactorialNegative() {
    try {
        MathUtils.factorial(-1);
        fail("Expected IllegalArgumentException for factorial(-1)");
    } catch (IllegalArgumentException ex) {
        // Expected
    }
    try {
        MathUtils.factorialDouble(-1);
        fail("Expected IllegalArgumentException for factorialDouble(-1)");
    } catch (IllegalArgumentException ex) {
        // Expected
    }
    try {
        MathUtils.factorialLog(-1);
        fail("Expected IllegalArgumentException for factorialLog(-1)");
    } catch (IllegalArgumentException ex) {
        // Expected
    }
}