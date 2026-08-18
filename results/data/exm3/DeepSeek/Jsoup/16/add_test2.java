// org/jsoup/nodes/DocumentTypeTest.java
@Test
    public void outerHtmlNameNotHtmlPublicOnly() {
        DocumentType dt = new DocumentType("math", "-//W3C//DTD MathML 2.0//EN", "", "");
        assertEquals("<!DOCTYPE math PUBLIC \"-//W3C//DTD MathML 2.0//EN\">", dt.outerHtml());
    }
