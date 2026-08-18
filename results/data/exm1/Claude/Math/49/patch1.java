public OpenMapRealVector ebeDivide(double[] v) {
    checkVectorDimensions(v.length);
    OpenMapRealVector res = new OpenMapRealVector(this);
    Iterator iter = res.entries.iterator();
    while (iter.hasNext()) {
        iter.advance();
        if (v[iter.key()] != 0.0) {
            res.setEntry(iter.key(), iter.value() / v[iter.key()]);
        } else {
            res.setEntry(iter.key(), iter.value() / v[iter.key()]);
        }
    }
    return res;
}