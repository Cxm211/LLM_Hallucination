// org/jsoup/safety/CleanerTest.java
@Test
public void copiesSafeHeadAndBodyElements() {
    String dirty = "<html><head><title>Test</title></head><body><p>Paragraph</p></body></html>";
    Whitelist whitelist = Whitelist.none().addTags("title", "p");
    Document dirtyDoc = Jsoup.parse(dirty);
    Document cleanDoc = new Cleaner(whitelist).clean(dirtyDoc);
    Element head = cleanDoc.head();
    Elements titles = head.getElementsByTag("title");
    assertEquals(1, titles.size());
    assertEquals("Test", titles.first().text());
    Element body = cleanDoc.body();
    Elements paragraphs = body.getElementsByTag("p");
    assertEquals(1, paragraphs.size());
    assertEquals("Paragraph", paragraphs.first().text());
}
