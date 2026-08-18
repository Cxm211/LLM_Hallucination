// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithCommentOnly() {
    String commentOnly = "<!-- comment -->";
    assertFalse(Jsoup.isValid(commentOnly, Whitelist.basic()));
}