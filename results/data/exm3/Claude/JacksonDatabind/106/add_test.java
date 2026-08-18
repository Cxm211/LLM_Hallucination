// com/fasterxml/jackson/databind/node/TestTreeTraversingParser.java
public void testNumberOverflowIntNegative() throws IOException
{
    final long tooSmall = -1L + Integer.MIN_VALUE;
    try (final JsonParser p = MAPPER.readTree("[ "+tooSmall+" ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(NumberType.LONG, p.getNumberType());
        try {
            p.getIntValue();
            fail("Expected failure for `int` underflow");
        } catch (InputCoercionException e) {
            verifyException(e, "Numeric value ("+tooSmall+") out of range of int");
        }
    }
}