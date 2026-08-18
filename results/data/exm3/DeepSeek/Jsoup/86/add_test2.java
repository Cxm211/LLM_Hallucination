// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
    public void asXmlDeclarationHandlesXmlDeclarationWithoutTrailingQuestion() {
        Document doc = Jsoup.parse("<!--?xml version='1.0'-->", "", Parser.xmlParser());
        Comment comment = (Comment) doc.childNode(0);
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertTrue(decl.isDeclaration());
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
    }
