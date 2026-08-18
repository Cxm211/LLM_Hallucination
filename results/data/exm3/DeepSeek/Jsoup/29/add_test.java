// org/jsoup/nodes/DocumentTest.java
@Test public void testTitleInBodyOnly() {
    Document doc = Jsoup.parse("<body><title>Wrong</title></body>");
    assertEquals("", doc.title());
}
