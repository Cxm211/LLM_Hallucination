// org/apache/commons/math/util/MathUtilsTest.java
public void testGcdAdditional3() {
    try {
        MathUtils.gcd(Integer.MIN_VALUE, 4);
        fail("expecting ArithmeticException");
    } catch (ArithmeticException expected) {
        // expected
    }
}