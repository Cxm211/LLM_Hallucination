// org/jsoup/nodes/DocumentTypeTest.java
@Test
pubic void outerHtmlGenerationWithSystemIdOnly() {
    DocumentType systemOnly = new DocumentType("html", "", "system-id-test", "");
    assertEquals("<!DOCTYPE html \"system-id-test\">", systemOnly.outerHtml());
}