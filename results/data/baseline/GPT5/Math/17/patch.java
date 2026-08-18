public Dfp multiply(final int x) {
    if (x >= 0) {
        return multiplyFast(x);
    } else {
        int ax = (int) Math.abs((long) x);
        return multiplyFast(ax).negate();
    }
}
