public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        final int n = getDimension();
        OpenMapRealVector res = new OpenMapRealVector(n);
        for (int i = 0; i < n; i++) {
            res.setEntry(i, getEntry(i) / v.getEntry(i));
        }
        return res;
    }