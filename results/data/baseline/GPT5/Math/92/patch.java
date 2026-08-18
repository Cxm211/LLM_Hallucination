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
        int kk = Math.min(k, n - k);
        long result = 1L;
        for (int i = 1; i <= kk; i++) {
            long num = n - kk + i;
            long den = i;
            long g = java.math.BigInteger.valueOf(num).gcd(java.math.BigInteger.valueOf(den)).longValue();
            num /= g;
            den /= g;
            long g2 = java.math.BigInteger.valueOf(result).gcd(java.math.BigInteger.valueOf(den)).longValue();
            result /= g2;
            den /= g2;
            if (den != 1L) {
                // Should not happen for binomial coefficients, but guard anyway
                // Divide any remaining denominator before multiply to avoid overflow
                if (result % den != 0) {
                    // Fallback safety (should not be needed)
                    throw new ArithmeticException("non-integer intermediate in binomial coefficient computation");
                }
                result /= den;
                den = 1L;
            }
            if (num != 0 && result > Long.MAX_VALUE / num) {
                throw new ArithmeticException(
                    "result too large to represent in a long integer");
            }
            result *= num;
        }
        return result;
    }