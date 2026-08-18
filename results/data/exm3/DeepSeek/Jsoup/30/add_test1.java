// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidOnlyComment() {
    String html = "<!-- comment -->";
    assertFalse(Jsoup.isValid(html, Whitelist.basic()));
}
