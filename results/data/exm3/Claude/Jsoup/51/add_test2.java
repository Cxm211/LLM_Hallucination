// org/jsoup/parser/HtmlParserTest.java
@Test public void testSupportsNonAsciiTagsWithGreek() {
    String body = "<Γειά>Data</Γειά>";
    Document doc = Jsoup.parse(body);
    Elements els = doc.select("Γειά");
    assertEquals("Data", els.text());
}