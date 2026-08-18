// org/jsoup/parser/AttributeParseTest.java
@Test public void selfClosingWithSlashBeforeEOF() {
    String html = "<img /";
    Document doc = Jsoup.parse(html);
    assertEquals("<img>", doc.body().html());
}