// org/jsoup/nodes/DocumentTypeTest.java
@Test public void testNullPublicId() {
    String base = "<!DOCTYPE html>";
    Document doc = Jsoup.parse(base);
    DocumentType doctype = (DocumentType) doc.childNode(0);
    assertFalse(doctype.has("publicId"));
    assertEquals("<!doctype html>", doc.outerHtml());
}