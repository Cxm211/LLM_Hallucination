// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java
public void testAppendFixedWidthPadRightVarious() {
    // Test width <= 0
    StrBuilder sb1 = new StrBuilder();
    sb1.append("base");
    sb1.appendFixedWidthPadRight("test", 0, '-');
    assertEquals("base", sb1.toString());
    sb1.appendFixedWidthPadRight("test", -1, '-');
    assertEquals("base", sb1.toString());

    // Test strLen < width: padding
    StrBuilder sb2 = new StrBuilder();
    sb2.appendFixedWidthPadRight("foo", 5, '-');
    assertEquals("foo--", sb2.toString());

    // Test strLen == width: no padding, no truncation
    StrBuilder sb3 = new StrBuilder();
    sb3.appendFixedWidthPadRight("foo", 3, '-');
    assertEquals("foo", sb3.toString());

    // Test strLen > width: truncation with small capacity
    StrBuilder sb4 = new StrBuilder(1);
    sb4.appendFixedWidthPadRight("foobar", 3, '-');
    assertEquals("foo", sb4.toString());

    // Test null object with getNullText() length > width and small capacity
    StrBuilder sb5 = new StrBuilder(1);
    sb5.appendFixedWidthPadRight(null, 2, '-');
    assertEquals("nu", sb5.toString());

    // Test null object with getNullText() length == width
    StrBuilder sb6 = new StrBuilder();
    sb6.appendFixedWidthPadRight(null, 4, '-');
    assertEquals("null", sb6.toString());

    // Test null object with getNullText() length < width
    StrBuilder sb7 = new StrBuilder();
    sb7.appendFixedWidthPadRight(null, 6, '-');
    assertEquals("null--", sb7.toString());
}
