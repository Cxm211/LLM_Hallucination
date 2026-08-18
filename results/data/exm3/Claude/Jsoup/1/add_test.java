// org/jsoup/parser/ParserTest.java
@Test public void normaliseSingleTextNode() {
    String html = "single";
    Document doc = Jsoup.parse(html);
    assertEquals("single", doc.text());
}