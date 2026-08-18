// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesMultipleControlCharsInAttributeName() {
        Document doc = Jsoup.parse("<p><a \u0006\u0007=test>Link</a></p>");
        assertEquals("<p><a>Link</a></p>", doc.body().html());
    }