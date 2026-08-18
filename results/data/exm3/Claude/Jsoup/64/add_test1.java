// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesKnownEmptyTitle() {
    String h = "<html><head><title /><meta name=foo></head><body>One</body></html>";
    Document doc = Jsoup.parse(h);
    assertEquals("<html><head><title></title><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
}