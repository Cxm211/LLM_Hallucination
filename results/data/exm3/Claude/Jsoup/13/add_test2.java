// org/jsoup/nodes/NodeTest.java
@Test public void handlesAbsPrefixWithMissingAttr() {
    Document doc = Jsoup.parse("<a>No href</a>", "http://jsoup.org/");
    Element a = doc.select("a").first();
    assertFalse(a.hasAttr("abs:href"));
    assertFalse(a.hasAttr("href"));
}