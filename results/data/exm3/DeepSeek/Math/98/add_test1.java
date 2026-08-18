// org/apache/commons/math/linear/RealMatrixImplTest.java
public void testOperateRowsLessThanCols() {
        RealMatrix a = new RealMatrixImpl(new double[][] {
                { 1, 2, 3 },
                { 4, 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals(6.0, b[0], 1.0e-12);
        assertEquals(15.0, b[1], 1.0e-12);
    }
