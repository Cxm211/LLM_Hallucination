// org/jsoup/parser/HtmlParserTest.java::fallbackToUtfIfCantEncode
@Test public void fallbackToUtfIfCantEncodeHttpEquiv() throws IOException {
        String in = "<html><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-2022-CN\"/>One</html>";
        Document doc = Jsoup.parse(new ByteArrayInputStream(in.getBytes()), null, "");
        assertEquals("UTF-8", doc.charset().name());
        assertEquals("One", doc.text());
        String html = doc.outerHtml();
        assertEquals("<html><head><meta charset=\"UTF-8\"></head><body>One</body></html>", TextUtil.stripNewlines(html));
    }