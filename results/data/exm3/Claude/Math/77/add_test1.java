// org/apache/commons/math/linear/ArrayRealVectorTest.java
public void testGetLInfNormSingleElement() {
    double[] vec_single = {-9.0};
    ArrayRealVector v_single = new ArrayRealVector(vec_single);
    double d_getLInfNorm = v_single.getLInfNorm();
    assertEquals("LInfNorm of single negative element", 9.0, d_getLInfNorm);
}