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
        double logSum = 0;
        // Use symmetry to reduce operations
        int m = Math.min(k, n - k);
        // n!/k!
        for (int i = n - m + 1; i <= n; i++) {
            logSum += Math.log((double) i);
        }
        // divide by (n-k)! which is (n-m)!
        for (int i = 2; i <= m; i++) {
            logSum -= Math.log((double) i);
        }
        return logSum;
    }