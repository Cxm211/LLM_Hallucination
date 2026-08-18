// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void handlesExclamationPseudoXmlDecl() {
    String html = "<script> var a='<!test>'; </script>";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertTrue(doc.html().contains("<script>"));
}