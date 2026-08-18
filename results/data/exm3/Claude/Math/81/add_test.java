// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testMath308SingleElementMatrix() {

    double[] mainTridiagonal = {5.0};
    double[] secondaryTridiagonal = {};

    double[] refEigenValues = {5.0};
    RealVector[] refEigenVectors = {
        new ArrayRealVector(new double[] {1.0})
    };

    EigenDecomposition decomposition =
        new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

    double[] eigenValues = decomposition.getRealEigenvalues();
    assertEquals(refEigenValues.length, eigenValues.length);
    for (int i = 0; i < refEigenValues.length; ++i) {
        assertEquals(refEigenValues[i], eigenValues[i], 1.0e-10);
        assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-10);
    }

}