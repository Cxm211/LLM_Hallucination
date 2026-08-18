// org/apache/commons/math3/linear/RectangularCholeskyDecompositionTest.java
@Test
    public void testPivotSelectionBug() {
        // Construct a positive semidefinite matrix where pivot selection matters
        RealMatrix x = MatrixUtils.createRealMatrix(new double[][] {
            {0.1, 0},
            {10, 0},
            {1, 0.5}
        });
        RealMatrix m = x.multiply(x.transpose());
        RectangularCholeskyDecomposition d = new RectangularCholeskyDecomposition(m, 1.0e-10);
        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuilt = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuilt).getNorm(), 1.0e-14);
    }
