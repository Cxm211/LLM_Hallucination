// org/jsoup/safety/CleanerTest.java
@Test public void handlesDocumentWithoutBody() {
    String dirty = "<html><head><title>Test</title></head></html>";
    Document dirtyDoc = Jsoup.parse(dirty);
    dirtyDoc.body().remove();
    Document cleanDoc = new Cleaner(Whitelist.basic()).clean(dirtyDoc);
    assertNotNull(cleanDoc);
    assertNotNull(cleanDoc.body());
    assertEquals(0, cleanDoc.body().childNodes().size());
}