// org/jsoup/nodes/DocumentTypeTest.java
@Test
    public void constructorWithEmptyNameAndNonEmptyIds() {
        DocumentType docType = new DocumentType("", "publicId", "systemId", "http://example.com");
        assertEquals("", docType.attr("name"));
        assertEquals("publicId", docType.attr("publicId"));
        assertEquals("systemId", docType.attr("systemId"));
        assertEquals("http://example.com", docType.baseUri());
    }
