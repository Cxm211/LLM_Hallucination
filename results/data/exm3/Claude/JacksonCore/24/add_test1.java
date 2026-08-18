// com/fasterxml/jackson/core/read/NumberCoercionTest.java
public void testLongBoundaryValues() throws Exception
{
    for (int mode : ALL_STREAMING_MODES) {
        JsonParser p;

        // Test Long.MAX_VALUE (should pass)
        p = createParser(mode, String.valueOf(Long.MAX_VALUE));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.MAX_VALUE, p.getLongValue());

        // Test Long.MIN_VALUE (should pass)
        p = createParser(mode, String.valueOf(Long.MIN_VALUE));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.MIN_VALUE, p.getLongValue());

        // Test BigInteger edge case: exactly at Long.MAX_VALUE + 1
        BigInteger justOverMax = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        p = createParser(mode, String.valueOf(justOverMax));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(justOverMax, p.getBigIntegerValue());
        try {
            p.getLongValue();
            fail("Should not pass");
        } catch (InputCoercionException e) {
            verifyException(e, "out of range of long");
            assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
            assertEquals(Long.TYPE, e.getTargetType());
        }

        // Test BigInteger edge case: exactly at Long.MIN_VALUE - 1
        BigInteger justUnderMin = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        p = createParser(mode, String.valueOf(justUnderMin));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(justUnderMin, p.getBigIntegerValue());
        try {
            p.getLongValue();
            fail("Should not pass");
        } catch (InputCoercionException e) {
            verifyException(e, "out of range of long");
            assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
            assertEquals(Long.TYPE, e.getTargetType());
        }
    }
}