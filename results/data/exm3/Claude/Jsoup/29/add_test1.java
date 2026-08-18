// org/jsoup/nodes/DocumentTest.java
@Test public void testTitleLeadingTrailingWhitespace() {
    Document doc = Jsoup.parse("<title>   Leading and trailing   </title>");
    assertEquals("Leading and trailing", doc.title());
}