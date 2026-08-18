// org/jsoup/parser/ParserTest.java
@Test public void parsesEmptyAttributeKeyWithQuotedValue() {
    String html = "<p =\"quoted\">Test</p>";
    Document doc = Jsoup.parse(html);
    assertEquals("<p>Test</p>", doc.body().html());
}