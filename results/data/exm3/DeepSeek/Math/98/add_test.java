// org/apache/commons/math/linear/BigMatrixImplTest.java
public void testOperateRowsLessThanCols() {
        BigMatrix a = new BigMatrixImpl(new BigDecimal[][] {
                { new BigDecimal(1), new BigDecimal(2), new BigDecimal(3) },
                { new BigDecimal(4), new BigDecimal(5), new BigDecimal(6) }
        }, false);
        BigDecimal[] b = a.operate(new BigDecimal[] { new BigDecimal(1), new BigDecimal(1), new BigDecimal(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals(6.0, b[0].doubleValue(), 1.0e-12);
        assertEquals(15.0, b[1].doubleValue(), 1.0e-12);
    }
