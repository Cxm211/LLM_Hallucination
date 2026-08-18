// org/jsoup/nodes/NodeTest.java
@Test public void handlesAbsPrefixWithEmptyBaseUri() {
    Document doc = Jsoup.parse("<a href='relative/path'>Link</a>");
    Element a = doc.select("a").first();
    assertFalse(a.hasAttr("abs:href"));
    assertTrue(a.hasAttr("href"));
    assertEquals("", a.absUrl("href"));
}