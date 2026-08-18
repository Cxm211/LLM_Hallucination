// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithMultipleTopLevelElements() {
    String multiple = "<p>First</p><p>Second</p><div>Third</div>";
    assertFalse(Jsoup.isValid(multiple, Whitelist.basic()));
}