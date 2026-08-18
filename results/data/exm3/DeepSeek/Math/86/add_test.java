// org/apache/commons/math/linear/CholeskyDecompositionImplTest.java
public void testNearSymmetric() throws MathException {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 + 1.5e-15 },
            { 1.0, 1.0 }
        }));
    }
