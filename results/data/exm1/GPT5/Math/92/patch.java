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
        int m = k;
        // use symmetry to reduce iterations
        if (m > n / 2) {
            m = n - m;
        }
        long result = 1L;
        // multiplicative formula with gcd reduction and overflow checks
        for (int i = 1; i <= m; i++) {
            long t = n - m + i; // factor to multiply
            long d = gcd(result, i);
            long rDiv = result / d;
            long iDiv = i / d;
            // check overflow for rDiv * t
            if (rDiv != 0) {
                long prod = rDiv * t;
                if (prod / t != rDiv) {
                    throw new ArithmeticException(
                        "result too large to represent in a long integer");
                }
                result = prod;
            } else {
                result = 0; // shouldn't occur for valid inputs
            }
            // divide by remaining iDiv (exact division)
            result /= iDiv;
        }
        if (result < 0) {
            throw new ArithmeticException(
                "result too large to represent in a long integer");
        }
        return result;
    }

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
        // compute from log for numerical stability then round to nearest integer
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
            return 0.0;
        }
        if ((k == 1) || (k == n - 1)) {
            return Math.log((double) n);
        }
        // use symmetry to reduce number of terms
        int m = k;
        if (m > n / 2) {
            m = n - m;
        }
        double logSum = 0.0;

        // n!/k! part: product from k+1 to n becomes from (n-m+1) to n
        for (int i = n - m + 1; i <= n; i++) {
            logSum += Math.log((double) i);
        }

        // divide by (n-k)! which is (n-m)!
        for (int i = 2; i <= n - m; i++) {
            logSum -= Math.log((double) i);
        }

        return logSum;
    }

    private static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }