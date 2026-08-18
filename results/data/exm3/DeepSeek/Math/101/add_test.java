// org/apache/commons/math/complex/ComplexFormatAbstractTest.java
public void testNegativeSignMissingImaginaryCharacter() {
    ParsePosition pos = new ParsePosition(0);
    assertNull(new ComplexFormat().parse("5 - 6", pos));
    assertEquals(5, pos.getErrorIndex());
}
