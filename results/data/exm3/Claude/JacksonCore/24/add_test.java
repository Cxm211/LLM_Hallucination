// com/fasterxml/jackson/core/json/async/AsyncNumberCoercionTest.java
public void testIntBoundaryValues() throws Exception
{
    AsyncReaderWrapper p;

    // Test Integer.MAX_VALUE (should pass)
    p = createParser(String.valueOf(Integer.MAX_VALUE));
    assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(Integer.MAX_VALUE, p.getIntValue());

    // Test Integer.MIN_VALUE (should pass)
    p = createParser(String.valueOf(Integer.MIN_VALUE));
    assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(Integer.MIN_VALUE, p.getIntValue());

    // Test BigDecimal overflow to int
    p = createParser(String.valueOf(Integer.MAX_VALUE + 1L) + ".0");
    assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
    try {
        p.getIntValue();
        fail("Should not pass");
    } catch (InputCoercionException e) {
        verifyException(e, "out of range of int");
        assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
        assertEquals(Integer.TYPE, e.getTargetType());
    }

    p = createParser(String.valueOf(Integer.MIN_VALUE - 1L) + ".0");
    assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
    try {
        p.getIntValue();
        fail("Should not pass");
    } catch (InputCoercionException e) {
        verifyException(e, "out of range of int");
        assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
        assertEquals(Integer.TYPE, e.getTargetType());
    }
}