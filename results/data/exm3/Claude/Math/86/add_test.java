// org/apache/commons/math/linear/CholeskyDecompositionImplTest.java
public void testNotPositiveDefiniteDuringDecomposition() throws MathException {
    try {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2, 3 },
                { 2, 4, 5 },
                { 3, 5, 6 }
        }));
        fail("Expected NotPositiveDefiniteMatrixException");
    } catch (NotPositiveDefiniteMatrixException e) {
        // Expected
    }
}