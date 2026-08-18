public OpenMapRealVector ebeDivide(RealVector v) {
    checkVectorDimensions(v.getDimension());
    OpenMapRealVector res = new OpenMapRealVector(this);
    Iterator iter = res.entries.iterator();
    while (iter.hasNext()) {
        iter.advance();
        double val = v.getEntry(iter.key());
        if (val != 0.0) {
            res.setEntry(iter.key(), iter.value() / val);
        } else {
            res.setEntry(iter.key(), Double.NaN);
        }
    }
    return res;
}