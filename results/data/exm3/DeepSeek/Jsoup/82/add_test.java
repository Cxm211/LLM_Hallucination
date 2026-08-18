// org/jsoup/parser/HtmlParserTest.java
@Test public void fallbackForExplicitUnencodableCharset() throws IOException {
        String in = "<html><body>Test</body></html>";
        Document doc = Jsoup.parse(new ByteArrayInputStream(in.getBytes()), "ISO-2022-CN", "");
        assertEquals("UTF-8", doc.charset().name());
    }
