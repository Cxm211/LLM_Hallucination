// org/jsoup/parser/HtmlParserTest.java
@Test public void testTemplateInTableRow() {
    String html = "<table><tr><template><td>Test</td></template></tr></table>";
    Document doc = Jsoup.parse(html);
    Elements templates = doc.select("template");
    assertEquals(1, templates.size());
    Element template = templates.first();
    assertEquals(1, template.childNodeSize());
    assertTrue(template.childNode(0) instanceof Element);
    assertEquals("td", ((Element)template.childNode(0)).tagName());
}