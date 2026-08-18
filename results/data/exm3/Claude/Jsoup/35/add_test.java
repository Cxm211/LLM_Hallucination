// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesNestedFormattingElements() {
    String h = "<b><i>Bold and italic</b> just italic</i>";
    Document doc = Jsoup.parse(h);
    String want = "<b><i>Bold and italic</i></b><i> just italic</i>";
    assertEquals(want, doc.body().html());
}