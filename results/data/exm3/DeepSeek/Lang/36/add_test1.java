// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testIsNumberTrailingDecimalPoint() {
        // Valid numbers with trailing decimal point
        assertTrue("isNumber(String) with trailing decimal point failed", NumberUtils.isNumber("2."));
        assertTrue("isNumber(String) with negative trailing decimal point failed", NumberUtils.isNumber("-2."));
        // Invalid: trailing decimal point without digit
        assertFalse("isNumber(String) with '.' only should be false", NumberUtils.isNumber("."));
        // Invalid: multiple decimal points
        assertFalse("isNumber(String) with multiple decimal points should be false", NumberUtils.isNumber("2.."));
        // Invalid: decimal point after exponent
        assertFalse("isNumber(String) with decimal point after exponent should be false", NumberUtils.isNumber("2E2."));
        // Valid: decimal point before exponent
        assertTrue("isNumber(String) with decimal point before exponent should be true", NumberUtils.isNumber("2.E2"));
        // Invalid: decimal point after type qualifier
        assertFalse("isNumber(String) with decimal point after type qualifier should be false", NumberUtils.isNumber("2f."));
        // Valid: hex numbers (should not be affected)
        assertTrue("isNumber(String) hex number should be true", NumberUtils.isNumber("0x2"));
        assertFalse("isNumber(String) invalid hex should be false", NumberUtils.isNumber("0x2."));
    }
