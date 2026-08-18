// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidWithDataNode() {
    String withDataNode = "<p>Text</p><script>alert('test')</script>";
    assertFalse(Jsoup.isValid(withDataNode, Whitelist.basic()));
}