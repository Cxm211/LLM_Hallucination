// org/apache/commons/math/linear/BigMatrixImplTest.java
public void testOperateNonSquareMatrix() {
    BigMatrix a = new BigMatrixImpl(new BigDecimal[][] {
            { new BigDecimal(2), new BigDecimal(3), new BigDecimal(4) },
            { new BigDecimal(1), new BigDecimal(0), new BigDecimal(2) }
    }, false);
    BigDecimal[] b = a.operate(new BigDecimal[] { new BigDecimal(1), new BigDecimal(2), new BigDecimal(3) });
    assertEquals(a.getRowDimension(), b.length);
    assertEquals(20.0, b[0].doubleValue(), 1.0e-12);
    assertEquals(7.0, b[1].doubleValue(), 1.0e-12);
}