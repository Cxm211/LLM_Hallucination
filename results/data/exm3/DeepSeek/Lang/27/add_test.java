// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testCreateNumberWithBothExponentChars() {
    try {
        NumberUtils.createNumber("1.2e3E4");
        fail("Expected NumberFormatException");
    } catch (NumberFormatException e) {
        // expected
    }
}
