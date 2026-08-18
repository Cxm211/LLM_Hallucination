// org/jsoup/nodes/DocumentTypeTest.java
@Test
    public void outerHtmlNameNotHtmlSystemOnly() {
        DocumentType dt = new DocumentType("foo", "", "system.dtd", "");
        assertEquals("<!DOCTYPE foo \"system.dtd\">", dt.outerHtml());
    }
