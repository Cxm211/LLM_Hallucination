// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithMultipleComments() {
    String multipleComments = "<!-- comment1 --><!-- comment2 --><p>Text</p>";
    assertFalse(Jsoup.isValid(multipleComments, Whitelist.basic()));
}