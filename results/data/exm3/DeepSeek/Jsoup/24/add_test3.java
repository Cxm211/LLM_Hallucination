// org/jsoup/parser/HtmlParserTest.java
@Test public void simpleEndTag() {
    String html = "<div></div>";
    Document doc = Jsoup.parse(html);
    Element div = doc.select("div").first();
    assertNotNull(div);
    assertEquals("div", div.tagName());
    assertEquals("", div.text());
}
