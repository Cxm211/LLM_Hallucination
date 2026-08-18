// org/jsoup/parser/ParserTest.java
@Test public void normaliseMovesMultipleTextNodesAroundElements() {
    String html = "a <b>b</b> c";
    Document doc = Jsoup.parse(html);
    assertEquals("a b c", doc.text());
}
