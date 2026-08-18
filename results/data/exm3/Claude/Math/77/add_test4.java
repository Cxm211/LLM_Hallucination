// org/apache/commons/math/linear/SparseRealVectorTest.java
public void testGetLInfNormAllZerosSparse() {
    double[] vec_zeros = {0.0, 0.0, 0.0, 0.0};
    OpenMapRealVector v_zeros = new OpenMapRealVector(vec_zeros);
    double d_getLInfNorm = v_zeros.getLInfNorm();
    assertEquals("LInfNorm of zero sparse vector", 0.0, d_getLInfNorm);
}