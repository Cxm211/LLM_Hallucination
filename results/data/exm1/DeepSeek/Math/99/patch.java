public static int gcd(final int p, final int q) {
    int u = p;
    int v = q;
    if ((u == 0) || (v == 0)) {
        if (u == 0) {
            return (v == Integer.MIN_VALUE) ? 1 << 31 : Math.abs(v);
        } else {
            return (u == Integer.MIN_VALUE) ? 1 << 31 : Math.abs(u);
        }
    }
    if (u > 0) {
        u = -u;
    }
    if (v > 0) {
        v = -v;
    }
    int k = 0;
    while ((u & 1) == 0 && (v & 1) == 0 && k < 31) {
        u /= 2;
        v /= 2;
        k++;
    }
    if (k == 31) {
        throw MathRuntimeException.createArithmeticException(
                "overflow: gcd({0}, {1}) is 2^31",
                new Object[] { p, q });
    }
    int t = ((u & 1) == 1) ? v : -(u / 2);
    do {
        while ((t & 1) == 0) {
            t /= 2;
        }
        if (t > 0) {
            u = -t;
        } else {
            v = t;
        }
        t = (v - u) / 2;
    } while (t != 0);
    long result = - (long) u * (1L << k);
    if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE) {
        throw MathRuntimeException.createArithmeticException(
                "overflow: gcd({0}, {1}) exceeds int range",
                new Object[] { p, q });
    }
    return (int) result;
}