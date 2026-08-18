// org/apache/commons/math/linear/CholeskyDecompositionImplTest.java
public void testPositiveDefinite2x2() throws MathException {
    CholeskyDecomposition cd = new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
            { 4, 2 },
            { 2, 3 }
    }));
    RealMatrix L = cd.getL();
    RealMatrix LT = cd.getLT();
    RealMatrix reconstructed = L.multiply(LT);
    assertEquals(4.0, reconstructed.getEntry(0, 0), 1e-10);
    assertEquals(2.0, reconstructed.getEntry(0, 1), 1e-10);
    assertEquals(2.0, reconstructed.getEntry(1, 0), 1e-10);
    assertEquals(3.0, reconstructed.getEntry(1, 1), 1e-10);
}