// org/apache/commons/math/linear/ArrayRealVectorTest.java
public void testGetLInfNormAllPositive() {
    double[] vec_positive = {1.5, 2.0, 0.5};
    ArrayRealVector v_positive = new ArrayRealVector(vec_positive);
    double d_getLInfNorm = v_positive.getLInfNorm();
    assertEquals("LInfNorm of all positive values", 2.0, d_getLInfNorm);
}