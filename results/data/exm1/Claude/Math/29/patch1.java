public OpenMapRealVector ebeMultiply(RealVector v) {
    checkVectorDimensions(v.getDimension());
    OpenMapRealVector res = new OpenMapRealVector(this);
    /*
     * MATH-803: the above loop assumes that 0d * x  = 0d for any double x,
     * which allows to consider only the non-zero entries of this. However,
     * this fails if this[i] == 0d and (v[i] = NaN or v[i] = Infinity).
     *
     * These special cases are handled below.
     */
    final int n = v.getDimension();
    for (int i = 0; i < n; i++) {
        final double vi = v.getEntry(i);
        if (this.getEntry(i) == 0d && (Double.isNaN(vi) || Double.isInfinite(vi))) {
            res.setEntry(i, Double.NaN);
        }
    }
    return res;
}