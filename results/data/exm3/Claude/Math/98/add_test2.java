// org/apache/commons/math/linear/BigMatrixImplTest.java
public void testOperateSingleRowMatrix() {
    BigMatrix a = new BigMatrixImpl(new BigDecimal[][] {
            { new BigDecimal(5), new BigDecimal(10) }
    }, false);
    BigDecimal[] b = a.operate(new BigDecimal[] { new BigDecimal(2), new BigDecimal(3) });
    assertEquals(1, b.length);
    assertEquals(40.0, b[0].doubleValue(), 1.0e-12);
}