// org/jsoup/parser/HtmlParserTest.java
@Test public void testSupportsNonAsciiTagsWithCyrillic() {
    String body = "<Привет>Content</Привет>";
    Document doc = Jsoup.parse(body);
    Elements els = doc.select("Привет");
    assertEquals("Content", els.text());
}