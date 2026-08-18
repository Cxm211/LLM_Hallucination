// org/apache/commons/math3/linear/RectangularCholeskyDecompositionTest.java
@Test
    public void testRankOne() {
        RealMatrix m = MatrixUtils.createRealMatrix(new double[][] {
            {1, 1},
            {1, 1}
        });
        RectangularCholeskyDecomposition d = new RectangularCholeskyDecomposition(m, 1.0e-10);
        Assert.assertEquals(1, d.getRank());
        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuilt = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuilt).getNorm(), 1.0e-15);
    }
