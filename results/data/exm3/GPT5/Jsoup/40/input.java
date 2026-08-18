// buggy function
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty(name);
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }

// trigger testcase
// org/jsoup/nodes/DocumentTypeTest.java::constructorValidationOkWithBlankName
@Test
    public void constructorValidationOkWithBlankName() {
        DocumentType fail = new DocumentType("","", "", "");
    }

// org/jsoup/parser/HtmlParserTest.java::handlesInvalidDoctypes
@Test public void handlesInvalidDoctypes() {
        // would previously throw invalid name exception on empty doctype
        Document doc = Jsoup.parse("<!DOCTYPE>");
        assertEquals(
                "<!DOCTYPE> <html> <head></head> <body></body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE><html><p>Foo</p></html>");
        assertEquals(
                "<!DOCTYPE> <html> <head></head> <body> <p>Foo</p> </body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE \u0000>");
        assertEquals(
                "<!DOCTYPE �> <html> <head></head> <body></body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
    }
