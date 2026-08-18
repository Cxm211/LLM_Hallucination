// org/jsoup/parser/ParserTest.java
@Test public void normaliseMultipleTextNodes() {
    String html = "first second third";
    Document doc = Jsoup.parse(html);
    assertEquals("first second third", doc.text());
}