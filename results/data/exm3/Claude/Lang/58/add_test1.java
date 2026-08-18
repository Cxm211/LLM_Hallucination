// org/apache/commons/lang/math/NumberUtilsTest.java
public void testCreateNumberWithNegativeLeadingZero() {
    NumberUtils.createNumber("-0L");
    NumberUtils.createNumber("-00l");
}