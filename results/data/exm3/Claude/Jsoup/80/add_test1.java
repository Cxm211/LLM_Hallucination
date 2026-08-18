// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void handlesBogusCommentWithExclamation() {
    String xml = "<!DOCTYPE html><root><val>Content</val></root>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    assertEquals("Content", doc.select("val").text());
}