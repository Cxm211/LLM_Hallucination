// com/fasterxml/jackson/databind/node/TestTreeTraversingParser.java
public void testNumberUnderflowIntLong() throws IOException
    {
        final long tooSmallInt = (long) Integer.MIN_VALUE - 1L;
        try (final JsonParser p = MAPPER.readTree("[ "+tooSmallInt+" ]").traverse()) {
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(NumberType.LONG, p.getNumberType());
            try {
                p.getIntValue();
                fail("Expected failure for `int` underflow");
            } catch (InputCoercionException e) {
                verifyException(e, "Numeric value ("+tooSmallInt+") out of range of int");
            }
        }

        final BigInteger tooSmallLong = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        try (final JsonParser p = MAPPER.readTree("[ "+tooSmallLong+" ]").traverse()) {
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(NumberType.BIG_INTEGER, p.getNumberType());
            try {
                p.getLongValue();
                fail("Expected failure for `long` underflow");
            } catch (InputCoercionException e) {
                verifyException(e, "Numeric value ("+tooSmallLong+") out of range of long");
            }
        }

        final String tooSmallIntFloat = "-1.0e10";
        try (final JsonParser p = MAPPER.readTree("[ "+tooSmallIntFloat+" ]").traverse()) {
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(NumberType.DOUBLE, p.getNumberType());
            try {
                p.getIntValue();
                fail("Expected failure for `int` underflow from float");
            } catch (InputCoercionException e) {
                verifyException(e, "Numeric value ("+tooSmallIntFloat+") out of range of int");
            }
        }
    }