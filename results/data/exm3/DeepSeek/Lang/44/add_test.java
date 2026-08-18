// org/apache/commons/lang/NumberUtilsTest.java
public void testCreateNumberExpPosBug() {
    try {
        NumberUtils.createNumber("aEe");
        fail("Expected NumberFormatException for aEe");
    } catch (NumberFormatException e) {
        // expected
    }
}
