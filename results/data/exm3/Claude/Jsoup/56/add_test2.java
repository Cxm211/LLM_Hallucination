// org/jsoup/nodes/DocumentTypeTest.java
@Test public void testOnlySystemId() {
    String base = "<!DOCTYPE html SYSTEM \"test.dtd\">";
    Document doc = Jsoup.parse(base);
    DocumentType doctype = (DocumentType) doc.childNode(0);
    assertFalse(doctype.has("publicId"));
    assertTrue(doctype.has("systemId"));
    assertEquals(base, doc.outerHtml());
}