// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testMath308_3() {
        double[] mainTridiagonal = {0.0, 0.0, 10.0};
        double[] secondaryTridiagonal = {0.0, 1.0};
        // Eigenvalues: 0, (10 ± sqrt(104))/2
        double sqrt104 = Math.sqrt(104.0);
        double[] refEigenValues = {(10.0 + sqrt104) / 2.0, 0.0, (10.0 - sqrt104) / 2.0};
        // The decomposition returns eigenvalues in descending order.
        Arrays.sort(refEigenValues); // Now ascending
        for (int i = 0; i < refEigenValues.length / 2; ++i) {
            double temp = refEigenValues[i];
            refEigenValues[i] = refEigenValues[refEigenValues.length - 1 - i];
            refEigenValues[refEigenValues.length - 1 - i] = temp;
        }
        EigenDecomposition decomposition = new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);
        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
        }
    }
