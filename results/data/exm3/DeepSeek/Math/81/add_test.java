// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testMath308_2() {
        double[] mainTridiagonal = {1.0, 10.0};
        double[] secondaryTridiagonal = {1.0};
        // Eigenvalues: (11 ± sqrt(85))/2
        double sqrt85 = Math.sqrt(85.0);
        double[] refEigenValues = {(11.0 + sqrt85) / 2.0, (11.0 - sqrt85) / 2.0};
        EigenDecomposition decomposition = new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);
        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
        }
    }
