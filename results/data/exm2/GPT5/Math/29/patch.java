    public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        for (int i = 0, dim = getDimension(); i < dim; i++) {
            res.setEntry(i, getEntry(i) / v.getEntry(i));
        }
        return res;
    }