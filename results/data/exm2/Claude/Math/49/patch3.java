public OpenMapRealVector ebeMultiply(double[] v) {
    checkVectorDimensions(v.length);
    OpenMapRealVector res = new OpenMapRealVector(this);
    Iterator iter = res.entries.iterator();
    while (iter.hasNext()) {
        iter.advance();
        double product = iter.value() * v[iter.key()];
        if (product != 0.0) {
            res.setEntry(iter.key(), product);
        } else {
            iter.remove();
        }
    }
    return res;
}