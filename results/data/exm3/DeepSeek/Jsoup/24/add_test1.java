// org/jsoup/parser/HtmlParserTest.java
@Test public void startTagWithAttribute() {
    String html = "<div id=foo>";
    Document doc = Jsoup.parse(html);
    Element div = doc.select("div").first();
    assertNotNull(div);
    assertEquals("div", div.tagName());
    assertEquals("foo", div.attr("id"));
}
