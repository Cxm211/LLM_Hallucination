// org/apache/commons/math/linear/ArrayRealVectorTest.java::testGetLInfNormNegativeOnly
public void testGetLInfNormNegativeOnly() {
        double[] vals = new double[] {0d, -5d, -2d};
        ArrayRealVector v = new ArrayRealVector(vals);
        assertEquals("LInf of negatives", 5d, v.getLInfNorm());
    }