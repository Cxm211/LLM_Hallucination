// org/apache/commons/math/util/MathUtilsTest.java::testLcm
public void testLcm_additional() {
        try {
            MathUtils.lcm(Integer.MIN_VALUE, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }