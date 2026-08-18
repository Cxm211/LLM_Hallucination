public static int lcm(int a, int b) {
    if (a==0 || b==0){
        return 0;
    }
    long lcm = Math.abs(((long) a) / gcd(a, b) * ((long) b));
    if (lcm > Integer.MAX_VALUE) {
        throw MathRuntimeException.createArithmeticException(
                "overflow: lcm(" + a + ", " + b + ") is " + lcm,
                new Object[] { new Integer(a), new Integer(b) });
    }
    return (int) lcm;
}