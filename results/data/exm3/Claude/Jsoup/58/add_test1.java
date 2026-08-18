// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithWhitespaceOnly() {
    String whitespace = "   \n\t  ";
    assertTrue(Jsoup.isValid(whitespace, Whitelist.basic()));
    assertTrue(Jsoup.isValid(whitespace, Whitelist.none()));
}