// org/jsoup/nodes/DocumentTypeTest.java
@Test
public void outerHtmlGenerationWithPublicIdOnly() {
    DocumentType publicOnly = new DocumentType("html", "public-id-test", "", "");
    assertEquals("<!DOCTYPE html PUBLIC \"public-id-test\">", publicOnly.outerHtml());
}