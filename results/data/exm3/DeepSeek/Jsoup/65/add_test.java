// org/jsoup/parser/HtmlParserTest.java
@Test
public void testTemplateInTableRow() {
    String html = "<table><tr><template><td>Test</td></template></tr></table>";
    Document doc = Jsoup.parse(html);
    Element template = doc.select("template").first();
    assertNotNull(template);
    assertEquals(1, template.children().size());
    assertEquals("td", template.child(0).tagName());
}
