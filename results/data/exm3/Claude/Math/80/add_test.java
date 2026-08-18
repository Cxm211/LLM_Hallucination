// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testFlipNotTriggered() {
        double[] mainTridiagonal = {
            1.0, 2.0, 3.0, 4.0, 5.0
        };
        double[] secondaryTridiagonal = {
            0.5, 0.5, 0.5, 0.5
        };
        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);
        
        double[] eigenValues = decomposition.getRealEigenvalues();
        assertNotNull(eigenValues);
        assertEquals(5, eigenValues.length);
    }