// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesUnclosedItalic() {
    String h = "<i>italic<p>error</i> text</p>";
    Document doc = Jsoup.parse(h);
    String want = "<i>italic</i>\n<p><i>error</i> text</p>";
    assertEquals(want, doc.body().html());
}
