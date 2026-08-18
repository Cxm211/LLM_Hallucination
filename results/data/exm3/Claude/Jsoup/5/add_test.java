// org/jsoup/parser/ParserTest.java
@Test public void parsesEmptyAttributeKeyWithValue() {
    String html = "<p =value>Test</p>";
    Document doc = Jsoup.parse(html);
    assertEquals("<p>Test</p>", doc.body().html());
}