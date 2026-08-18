// org/jsoup/parser/ParserTest.java
@Test public void handlesMarqueeEndTag() {
    String html = "<marquee>content</marquee>";
    Document doc = Jsoup.parse(html);
    assertEquals("content", doc.body().text());
    assertEquals("marquee", doc.body().child(0).tagName());
}