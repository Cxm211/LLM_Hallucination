// org/jsoup/helper/DataUtilTest.java::discardsBOMWhenNoCharset
@Test public void discardsBOMWhenNoCharset() {
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
        Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com/", Parser.htmlParser());
        assertEquals("One", doc.head().text());
    }