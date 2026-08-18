// org/jsoup/helper/DataUtilTest.java::testSingleQuotedCharset
@Test public void testSingleQuotedCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset='Unsupported'"));
    }