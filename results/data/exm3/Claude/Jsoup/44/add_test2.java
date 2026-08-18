// org/jsoup/parser/HtmlParserTest.java
@Test
public void testNestedTagsWithAttributes() {
    String html = "<table><tr><td class='test'>Cell</td></tr></table>";
    Document doc = Jsoup.parse(html);
    Element td = doc.select("td").first();
    assertNotNull(td);
    assertEquals("test", td.attr("class"));
    assertEquals("Cell", td.text());
}