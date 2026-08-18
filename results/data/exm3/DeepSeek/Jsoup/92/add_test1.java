// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void duplicateAttributesEmptyValue() {
    String html = "<p attr attr=''>Text</p>";
    Parser parser = Parser.xmlParser().setTrackErrors(10);
    Document doc = parser.parseInput(html, "");
    assertEquals("<p attr=\"\">Text</p>", doc.selectFirst("p").outerHtml());
    assertEquals(1, parser.getErrors().size());
    assertEquals("Duplicate attribute", parser.getErrors().get(0).getErrorMessage());
}
