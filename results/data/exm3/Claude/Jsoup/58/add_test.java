// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithEmptyInput() {
    String empty = "";
    assertTrue(Jsoup.isValid(empty, Whitelist.basic()));
    assertTrue(Jsoup.isValid(empty, Whitelist.none()));
}