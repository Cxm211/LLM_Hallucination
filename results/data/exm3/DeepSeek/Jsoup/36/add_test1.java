// org/jsoup/helper/DataUtilTest.java
@Test
    public void shouldTrimAndUseCharsetAttribute() {
        String html = "<html><head><meta charset=\" ISO-8859-1 \"></head><body></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com/", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().displayName());
    }
