    public static long binomialCoefficient(final int n, final int k) {
        if (n < k) {
            throw new IllegalArgumentException(
                "must have n >= k for binomial coefficient (n,k)");
        }
        if (n < 0) {
            throw new IllegalArgumentException(
                "must have n >= 0 for binomial coefficient (n,k)");
        }
        if ((n == k) || (k == 0)) {
            return 1;
        }
        if ((k == 1) || (k == n - 1)) {
            return n;
        }
        long result = Math.round(binomialCoefficientDouble(n, k));
        if (result == Long.MAX_VALUE) {
            throw new ArithmeticException(
                "result too large to represent in a long integer");
        }
        // Use symmetry for large k
        
        // We use the formula
        // (n choose k) = n! / (n-k)! / k!
        // (n choose k) == ((n-k+1)*...*n) / (1*...*k)
        // which could be written
        // (n choose k) == (n-1 choose k-1) * n / k
            // For n <= 61, the naive implementation cannot overflow.
            // For n > 61 but n <= 66, the result cannot overflow,
            // but we must take care not to overflow intermediate values.
                // We know that (result * i) is divisible by j,
                // but (result * i) may overflow, so we split j:
                // Filter out the gcd, d, so j/d and i/d are integer.
                // result is divisible by (j/d) because (j/d)
                // is relative prime to (i/d) and is a divisor of
                // result * (i/d).
            // For n > 66, a result overflow might occur, so we check
            // the multiplication, taking care to not overflow
            // unnecessary.
        return result;
    }

    public static double binomialCoefficientDouble(final int n, final int k) {
        
  
        return Math.floor(Math.exp(binomialCoefficientLog(n, k)) + 0.5);
    }

    public static double binomialCoefficientLog(final int n, final int k) {
        if (n < k) {
            throw new IllegalArgumentException(
                "must have n >= k for binomial coefficient (n,k)");
        }
        if (n < 0) {
            throw new IllegalArgumentException(
                "must have n >= 0 for binomial coefficient (n,k)");
        }
        if ((n == k) || (k == 0)) {
            return 0;
        }
        if ((k == 1) || (k == n - 1)) {
            return Math.log((double) n);
        }
        
        /*
         * For values small enough to do exact integer computation,
         * return the log of the exact value 
         */
        
        /*
         * Return the log of binomialCoefficientDouble for values that will not
         * overflow binomialCoefficientDouble
         */
        
        /*
         * Sum logs for values that could overflow
         */
        double logSum = 0;

        // n!/k!
        for (int i = k + 1; i <= n; i++) {
            logSum += Math.log((double)i);
        }

        // divide by (n-k)!
        for (int i = 2; i <= n - k; i++) {
            logSum -= Math.log((double)i);
        }

        return logSum;      
    }

// trigger testcase
public void testBinomialCoefficientLarge() throws Exception {
        // This tests all legal and illegal values for n <= 200.
        for (int n = 0; n <= 200; n++) {
            for (int k = 0; k <= n; k++) {
                long ourResult = -1;
                long exactResult = -1;
                boolean shouldThrow = false;
                boolean didThrow = false;
                try {
                    ourResult = MathUtils.binomialCoefficient(n, k);
                } catch (ArithmeticException ex) {
                    didThrow = true;
                }
                try {
                    exactResult = binomialCoefficient(n, k);
                } catch (ArithmeticException ex) {
                    shouldThrow = true;
                }
                assertEquals(n+","+k, shouldThrow, didThrow);
                assertEquals(n+","+k, exactResult, ourResult);
                assertTrue(n+","+k, (n > 66 || !didThrow));
            }
        }

        long ourResult = MathUtils.binomialCoefficient(300, 3);
        long exactResult = binomialCoefficient(300, 3);
        assertEquals(exactResult, ourResult);

        ourResult = MathUtils.binomialCoefficient(700, 697);
        exactResult = binomialCoefficient(700, 697);
        assertEquals(exactResult, ourResult);

        // This one should throw
        try {
            MathUtils.binomialCoefficient(700, 300);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            // Expected
        }

        // Larger values cannot be computed directly by our
        // test implementation because of stack limitations,
        // so we make little jumps to fill the cache.
        for (int i = 2000; i <= 10000; i += 2000) {
            ourResult = MathUtils.binomialCoefficient(i, 3);
            exactResult = binomialCoefficient(i, 3);
            assertEquals(exactResult, ourResult);
        }

    }
