// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithDisallowedAttribute() {
    String disallowedAttr = "<p id='test'>Text</p>";
    assertFalse(Jsoup.isValid(disallowedAttr, Whitelist.basic()));
}