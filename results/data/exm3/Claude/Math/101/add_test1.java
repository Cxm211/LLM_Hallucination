// org/apache/commons/math/complex/ComplexFormatAbstractTest.java
public void testImaginaryCharacterAtBoundary() {
    ParsePosition pos = new ParsePosition(0);
    assertNull(new ComplexFormat().parse("5 + 7", pos));
    assertEquals(4, pos.getErrorIndex());
}