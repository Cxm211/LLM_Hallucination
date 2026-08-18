// org/apache/commons/math/util/MathUtilsTest.java
public void testGcdOverflow() {
        // Test overflow due to multiplication zero detection
        // gcd(65536, 65536) should be 65536
        assertEquals(65536, MathUtils.gcd(65536, 65536));
        // gcd(65536, 131072) should be 65536
        assertEquals(65536, MathUtils.gcd(65536, 131072));
        
        // Test zero with Integer.MIN_VALUE
        try {
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        
        // Test both arguments Integer.MIN_VALUE
        try {
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }
