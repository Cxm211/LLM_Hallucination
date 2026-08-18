// org/jsoup/nodes/DocumentTypeTest.java
@Test
    public void outerHtmlNameNotHtmlNoIds() {
        DocumentType dt = new DocumentType("svg", "", "", "");
        assertEquals("<!DOCTYPE svg>", dt.outerHtml());
    }
