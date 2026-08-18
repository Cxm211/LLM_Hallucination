// org/jsoup/nodes/DocumentTypeTest.java
@Test
public void constructorAcceptsWhitespaceOnlyName() {
    DocumentType docType = new DocumentType("   ", "", "", "");
    assertEquals("   ", docType.attr("name"));
}