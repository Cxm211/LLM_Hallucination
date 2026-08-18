// org/apache/commons/math/linear/SparseRealVectorTest.java
public void testGetLInfNormSparse() {
    double[] vec_sparse = {0.0, 0.0, 7.5, 0.0, -3.2, 0.0};
    OpenMapRealVector v_sparse = new OpenMapRealVector(vec_sparse);
    double d_getLInfNorm = v_sparse.getLInfNorm();
    assertEquals("LInfNorm of sparse vector", 7.5, d_getLInfNorm);
}