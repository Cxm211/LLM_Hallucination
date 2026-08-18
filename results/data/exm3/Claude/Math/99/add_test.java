// org/apache/commons/math/util/MathUtilsTest.java
public void testGcdAdditional1() {
    try {
        MathUtils.gcd(Integer.MIN_VALUE, 2);
        fail("expecting ArithmeticException");
    } catch (ArithmeticException expected) {
        // expected
    }
}