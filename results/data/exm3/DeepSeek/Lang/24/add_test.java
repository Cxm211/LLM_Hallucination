// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testIsNumberDecimalLInvalid() {
        assertFalse("0.L should be invalid", NumberUtils.isNumber("0.L"));
        assertFalse(".1L should be invalid", NumberUtils.isNumber(".1L"));
        assertFalse("123.456L should be invalid", NumberUtils.isNumber("123.456L"));
        assertFalse("-123.456L should be invalid", NumberUtils.isNumber("-123.456L"));
        assertFalse("2.L should be invalid", NumberUtils.isNumber("2.L"));
        assertFalse("0.0L should be invalid", NumberUtils.isNumber("0.0L"));
    }
