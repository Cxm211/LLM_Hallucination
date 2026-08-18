// org/apache/commons/math3/linear/RectangularCholeskyDecompositionTest.java
@Test
    public void testMultipleZeroRowsAtEnd() {
        final RealMatrix m = MatrixUtils.createRealMatrix(new double[][]{
            {0.02, 0.01, 0.0, 0.0},
            {0.01, 0.03, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0}
        });
        RealMatrix root = new RectangularCholeskyDecomposition(m, 1.0e-10).getRootMatrix();
        RealMatrix rebuiltM = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuiltM).getNorm(), 1.0e-16);
        Assert.assertEquals(2, new RectangularCholeskyDecomposition(m, 1.0e-10).getRank());
    }