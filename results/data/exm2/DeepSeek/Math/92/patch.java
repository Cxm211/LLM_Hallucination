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
        // Use symmetry for large k
        int k2 = k;
        if (k2 > n - k2) {
            k2 = n - k2;
        }
        long result = 1;
        for (int i = 1; i <= k2; i++) {
            long numerator = n - k2 + i;
            long denominator = i;
            // compute gcd
            long a = numerator;
            long b = denominator;
            while (b != 0) {
                long temp = b;
                b = a % b;
                a = temp;
            }
            long gcd = a;
            numerator /= gcd;
            denominator /= gcd;
            // denominator must divide result
            if (result % denominator != 0) {
                throw new ArithmeticException("internal error: cannot compute binomial coefficient");
            }
            long div = result / denominator;
            if (div > Long.MAX_VALUE / numerator) {
                throw new ArithmeticException("result too large to represent in a long integer");
            }
            result = div * numerator;
        }
        return result;
    }