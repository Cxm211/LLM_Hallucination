// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testLargeGapEigenvalues() {
        double[] mainTridiagonal = {
            10000.0, 100.0, 50.0, 25.0, 10.0, 1.0
        };
        double[] secondaryTridiagonal = {
            -500.0, 20.0, 10.0, 5.0, 0.5
        };
        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);
        
        double[] eigenValues = decomposition.getRealEigenvalues();
        assertNotNull(eigenValues);
        assertEquals(6, eigenValues.length);
        
        for (int i = 0; i < eigenValues.length; ++i) {
            RealVector eigenvector = decomposition.getEigenvector(i);
            assertNotNull(eigenvector);
            assertEquals(6, eigenvector.getDimension());
        }
    }