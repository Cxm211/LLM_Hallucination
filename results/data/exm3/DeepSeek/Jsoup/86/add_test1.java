// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
    public void asXmlDeclarationHandlesDoctype() {
        Document doc = Jsoup.parse("<!--!DOCTYPE html-->", "", Parser.xmlParser());
        Comment comment = (Comment) doc.childNode(0);
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertFalse(decl.isDeclaration());
        assertEquals("DOCTYPE", decl.name());
    }
