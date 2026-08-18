// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithPlainText() {
    String plainText = "Just plain text without any tags";
    assertTrue(Jsoup.isValid(plainText, Whitelist.basic()));
    assertTrue(Jsoup.isValid(plainText, Whitelist.none()));
}