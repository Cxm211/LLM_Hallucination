// org/jsoup/helper/DataUtilTest.java
@Test
public void testUnsupportedCharsetUnquoted() {
    assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset=InvalidCharset123"));
    assertEquals(null, DataUtil.getCharsetFromContentType("text/html;charset=XYZ-999"));
}