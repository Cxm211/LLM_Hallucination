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
        int m = Math.min(k, n - k);
        long result = 1L;
        long numerator;
        long denominator;
        for (int i = 1; i <= m; i++) {
            numerator = (long) n - m + i;
            denominator = i;
            long d = java.math.BigInteger.valueOf(numerator).gcd(java.math.BigInteger.valueOf(denominator)).longValue();
            numerator /= d;
            denominator /= d;
            d = java.math.BigInteger.valueOf(result).gcd(java.math.BigInteger.valueOf(denominator)).longValue();
            result /= d;
            denominator /= d;
            if (denominator != 1) {
                // After gcd reductions, denominator should divide result exactly after multiplication.
                // But keep it in place to avoid overflow by multiplying first, then dividing.
            }
            if (numerator != 0 && result > Long.MAX_VALUE / numerator) {
                throw new ArithmeticException(
                    "result too large to represent in a long integer");
            }
            result *= numerator;
            result /= denominator;
        }
        return result;
    }