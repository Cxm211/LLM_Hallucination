// com/fasterxml/jackson/core/json/TestParserNonStandard.java
public void testBigDecimalWithEmptyString() throws Exception {
    String json = "   ";
    try {
        NumberInput.parseBigDecimal(json);
        fail("Should throw NumberFormatException for empty/whitespace-only string");
    } catch (NumberFormatException e) {
        // Expected
    }
}