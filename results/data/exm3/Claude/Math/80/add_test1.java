// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testSmallMatrix() {
        double[] mainTridiagonal = {
            10.0, 5.0, 2.0
        };
        double[] secondaryTridiagonal = {
            3.0, 1.0
        };
        
        double[] refEigenValues = {
            11.90983, 5.0, 0.09017
        };
        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);
        
        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-3);
        }
    }