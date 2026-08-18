// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void handlesShortPseudoXmlDecl() {
    String html = "<script> var a='<'; var b='>'; </script>";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("<script> var a='&lt;'; var b='&gt;'; </script>", doc.html());
}