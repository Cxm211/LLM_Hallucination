// buggy function
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }

    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum.append("<!DOCTYPE html");
        if (!StringUtil.isBlank(attr("publicId")))
            accum.append(" PUBLIC \"").append(attr("publicId")).append("\"");
        if (!StringUtil.isBlank(attr("systemId")))
            accum.append(' ').append(attr("systemId")).append("\"");
        accum.append('>');
    }

// trigger testcase
// org/jsoup/nodes/DocumentTypeTest.java::constructorValidationThrowsExceptionOnBlankName
public void constructorValidationThrowsExceptionOnBlankName() {
        DocumentType fail = new DocumentType("","", "", "");
    }

// org/jsoup/nodes/DocumentTypeTest.java::outerHtmlGeneration
@Test public void outerHtmlGeneration() {
        DocumentType html5 = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", html5.outerHtml());

        DocumentType publicDocType = new DocumentType("html", "-//IETF//DTD HTML//", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//IETF//DTD HTML//\">", publicDocType.outerHtml());

        DocumentType systemDocType = new DocumentType("html", "", "http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd", "");
        assertEquals("<!DOCTYPE html \"http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd\">", systemDocType.outerHtml());

        DocumentType combo = new DocumentType("notHtml", "--public", "--system", "");
        assertEquals("<!DOCTYPE notHtml PUBLIC \"--public\" \"--system\">", combo.outerHtml());
    }
