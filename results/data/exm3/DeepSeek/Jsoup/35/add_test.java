// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesUnclosedBold() {
    String h = "<b>bold<p>error</b> text</p>";
    Document doc = Jsoup.parse(h);
    String want = "<b>bold</b>\n<p><b>error</b> text</p>";
    assertEquals(want, doc.body().html());
}
