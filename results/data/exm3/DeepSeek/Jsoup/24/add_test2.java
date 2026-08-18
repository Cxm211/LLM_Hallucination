// org/jsoup/parser/HtmlParserTest.java
@Test public void selfClosingTag() {
    String html = "<br/>";
    Document doc = Jsoup.parse(html);
    Element br = doc.select("br").first();
    assertNotNull(br);
    assertEquals("br", br.tagName());
    assertTrue(br.tag().isSelfClosing());
}
