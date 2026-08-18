// org/jsoup/parser/HtmlParserTest.java
@Test public void fallbackToUtfIfCantEncodeWithHttpEquiv() throws IOException {
    // Test with http-equiv meta tag instead of charset attribute
    String in = "<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-2022-CN\"/>Two</html>";
    Document doc = Jsoup.parse(new ByteArrayInputStream(in.getBytes()), null, "");

    assertEquals("UTF-8", doc.charset().name());
    assertEquals("Two", doc.text());

    String html = doc.outerHtml();
    assertEquals("<html><head><meta charset=\"UTF-8\"></head><body>Two</body></html>", TextUtil.stripNewlines(html));
}