// org/apache/commons/math/util/MathUtilsTest.java
public void testLcmAdditionalOverflow() {
        // lcm(Integer.MIN_VALUE, 2) should overflow
        try {
            MathUtils.lcm(Integer.MIN_VALUE, 2);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
        // lcm(1 << 30, Integer.MIN_VALUE) should overflow
        try {
            MathUtils.lcm(1 << 30, Integer.MIN_VALUE);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }
