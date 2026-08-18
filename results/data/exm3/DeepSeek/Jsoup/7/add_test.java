// org/jsoup/nodes/DocumentTest.java
@Test public void testNormalisesNonMetadataElementsInHead() {
    Document doc = Jsoup.parse("<html><head><div>direct</div></head><body></body></html>");
    assertEquals("<html><head></head><body><div>direct</div></body></html>", TextUtil.stripNewlines(doc.html()));
    doc = Jsoup.parse("<html><head><noscript><p>inside</p></noscript></head><body></body></html>");
    assertEquals("<html><head><noscript></noscript></head><body><p>inside</p></body></html>", TextUtil.stripNewlines(doc.html()));
    doc = Jsoup.parse("<html><head><noscript><div><span>deep</span></div></noscript></head><body></body></html>");
    assertEquals("<html><head><noscript></noscript></head><body><div><span>deep</span></div></body></html>", TextUtil.stripNewlines(doc.html()));
}
