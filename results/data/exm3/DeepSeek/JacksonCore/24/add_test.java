// com/fasterxml/jackson/core/read/NumberCoercionTest.java
public void testToIntFailingBigDecimal() throws Exception
{
    for (int mode : ALL_STREAMING_MODES) {
        JsonParser p;
        // BigDecimal out of range positive
        BigDecimal big = BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.ONE);
        p = createParser(mode, String.valueOf(big));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(big, p.getDecimalValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (InputCoercionException e) {
            verifyException(e, "out of range of int");
            assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
            assertEquals(Integer.TYPE, e.getTargetType());
        }
        // BigDecimal out of range negative
        BigDecimal small = BigDecimal.valueOf(Integer.MIN_VALUE).subtract(BigDecimal.ONE);
        p = createParser(mode, String.valueOf(small));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(small, p.getDecimalValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (InputCoercionException e) {
            verifyException(e, "out of range of int");
            assertEquals(JsonToken.VALUE_NUMBER_INT, e.getInputType());
            assertEquals(Integer.TYPE, e.getTargetType());
        }
    }
}
