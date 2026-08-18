// buggy function
    public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v.getEntry(iter.key()));
        }
        return res;
    }

    public OpenMapRealVector ebeDivide(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v[iter.key()]);
        }
        return res;
    }

    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }

    public OpenMapRealVector ebeMultiply(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v[iter.key()]);
        }
        return res;
    }

// trigger testcase
// org/apache/commons/math/linear/SparseRealVectorTest.java::testConcurrentModification
@Test
    public void testConcurrentModification() {
        final RealVector u = new OpenMapRealVector(3, 1e-6);
        u.setEntry(0, 1);
        u.setEntry(1, 0);
        u.setEntry(2, 2);

        final RealVector v1 = new OpenMapRealVector(3, 1e-6);
        final double[] v2 = new double[3];
        v1.setEntry(0, 0);
        v2[0] = 0;
        v1.setEntry(1, 3);
        v2[1] = 3;
        v1.setEntry(2, 0);
        v2[2] = 0;

        RealVector w;

        w = u.ebeMultiply(v1);
        w = u.ebeMultiply(v2);

        w = u.ebeDivide(v1);
        w = u.ebeDivide(v2);
    }
