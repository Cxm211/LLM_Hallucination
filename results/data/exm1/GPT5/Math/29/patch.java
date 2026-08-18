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
        // Handle entries where this[i] == 0 and v[i] == 0 -> NaN
        for (int i = 0; i < getDimension(); i++) {
            double vi = v.getEntry(i);
            if (vi == 0.0 && getEntry(i) == 0.0) {
                res.setEntry(i, Double.NaN);
            }
        }
        return res;
    }