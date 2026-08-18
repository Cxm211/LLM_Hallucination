// org/jsoup/parser/HtmlParserTest.java
@Test public void preWithoutLeadingNewline() {
    Document doc = Jsoup.parse("<pre>No leading newline</pre>");
    Element pre = doc.selectFirst("pre");
    assertEquals("No leading newline", pre.text());
    assertEquals("No leading newline", pre.wholeText());
}