// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testSmallTridiagonalUpperSpectraLastIndex() {
        double[] mainTridiagonal = { 2.0, 3.0 };
        double[] secondaryTridiagonal = { 1.0 };
        EigenDecomposition decomposition = new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);
        double[] eigenValues = decomposition.getRealEigenvalues();
        // Expected eigenvalues of [[2,1],[1,3]] are (5 +/- sqrt(5))/2 in descending order
        assertEquals((5.0 + Math.sqrt(5.0)) / 2.0, eigenValues[0], 1e-12);
        assertEquals((5.0 - Math.sqrt(5.0)) / 2.0, eigenValues[1], 1e-12);
    }