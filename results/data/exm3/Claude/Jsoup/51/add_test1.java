// org/jsoup/parser/HtmlParserTest.java
@Test public void testSupportsNonAsciiTagsWithArabic() {
    String body = "<مرحبا>Text</مرحبا>";
    Document doc = Jsoup.parse(body);
    Elements els = doc.select("مرحبا");
    assertEquals("Text", els.text());
}