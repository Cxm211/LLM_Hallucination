// org/jsoup/parser/ParserTest.java
@Test public void handlesAppletEndTag() {
    String html = "<applet>applet content</applet>";
    Document doc = Jsoup.parse(html);
    assertEquals("applet content", doc.body().text());
    assertEquals("applet", doc.body().child(0).tagName());
}