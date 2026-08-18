// org/apache/commons/math/linear/SparseRealVectorTest.java
@Test
    public void testConcurrentModificationSingleEntry() {
        final RealVector u = new OpenMapRealVector(2, 1e-6);
        u.setEntry(0, 5);
        u.setEntry(1, 0);
        final RealVector v1 = new OpenMapRealVector(2, 1e-6);
        final double[] v2 = new double[2];
        v1.setEntry(0, 0);
        v2[0] = 0;
        v1.setEntry(1, 1);
        v2[1] = 1;
        RealVector w;
        w = u.ebeMultiply(v1);
        w = u.ebeMultiply(v2);
        w = u.ebeDivide(v1);
        w = u.ebeDivide(v2);
    }
