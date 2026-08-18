// org/apache/commons/math3/linear/RectangularCholeskyDecompositionTest.java::testSemidefiniteRank
@Test
    public void testSemidefiniteRank() {
        // Rank-1 positive semidefinite matrix v v^T
        RealMatrix v = MatrixUtils.createRealMatrix(new double[][] {{1.0}, {2.0}, {3.0}});
        RealMatrix m = v.multiply(v.transpose());

        RectangularCholeskyDecomposition d = new RectangularCholeskyDecomposition(m, 1.0e-12);
        RealMatrix root = d.getRootMatrix();

        // Rank should be 1, so root must have exactly 1 column
        Assert.assertEquals(1, root.getColumnDimension());

        // Reconstruction must be exact
        RealMatrix rebuilt = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuilt).getNorm(), 1.0e-16);
    }