// org/jsoup/helper/DataUtilTest.java
@Test public void testMixedCaseCharset() {
    assertEquals(\"Utf-8\", DataUtil.getCharsetFromContentType(\"text/html; charset=Utf-8\"));
    assertEquals(\"Us-Ascii\", DataUtil.getCharsetFromContentType(\"text/html; charset=Us-Ascii\"));
}
