// org/jsoup/helper/DataUtilTest.java
@Test public void testSingleQuotedCharset() {
    assertEquals(\"utf-8\", DataUtil.getCharsetFromContentType(\"text/html; charset='utf-8'\"));
    assertEquals(\"UTF-8\", DataUtil.getCharsetFromContentType(\"text/html; charset='UTF-8'\"));
}
