    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int gcd = gcd(a, b);
        int product = mulAndCheck(a / gcd, b);
        int lcm = Math.abs(product);
        if (lcm < 0) {
            throw MathRuntimeException.createArithmeticException(
                "overflow: lcm({0}, {1}) is 2^31",
                new Object[] { a, b });
        }
        return lcm;
    }