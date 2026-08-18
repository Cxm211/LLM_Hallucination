// org/jsoup/parser/HtmlParserTest.java
@Test
public void testTemplateInTableBody() {
    String html = "<table><tbody><template><tr><td>Test</td></tr></template></tbody></table>";
    Document doc = Jsoup.parse(html);
    Element template = doc.select("template").first();
    assertNotNull(template);
    assertEquals(1, template.children().size());
    assertEquals("tr", template.child(0).tagName());
}
