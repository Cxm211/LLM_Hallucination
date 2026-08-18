// org/jsoup/nodes/DocumentTypeTest.java
@Test public void testNullSystemId() {
    String base = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
    Document doc = Jsoup.parse(base);
    DocumentType doctype = (DocumentType) doc.childNode(0);
    assertTrue(doctype.has("publicId"));
    assertFalse(doctype.has("systemId"));
    assertEquals(base, doc.outerHtml());
}