// com/fasterxml/jackson/core/json/TestParserNonStandard.java
public void testBigDecimalWithCharArray() throws Exception {
    char[] buffer = "  -987.654  ".toCharArray();
    try {
        BigDecimal result = NumberInput.parseBigDecimal(buffer, 0, buffer.length);
        assertEquals(new BigDecimal("-987.654"), result);
    } catch (NumberFormatException e) {
        fail("Should handle whitespace in char array BigDecimal parsing");
    }
}