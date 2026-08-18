// org/jsoup/helper/DataUtilTest.java
@Test
    public void shouldNotThrowExceptionWhenMetaCharsetIsUnsupported() {
        String html = "<html><head><meta charset=\"invalid-charset\"></head><body></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com/", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
    }
