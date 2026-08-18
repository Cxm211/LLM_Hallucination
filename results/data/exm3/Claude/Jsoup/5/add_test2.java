// org/jsoup/parser/ParserTest.java
@Test public void parsesMultipleEmptyAttributeKeys() {
    String html = "<div = =x =>Content</div>";
    Document doc = Jsoup.parse(html);
    assertEquals("<div>Content</div>", doc.body().html());
}