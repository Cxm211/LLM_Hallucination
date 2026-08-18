// org/apache/commons/math/linear/SparseRealVectorTest.java
public void testGetLInfNorm() {
    // empty vector
    OpenMapRealVector v0 = new OpenMapRealVector(new double[]{});
    assertEquals("Empty vector L-inf norm", 0.0, v0.getLInfNorm());
    // zero vector
    OpenMapRealVector vZ = new OpenMapRealVector(new double[]{0,0,0});
    assertEquals("Zero vector L-inf norm", 0.0, vZ.getLInfNorm());
    // single positive
    OpenMapRealVector v1 = new OpenMapRealVector(new double[]{5});
    assertEquals("Single positive L-inf norm", 5.0, v1.getLInfNorm());
    // single negative
    OpenMapRealVector v2 = new OpenMapRealVector(new double[]{-5});
    assertEquals("Single negative L-inf norm", 5.0, v2.getLInfNorm());
    // all positive
    OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1,2,3});
    assertEquals("All positive L-inf norm", 3.0, v3.getLInfNorm());
    // all negative
    OpenMapRealVector v4 = new OpenMapRealVector(new double[]{-1,-2,-3});
    assertEquals("All negative L-inf norm", 3.0, v4.getLInfNorm());
    // mixed signs, max negative
    OpenMapRealVector v5 = new OpenMapRealVector(new double[]{-10,5,3});
    assertEquals("Mixed signs L-inf norm", 10.0, v5.getLInfNorm());
}
