// org/jsoup/nodes/NodeTest.java
@Test public void hasAbsAttrVarious() {
    Document doc = Jsoup.parse("<a href='/foo' src='/img' data-uri='/bar'>Link</a>", "http://example.com/");
    Element a = doc.select("a").first();
    assertTrue(a.hasAttr("abs:href"));
    assertTrue(a.hasAttr("abs:src"));
    assertTrue(a.hasAttr("abs:data-uri"));
    assertTrue(a.hasAttr("href"));
    assertTrue(a.hasAttr("src"));
    assertTrue(a.hasAttr("data-uri"));
}
