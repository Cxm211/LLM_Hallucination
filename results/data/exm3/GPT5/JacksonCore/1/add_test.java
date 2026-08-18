// com/fasterxml/jackson/core/json/TestParserNonStandard.java::testAllowNaN
public void testBigDecimalFromNaNMessage() {
        try {
            com.fasterxml.jackson.core.io.NumberInput.parseBigDecimal("NaN");
            fail("Should have failed for NaN as BigDecimal");
        } catch (NumberFormatException e) {
            assertTrue(e.getMessage().contains("Non-standard token 'NaN'"));
        }
    }