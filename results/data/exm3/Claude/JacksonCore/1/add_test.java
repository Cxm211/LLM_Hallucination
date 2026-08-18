// com/fasterxml/jackson/core/json/TestParserNonStandard.java
public void testBigDecimalWithWhitespace() throws Exception {
    String json = "  123.456  ";
    try {
        BigDecimal result = NumberInput.parseBigDecimal(json);
        assertEquals(new BigDecimal("123.456"), result);
    } catch (NumberFormatException e) {
        fail("Should handle whitespace in BigDecimal parsing");
    }
}