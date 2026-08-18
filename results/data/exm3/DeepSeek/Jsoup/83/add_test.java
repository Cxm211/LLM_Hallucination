// org/jsoup/parser/HtmlParserTest.java
@Test public void tagNameTerminatedByLessThan() {
    String html = "<p<div>content</div>";
    Document doc = Jsoup.parse(html);
    assertEquals("<p></p><div>content</div>", doc.body().html());
}
