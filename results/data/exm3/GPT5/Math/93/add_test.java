// org/apache/commons/math/util/MathUtilsTest.java::testFactorialOverflow
public void testFactorialOverflow() {
        try {
            MathUtils.factorial(21);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }