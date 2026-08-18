// org/jsoup/parser/HtmlParserTest.java
@Test public void simpleStartTag() {
    String html = "<div>";
    Document doc = Jsoup.parse(html);
    Element div = doc.select("div").first();
    assertNotNull(div);
    assertEquals("div", div.tagName());
}
