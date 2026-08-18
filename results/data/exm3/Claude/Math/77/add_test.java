// org/apache/commons/math/linear/ArrayRealVectorTest.java
public void testGetLInfNormAllZeros() {
    double[] vec_zeros = {0.0, 0.0, 0.0};
    ArrayRealVector v_zeros = new ArrayRealVector(vec_zeros);
    double d_getLInfNorm = v_zeros.getLInfNorm();
    assertEquals("LInfNorm of zero vector should be 0", 0.0, d_getLInfNorm);
}