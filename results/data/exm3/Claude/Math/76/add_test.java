// org/apache/commons/math/linear/SingularValueSolverTest.java
@Test
    public void testTallMatrix() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 },
            { 7.0, 8.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(rm);
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertEquals(0.0, recomposed.subtract(rm).getNorm(), 1.0e-13);
    }