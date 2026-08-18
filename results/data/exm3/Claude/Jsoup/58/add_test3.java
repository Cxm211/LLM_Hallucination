// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithNestedAllowedTags() {
    String nested = "<p><b><i>Nested allowed tags</i></b></p>";
    assertTrue(Jsoup.isValid(nested, Whitelist.basic()));
    assertFalse(Jsoup.isValid(nested, Whitelist.none()));
}