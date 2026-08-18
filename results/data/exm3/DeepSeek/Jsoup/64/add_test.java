// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesSelfClosingScriptWithContent() {
        String h = "<html><head><script />alert('hi')</script></head><body></body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><script>alert('hi')</script></head><body></body></html>", TextUtil.stripNewlines(doc.html()));
    }
