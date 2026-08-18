// org/apache/commons/lang/math/NumberUtilsTest.java
public void testCreateNumberWithLeadingZeroAndTypeSuffix() {
    NumberUtils.createNumber("0L");
    NumberUtils.createNumber("00L");
    NumberUtils.createNumber("000l");
}