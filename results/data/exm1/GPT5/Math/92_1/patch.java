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
        for (int i = 1; i <= m; i++) {
            long n_i = n - m + i;
            long d_i = i;

            long g1 = gcd((int) n_i, (int) d_i);
            n_i /= g1;
            d_i /= g1;

            long g2 = java.math.BigInteger.valueOf(result).gcd(java.math.BigInteger.valueOf(d_i)).longValue();
            if (g2 > 1L) {
                result /= g2;
                d_i /= g2;
            }

            if (d_i != 1L) {
                // Should not happen, but guard against division issues
                // result is guaranteed to be divisible by d_i here
                result /= d_i;
                d_i = 1L;
            }

            if (n_i > 0 && result > Long.MAX_VALUE / n_i) {
                throw new ArithmeticException(
                    "result too large to represent in a long integer");
            }
            result *= n_i;
        }
        return result;
    }