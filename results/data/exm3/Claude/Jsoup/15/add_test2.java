// org/jsoup/parser/ParserTest.java
@Test public void handlesObjectEndTag() {
    String html = "<object>object data</object>";
    Document doc = Jsoup.parse(html);
    assertEquals("object data", doc.body().text());
    assertEquals("object", doc.body().child(0).tagName());
}