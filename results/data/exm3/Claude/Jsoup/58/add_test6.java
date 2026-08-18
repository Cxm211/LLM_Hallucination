// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidDocumentWithEmptyBody() {
    String emptyBody = "<html><head></head><body></body></html>";
    Whitelist relaxed = Whitelist.relaxed();
    Cleaner cleaner = new Cleaner(relaxed);
    Document doc = Jsoup.parse(emptyBody);
    assertTrue(cleaner.isValid(doc));
}