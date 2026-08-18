// org/apache/commons/math/util/MathUtilsTest.java
public void testGcdAdditional2() {
    try {
        MathUtils.gcd(2, Integer.MIN_VALUE);
        fail("expecting ArithmeticException");
    } catch (ArithmeticException expected) {
        // expected
    }
}