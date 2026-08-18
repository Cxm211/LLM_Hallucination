// org/apache/commons/math3/linear/RectangularCholeskyDecompositionTest.java
@Test
    public void testDescendingDiagonals() {
        RealMatrix base = MatrixUtils.createRealMatrix(new double[][] {
            { 0.2, 0.0, 0.0 },
            { 0.1, 0.15, 0.0 },
            { 0.05, 0.08, 0.1 }
        });

        RealMatrix m = base.multiply(base.transpose());

        RectangularCholeskyDecomposition d =
                new RectangularCholeskyDecomposition(m, 1.0e-10);

        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuiltM = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuiltM).getNorm(), 1.0e-15);
    }