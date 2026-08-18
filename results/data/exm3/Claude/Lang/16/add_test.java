// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
    public void testCreateNumberWithBothEAndUppercaseE() {
        // Test string with both 'e' and 'E' - should use first occurrence
        assertEquals("createNumber with both e and E failed", Double.valueOf(1.5e10), NumberUtils.createNumber("1.5e10"));
        assertEquals("createNumber with uppercase E failed", Double.valueOf(2.5E10), NumberUtils.createNumber("2.5E10"));
    }