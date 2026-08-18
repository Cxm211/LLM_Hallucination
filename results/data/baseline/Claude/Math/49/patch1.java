public OpenMapRealVector ebeDivide(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v[iter.key()]);
        }
        for (int i = 0; i < v.length; i++) {
            if (v[i] == 0.0 && res.getEntry(i) == 0.0) {
                res.setEntry(i, Double.NaN);
            }
        }
        return res;
    }