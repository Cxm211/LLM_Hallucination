// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidCommentInside() {
    String html = "<p><!-- comment -->Test</p>";
    assertFalse(Jsoup.isValid(html, Whitelist.basic()));
}
