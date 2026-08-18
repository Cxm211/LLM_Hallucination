// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesKnownEmptyScript() {
    String h = "<html><head><script /><meta name=foo></head><body>One</body></html>";
    Document doc = Jsoup.parse(h);
    assertEquals("<html><head><script></script><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
}