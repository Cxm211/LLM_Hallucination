// org/jsoup/parser/HtmlParserTest.java
@Test public void fallbackForXmlDeclarationUnencodableCharset() throws IOException {
        String in = "<?xml encoding='ISO-2022-CN'?><html><body>Test</body></html>";
        Document doc = Jsoup.parse(new ByteArrayInputStream(in.getBytes()), null, "");
        assertEquals("UTF-8", doc.charset().name());
        String html = doc.outerHtml();
        assertTrue(html.contains("UTF-8"));
    }
