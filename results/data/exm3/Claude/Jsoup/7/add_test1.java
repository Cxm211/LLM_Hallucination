// org/jsoup/nodes/DocumentTest.java
@Test public void testNormalisesMultipleBodies() {
    Document doc = Jsoup.parse("<html><head></head><body><p>First</p></body><body><p>Second</p></body></html>");
    assertEquals("<html><head></head><body><p>First</p><p>Second</p></body></html>", TextUtil.stripNewlines(doc.html()));
}