// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
    public void handlesDocTypeDecl() {
        String xml = "<!DOCTYPE note><note>test</note>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("test", doc.select("note").text());
    }
