// org/jsoup/nodes/NodeTest.java
@Test public void handlesAbsPrefixWithFullUrl() {
    Document doc = Jsoup.parse("<img src='https://example.com/image.png'>", "http://jsoup.org/");
    Element img = doc.select("img").first();
    assertTrue(img.hasAttr("abs:src"));
    assertEquals("https://example.com/image.png", img.attr("abs:src"));
}