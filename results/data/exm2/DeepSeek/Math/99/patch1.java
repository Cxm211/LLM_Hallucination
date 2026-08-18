    public static int lcm(int a, int b) {
        if (a==0 || b==0){
            return 0;
        }
        int product = mulAndCheck(a / gcd(a, b), b);
        if (product == Integer.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(
                "overflow: lcm is 2^31",
                new Object[] { a, b });
        }
        return Math.abs(product);
    }