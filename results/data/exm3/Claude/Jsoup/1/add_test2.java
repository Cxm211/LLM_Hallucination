// org/jsoup/parser/ParserTest.java
@Test public void normaliseEmptyString() {
    String html = "";
    Document doc = Jsoup.parse(html);
    assertEquals("", doc.text());
}