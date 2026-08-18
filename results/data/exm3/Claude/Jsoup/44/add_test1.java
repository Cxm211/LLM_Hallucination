// org/jsoup/parser/HtmlParserTest.java
@Test
public void testEndTagProcessing() {
    String html = "<div><span>Content</span></div>";
    Document doc = Jsoup.parse(html);
    Elements spans = doc.select("span");
    assertEquals(1, spans.size());
    assertEquals("Content", spans.first().text());
}