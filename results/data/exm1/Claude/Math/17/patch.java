public Dfp multiply(final int x) {
        if (x >= 0 && x < 10000) {
            return multiplyFast(x);
        } else {
            return multiply(field.newDfp(x));
        }
    }