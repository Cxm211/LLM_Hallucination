// org/jsoup/parser/HtmlParserTest.java
@Test public void textareaSkipsFirstNewline() {
    Document doc = Jsoup.parse("<textarea>\n\nFirst\nSecond\n</textarea>");
    Element textarea = doc.selectFirst("textarea");
    assertEquals("First Second", textarea.text());
    assertEquals("\nFirst\nSecond\n", textarea.wholeText());
}