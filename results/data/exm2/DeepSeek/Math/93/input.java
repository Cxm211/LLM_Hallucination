    public static long factorial(final int n) {
        long result = Math.round(factorialDouble(n));
        if (result == Long.MAX_VALUE) {
            throw new ArithmeticException(
                    "factorial value is too large to fit in a long");
        }
        return factorials[n];
    }

    public static double factorialDouble(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n >= 0 for n!");
        }
        return Math.floor(Math.exp(factorialLog(n)) + 0.5);
    }

    public static double factorialLog(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("must have n > 0 for n!");
        }
        double logSum = 0;
        for (int i = 2; i <= n; i++) {
            logSum += Math.log((double)i);
        }
        return logSum;
    }

// trigger testcase
public void testFactorial() {
        for (int i = 1; i < 21; i++) {
            assertEquals(i + "! ", factorial(i), MathUtils.factorial(i));
            assertEquals(i + "! ", (double)factorial(i), MathUtils.factorialDouble(i), Double.MIN_VALUE);
            assertEquals(i + "! ", Math.log((double)factorial(i)), MathUtils.factorialLog(i), 10E-12);
        }
        
        assertEquals("0", 1, MathUtils.factorial(0));
        assertEquals("0", 1.0d, MathUtils.factorialDouble(0), 1E-14);
        assertEquals("0", 0.0d, MathUtils.factorialLog(0), 1E-14);
    }
