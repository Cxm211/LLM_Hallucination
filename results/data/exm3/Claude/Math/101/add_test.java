// org/apache/commons/math/complex/ComplexFormatAbstractTest.java
public void testMissingImaginaryCharacterShortString() {
    ParsePosition pos = new ParsePosition(0);
    assertNull(new ComplexFormat().parse("2 - 3", pos));
    assertEquals(4, pos.getErrorIndex());
}