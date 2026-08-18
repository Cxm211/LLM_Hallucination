// com/fasterxml/jackson/databind/node/TestTreeTraversingParser.java
public void testNumberLongUnderflowAndFractional() throws IOException {
    // negative underflow for long
    final BigInteger tooSmall = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
    try (final JsonParser p = MAPPER.readTree("[ " + tooSmall + " ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(NumberType.BIG_INTEGER, p.getNumberType());
        try {
            p.getLongValue();
            fail("Expected failure for `long` underflow");
        } catch (InputCoercionException e) {
            verifyException(e, "Numeric value (" + tooSmall + ") out of range of long");
        }
    }
    // fractional double for long
    final String fractional = "3.14";
    try (final JsonParser p = MAPPER.readTree("[ " + fractional + " ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(NumberType.DOUBLE, p.getNumberType());
        try {
            p.getLongValue();
            fail("Expected failure for non-integer `double` to `long`");
        } catch (InputCoercionException e) {
            verifyException(e, "Numeric value (" + fractional + ") out of range of long");
        }
    }
}
