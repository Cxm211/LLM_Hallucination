// org/jsoup/safety/CleanerTest.java
@Test public void handlesEmptyDocument() {
    String dirty = "";
    Document dirtyDoc = Jsoup.parse(dirty);
    Document cleanDoc = new Cleaner(Whitelist.basic()).clean(dirtyDoc);
    assertNotNull(cleanDoc);
    assertNotNull(cleanDoc.body());
    assertEquals(0, cleanDoc.body().childNodes().size());
}