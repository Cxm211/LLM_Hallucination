// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesControlCodeInBooleanAndEmptyAttributeName() {
        Document doc = Jsoup.parse("<p><a foo\06>One</a><a foo\06=>Two</a></p>");
        assertEquals("<p><a foo>One</a><a foo=\"\">Two</a></p>", doc.body().html());
    }