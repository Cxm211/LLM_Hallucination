    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        int dim = getDimension();
        for (int i = 0; i < dim; i++) {
            res.setEntry(i, getEntry(i) * v.getEntry(i));
        }
        return res;
    }