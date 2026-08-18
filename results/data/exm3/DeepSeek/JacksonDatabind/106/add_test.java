// com/fasterxml/jackson/databind/node/TestTreeTraversingParser.java
public void testNumberIntUnderflowAndFractional() throws IOException {
    // negative underflow
    final long tooSmall = (long) Integer.MIN_VALUE - 1;
    try (final JsonParser p = MAPPER.readTree("[ " + tooSmall + " ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(NumberType.LONG, p.getNumberType());
        try {
            p.getIntValue();
            fail("Expected failure for `int` underflow");
        } catch (InputCoercionException e) {
            verifyException(e, "Numeric value (" + tooSmall + ") out of range of int");
        }
    }
    // fractional double
    final String fractional = "3.14";
    try (final JsonParser p = MAPPER.readTree("[ " + fractional + " ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(NumberType.DOUBLE, p.getNumberType());
        try {
            p.getIntValue();
            fail("Expected failure for non-integer `double` to `int`");
        } catch (InputCoercionException e) {
            verifyException(e, "Numeric value (" + fractional + ") out of range of int");
        }
    }
}
