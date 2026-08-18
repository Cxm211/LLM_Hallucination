    public Dfp multiply(final int x) {
        String s = Integer.toString(x);
        Dfp dx = field.newDfp(s);
        return this.multiply(dx);
    }