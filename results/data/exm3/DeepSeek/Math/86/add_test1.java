// org/apache/commons/math/linear/CholeskyDecompositionImplTest.java
public void testNotPositiveDefiniteDuringDecomposition() throws MathException {
        try {
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
                { 1.0, 2.0 },
                { 2.0, 1.0 }
            }));
            fail("Expected NotPositiveDefiniteMatrixException");
        } catch (NotPositiveDefiniteMatrixException e) {
            // expected
        }
    }
