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
        if (k > n - k) {
            return binomialCoefficient(n, n - k);
        }
        java.math.BigInteger result = java.math.BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            result = result.multiply(java.math.BigInteger.valueOf(n - k + i));
            result = result.divide(java.math.BigInteger.valueOf(i));
        }
        if (result.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException(
                "result too large to represent in a long integer");
        }
        return result.longValue();
    }