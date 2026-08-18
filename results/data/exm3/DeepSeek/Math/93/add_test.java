// org/apache/commons/math/util/MathUtilsTest.java
@Test
    public void testFactorialOverflow() {
        int[] ns = {21, 1000};
        for (int n : ns) {
            try {
                MathUtils.factorial(n);
                fail("Expected ArithmeticException for n=" + n);
            } catch (ArithmeticException e) {
                // expected
            }
        }
    }
