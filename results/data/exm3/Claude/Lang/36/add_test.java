// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testCreateNumberTrailingDecimal() {
    assertEquals("createNumber(\"3.\") failed", new Float("3."), NumberUtils.createNumber("3."));
    assertEquals("createNumber(\"123.\") failed", new Float("123."), NumberUtils.createNumber("123."));
    assertEquals("createNumber(\"-5.\") failed", new Float("-5."), NumberUtils.createNumber("-5."));
    assertTrue("isNumber(\"3.\") failed", NumberUtils.isNumber("3."));
    assertTrue("isNumber(\"123.\") failed", NumberUtils.isNumber("123."));
    assertTrue("isNumber(\"-5.\") failed", NumberUtils.isNumber("-5."));
}