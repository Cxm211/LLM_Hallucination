// com/fasterxml/jackson/databind/node/TestTreeTraversingParser.java
public void testNumberOverflowLongNegative() throws IOException
{
    final BigInteger tooSmall = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
    try (final JsonParser p = MAPPER.readTree("[ "+tooSmall+" ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(NumberType.BIG_INTEGER, p.getNumberType());
        try {
            p.getLongValue();
            fail("Expected failure for `long` underflow");
        } catch (InputCoercionException e) {
            verifyException(e, "Numeric value ("+tooSmall+") out of range of long");
        }
    }
}