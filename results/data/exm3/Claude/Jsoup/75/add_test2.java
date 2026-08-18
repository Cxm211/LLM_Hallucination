// org/jsoup/nodes/ElementTest.java
@Test
public void booleanAttributeOutputXmlSyntax() {
    Document doc = Jsoup.parse("<input checked='' disabled='disabled'>");
    doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    Element input = doc.selectFirst("input");
    assertEquals("<input checked=\"\" disabled=\"disabled\" />", input.outerHtml());
}