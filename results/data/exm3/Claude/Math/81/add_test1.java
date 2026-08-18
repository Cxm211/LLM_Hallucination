// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testMath308TwoElementMatrix() {

    double[] mainTridiagonal = {3.0, 7.0};
    double[] secondaryTridiagonal = {2.0};

    double[] refEigenValues = {8.236067977499790, 1.763932022500210};
    RealVector[] refEigenVectors = {
        new ArrayRealVector(new double[] {-0.525731112119134, -0.850650808352040}),
        new ArrayRealVector(new double[] {-0.850650808352040, 0.525731112119134})
    };

    EigenDecomposition decomposition =
        new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

    double[] eigenValues = decomposition.getRealEigenvalues();
    assertEquals(refEigenValues.length, eigenValues.length);
    for (int i = 0; i < refEigenValues.length; ++i) {
        assertEquals(refEigenValues[i], eigenValues[i], 1.0e-10);
        assertEquals(0, Math.abs(refEigenVectors[i].dotProduct(decomposition.getEigenvector(i))) - 1.0, 1.0e-10);
    }

}