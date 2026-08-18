// buggy code
    public Line revert() {
        final Line reverted = new Line(zero, zero.subtract(direction));
        return reverted;
    }

