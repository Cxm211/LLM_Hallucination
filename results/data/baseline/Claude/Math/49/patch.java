public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v.getEntry(iter.key()));
        }
        for (int i = 0; i < v.getDimension(); i++) {
            if (v.getEntry(i) == 0.0 && res.getEntry(i) == 0.0) {
                res.setEntry(i, Double.NaN);
            }
        }
        return res;
    }