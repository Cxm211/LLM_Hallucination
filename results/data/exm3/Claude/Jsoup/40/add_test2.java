// org/jsoup/parser/HtmlParserTest.java
@Test
public void handlesDoctypeWithWhitespace() {
    Document doc = Jsoup.parse("<!DOCTYPE   >");
    assertEquals("<!DOCTYPE> <html> <head></head> <body></body> </html>",
                 StringUtil.normaliseWhitespace(doc.outerHtml()));
}