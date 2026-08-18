public static double binomialCoefficientLog(final int n, final int k) {
        if (n < k) {
            throw new IllegalArgumentException(
                "must have n >= k for binomial coefficient (n,k)");
        }
        if (n < 0) {
            throw new IllegalArgumentException(
                "must have n >= 0 for binomial coefficient (n,k)");
        }
        final int k2 = Math.min(k, n - k);
        if (k2 == 0) {
            return 0;
        }
        if (k2 == 1) {
            return Math.log((double) n);
        }
        
        double logSum = 0;

        // n!/k2!
        for (int i = k2 + 1; i <= n; i++) {
            logSum += Math.log((double)i);
        }

        // divide by (n-k2)!
        for (int i = 2; i <= n - k2; i++) {
            logSum -= Math.log((double)i);
        }

        return logSum;      
    }