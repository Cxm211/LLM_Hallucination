public OpenMapRealVector ebeDivide(double[] v) {
    checkVectorDimensions(v.length);
    OpenMapRealVector res = new OpenMapRealVector(this);
    Iterator iter = res.entries.iterator();
    while (iter.hasNext()) {
        iter.advance();
        int key = iter.key();
        double newVal = iter.value() / v[key];
        if (newVal == 0.0) {
            iter.remove();
        } else {
            res.setEntry(key, newVal);
        }
    }
    return res;
}