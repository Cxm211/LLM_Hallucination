// org/jsoup/nodes/DocumentTest.java
@Test public void testTitleConsecutiveSpaces() {
    Document doc = Jsoup.parse("<title>Multiple     consecutive    spaces</title>");
    assertEquals("Multiple consecutive spaces", doc.title());
}