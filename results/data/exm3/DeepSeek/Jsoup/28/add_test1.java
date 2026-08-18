// org/jsoup/parser/HtmlParserTest.java
@Test public void baseEntityMatchGreedy() {
    String html = "&ampfoo";
    Document doc = Jsoup.parse(html);
    assertEquals("&amp;foo", doc.body().html());
}
