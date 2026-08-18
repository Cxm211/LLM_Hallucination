public static int lcm(int a, int b) {
        if (a==0 || b==0){
            return 0;
        }
        int lcm = Math.abs(mulAndCheck(Math.abs(a) / gcd(a, b), Math.abs(b)));
        return lcm;
    }