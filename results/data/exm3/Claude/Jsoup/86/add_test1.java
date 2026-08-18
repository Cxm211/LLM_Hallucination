// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void handlesEmptyPseudoXmlDecl() {
    String html = "<script> var a='<??>'; </script>";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("<script> var a='\n <!--?-->'; </script>", doc.html());
}