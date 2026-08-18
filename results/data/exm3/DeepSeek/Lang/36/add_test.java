// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testCreateNumberTrailingDecimalPoint() {
        // Test cases for trailing decimal point without exponent or type qualifier
        assertEquals("createNumber(String) with trailing decimal point failed", new Float("2.0"), NumberUtils.createNumber("2."));
        assertEquals("createNumber(String) with negative trailing decimal point failed", new Float("-2.0"), NumberUtils.createNumber("-2."));
        // Test case with exponent and trailing decimal point (should be handled by the digit branch)
        assertEquals("createNumber(String) with exponent and trailing decimal point failed", new Double("2.0E2"), NumberUtils.createNumber("2.E2"));
        // Test case with type qualifier and trailing decimal point (should be handled by the non-digit branch)
        assertEquals("createNumber(String) with trailing decimal point and 'f' qualifier failed", new Float("2.0"), NumberUtils.createNumber("2.f"));
        assertEquals("createNumber(String) with trailing decimal point and 'd' qualifier failed", new Double("2.0"), NumberUtils.createNumber("2.d"));
        // Test case with multiple decimal points (should throw NumberFormatException)
        try {
            NumberUtils.createNumber("2..");
            fail("Expected NumberFormatException for '2..'");
        } catch (NumberFormatException e) {
            // expected
        }
    }
