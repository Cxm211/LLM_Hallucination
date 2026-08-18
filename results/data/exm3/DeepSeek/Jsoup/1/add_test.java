// org/jsoup/parser/ParserTest.java
@Test public void normaliseMovesLeadingTextNode() {
    String html = "foo <b>bar</b>";
    Document doc = Jsoup.parse(html);
    assertEquals("foo bar", doc.text());
}
