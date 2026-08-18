public static double binomialCoefficientDouble(final int n, final int k) {
        if (n < k) {
            throw new IllegalArgumentException(
                "must have n >= k for binomial coefficient (n,k)");
        }
        if (n < 0) {
            throw new IllegalArgumentException(
                "must have n >= 0 for binomial coefficient (n,k)");
        }
        if ((n == k) || (k == 0)) {
            return 1.0;
        }
        if ((k == 1) || (k == n - 1)) {
            return (double) n;
        }
        return Math.floor(Math.exp(binomialCoefficientLog(n, k)) + 0.5);
    }