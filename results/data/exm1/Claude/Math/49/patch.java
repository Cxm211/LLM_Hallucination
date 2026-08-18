public OpenMapRealVector ebeDivide(RealVector v) {
    checkVectorDimensions(v.getDimension());
    OpenMapRealVector res = new OpenMapRealVector(this);
    Iterator iter = res.entries.iterator();
    while (iter.hasNext()) {
        iter.advance();
        double vEntry = v.getEntry(iter.key());
        if (vEntry != 0.0) {
            res.setEntry(iter.key(), iter.value() / vEntry);
        } else {
            res.setEntry(iter.key(), iter.value() / vEntry);
        }
    }
    return res;
}