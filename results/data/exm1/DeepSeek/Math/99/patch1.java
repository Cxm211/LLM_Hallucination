public static int lcm(int a, int b) {
    if (a == 0 || b == 0) {
        return 0;
    }
    int g = gcd(a, b);
    long product = (long) a * b;
    long lcm = Math.abs(product / g);
    if (lcm > Integer.MAX_VALUE) {
        throw MathRuntimeException.createArithmeticException(
                "overflow: lcm({0}, {1}) exceeds int range",
                new Object[] { a, b });
    }
    return (int) lcm;
}