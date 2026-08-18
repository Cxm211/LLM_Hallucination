public static int lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int tmp = mulAndCheck(a / gcd(a, b), b);
        int lcm = Math.abs(tmp);
        if (lcm == Integer.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: lcm({0}, {1}) is 2^31",
                    new Object[] { a, b });
        }
        return lcm;
    }