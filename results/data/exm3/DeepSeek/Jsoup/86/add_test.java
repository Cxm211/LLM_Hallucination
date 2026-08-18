// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
    public void asXmlDeclarationHandlesShortData() {
        // Comment with just '?'
        Document doc1 = Jsoup.parse("<!--?-->", "", Parser.xmlParser());
        Comment comment1 = (Comment) doc1.childNode(0);
        XmlDeclaration decl1 = comment1.asXmlDeclaration();
        assertNull(decl1);
        // Comment with just '!'
        Document doc2 = Jsoup.parse("<!--!-->", "", Parser.xmlParser());
        Comment comment2 = (Comment) doc2.childNode(0);
        XmlDeclaration decl2 = comment2.asXmlDeclaration();
        assertNull(decl2);
    }
