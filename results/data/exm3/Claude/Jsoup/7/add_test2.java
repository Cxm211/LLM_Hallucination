// org/jsoup/nodes/DocumentTest.java
@Test public void testNormalisesEmptyNoscriptInHead() {
    Document doc = Jsoup.parse("<html><head><noscript></noscript></head><body></body></html>");
    assertEquals("<html><head><noscript></noscript></head><body></body></html>", TextUtil.stripNewlines(doc.html()));
}