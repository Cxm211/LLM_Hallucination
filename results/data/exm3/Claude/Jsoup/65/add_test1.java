// org/jsoup/parser/HtmlParserTest.java
@Test public void testTemplateInTableBody() {
    String html = "<table><tbody><template><tr><td>Content</td></tr></template></tbody></table>";
    Document doc = Jsoup.parse(html);
    Elements templates = doc.select("template");
    assertEquals(1, templates.size());
    Element template = templates.first();
    assertEquals(1, template.childNodeSize());
    assertTrue(template.childNode(0) instanceof Element);
    assertEquals("tr", ((Element)template.childNode(0)).tagName());
}