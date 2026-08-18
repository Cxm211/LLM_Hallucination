// org/apache/commons/math/linear/SingularValueSolverTest.java
@Test
    public void testMath320C() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(rm);
        RealMatrix recomposed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        Assert.assertEquals(0.0, recomposed.subtract(rm).getNorm(), 1.0e-14);
    }
