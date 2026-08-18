// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testCreateNumberInvalidTrailingDecimal() {
    try {
        NumberUtils.createNumber("..");
        fail("createNumber(\"..\") should throw NumberFormatException");
    } catch (NumberFormatException e) {
        // expected
    }
    assertFalse("isNumber(\"..\") should be false", NumberUtils.isNumber(".."));
    try {
        NumberUtils.createNumber("1.2.");
        fail("createNumber(\"1.2.\") should throw NumberFormatException");
    } catch (NumberFormatException e) {
        // expected
    }
    assertFalse("isNumber(\"1.2.\") should be false", NumberUtils.isNumber("1.2."));
}