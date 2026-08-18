// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesUnclosedFormattingInTable() {
    String h = "<table><tr><td><b>Bold<p>Para</td></tr></table>";
    Document doc = Jsoup.parse(h);
    assertTrue(doc.body().html().contains("<b>Bold</b>"));
    assertTrue(doc.body().html().contains("<b>") && doc.body().html().contains("<p>"));
}