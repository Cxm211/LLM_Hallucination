// org/apache/commons/math/linear/ArrayRealVectorTest.java
public void testGetLInfNorm() {
    // empty vector
    ArrayRealVector v0 = new ArrayRealVector(new double[]{});
    assertEquals("Empty vector L-inf norm", 0.0, v0.getLInfNorm());
    // zero vector
    ArrayRealVector vZ = new ArrayRealVector(new double[]{0,0,0});
    assertEquals("Zero vector L-inf norm", 0.0, vZ.getLInfNorm());
    // single positive
    ArrayRealVector v1 = new ArrayRealVector(new double[]{5});
    assertEquals("Single positive L-inf norm", 5.0, v1.getLInfNorm());
    // single negative
    ArrayRealVector v2 = new ArrayRealVector(new double[]{-5});
    assertEquals("Single negative L-inf norm", 5.0, v2.getLInfNorm());
    // all positive
    ArrayRealVector v3 = new ArrayRealVector(new double[]{1,2,3});
    assertEquals("All positive L-inf norm", 3.0, v3.getLInfNorm());
    // all negative
    ArrayRealVector v4 = new ArrayRealVector(new double[]{-1,-2,-3});
    assertEquals("All negative L-inf norm", 3.0, v4.getLInfNorm());
    // mixed signs, max negative
    ArrayRealVector v5 = new ArrayRealVector(new double[]{-10,5,3});
    assertEquals("Mixed signs L-inf norm", 10.0, v5.getLInfNorm());
}
