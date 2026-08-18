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
    int k1 = Math.min(k, n - k);
    long result = 1;
    for (int i = 1; i <= k1; i++) {
        int a = n - k1 + i;
        int b = i;
        int g = gcd(a, b);
        a /= g;
        b /= g;
        long g2 = gcd(result, b);
        result /= g2;
        b /= g2;
        if (result > Long.MAX_VALUE / a) {
            throw new ArithmeticException(
                "result too large to represent in a long integer");
        }
        result = result * a;
        result = result / b;
    }
    return result;
}