public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        /*
         * MATH-803: it is not sufficient to loop through non zero entries of
         * this only. Indeed, if this[i] = 0d and v[i] = 0d, then
         * this[i] / v[i] = NaN, and not 0d.
         */
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v.getEntry(iter.key()));
        }
        int dim = getDimension();
        for (int i = 0; i < dim; i++) {
            if (getEntry(i) == 0d) {
                double vi = v.getEntry(i);
                double val = 0d / vi; // ensures NaN for 0/0 and 0/NaN
                if (Double.isNaN(val)) {
                    res.setEntry(i, val);
                }
            }
        }
        return res;
    }