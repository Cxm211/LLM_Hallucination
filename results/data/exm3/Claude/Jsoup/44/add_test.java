// org/jsoup/parser/HtmlParserTest.java
@Test
public void testStartTagWithNameOnly() {
    String html = "<div><p>Test</p></div>";
    Document doc = Jsoup.parse(html);
    assertEquals("Test", doc.select("p").text());
    assertEquals("div", doc.body().child(0).tagName());
}